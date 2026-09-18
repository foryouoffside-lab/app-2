package com.example

import com.example.util.BREAK_SECONDS
import com.example.util.spokenCount
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BreakCountdownTest {

    @Test
    fun countsEverySecondDownToZero() {
        val spoken = (BREAK_SECONDS - 1 downTo 0).map { spokenCount(it) }

        // 19 numbers plus the closing line: one utterance per elapsed second, no gaps.
        assertEquals(BREAK_SECONDS, spoken.size)
        assertEquals("19", spoken.first())
        assertEquals("1", spoken[BREAK_SECONDS - 2])
    }

    @Test
    fun endsOnWordsSoTheLastThingHeardIsNotANumber() {
        assertTrue(spokenCount(0).startsWith("Done"))
        // A late tick must not resurrect the countdown.
        assertEquals(spokenCount(0), spokenCount(-1))
    }
}
