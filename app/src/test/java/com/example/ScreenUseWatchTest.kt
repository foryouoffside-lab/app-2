package com.example

import com.example.util.WATCH_POLL_MILLIS
import com.example.util.WatchState
import com.example.util.advanceWatch
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

private val WATCHED = setOf("com.instagram.android", "com.google.android.youtube")
private const val THRESHOLD = 20 * 60_000L

/** Runs [samples] polls of [pkg], returning the final state and whether a nudge fired. */
private fun run(
    samples: Int,
    pkg: String?,
    start: WatchState = WatchState(),
    startAt: Long = 1_000_000L,
    step: Long = WATCH_POLL_MILLIS
): Pair<WatchState, Int> {
    var state = start
    var nudges = 0
    repeat(samples) { i ->
        val result = advanceWatch(state, pkg, startAt + (i + 1) * step, WATCHED, THRESHOLD)
        state = result.state
        if (result.shouldNudge) nudges++
    }
    return state to nudges
}

class ScreenUseWatchTest {

    @Test
    fun nudgesOnceAfterThresholdOfUnbrokenScrolling() {
        val polls = (THRESHOLD / WATCH_POLL_MILLIS).toInt()
        // +1: the first poll only establishes a baseline, it credits no elapsed time.
        val (_, nudges) = run(polls + 1, "com.instagram.android")

        assertEquals(1, nudges)
    }

    @Test
    fun leavingWatchedAppsResetsTheCount() {
        val (partial, _) = run(20, "com.instagram.android")
        assertTrue(partial.continuousMillis > 0)

        val (afterLeaving, nudges) = run(1, "com.example.somelauncher", start = partial, startAt = 2_000_000L)
        assertEquals(0, afterLeaving.continuousMillis)
        assertEquals(0, nudges)
    }

    @Test
    fun appHoppingBetweenWatchedAppsDoesNotDodgeTheBreak() {
        var state = WatchState()
        var nudged = false
        var now = 1_000_000L
        // Alternate Instagram/YouTube for the full interval; it is the same near work.
        repeat((THRESHOLD / WATCH_POLL_MILLIS).toInt() + 1) { i ->
            now += WATCH_POLL_MILLIS
            val pkg = if (i % 2 == 0) "com.instagram.android" else "com.google.android.youtube"
            val result = advanceWatch(state, pkg, now, WATCHED, THRESHOLD)
            state = result.state
            if (result.shouldNudge) nudged = true
        }
        assertTrue(nudged)
    }

    @Test
    fun timeWhileDozedOrAsleepIsNotCredited() {
        val (partial, _) = run(10, "com.instagram.android")

        // Service suspended for an hour, then one sample: that hour was not scrolling.
        val jumped = advanceWatch(partial, "com.instagram.android", partial.lastSampleAt + 3_600_000L, WATCHED, THRESHOLD)

        assertFalse(jumped.shouldNudge)
        assertEquals(partial.continuousMillis, jumped.state.continuousMillis)
    }

    @Test
    fun countRestartsAfterANudgeRatherThanFiringEverySample() {
        val polls = (THRESHOLD / WATCH_POLL_MILLIS).toInt()
        val (_, nudges) = run(polls + 5, "com.instagram.android")

        assertEquals(1, nudges)
    }
}
