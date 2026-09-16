package com.example.model

/**
 * What the coach should do on a given 1 Hz tick.
 *
 * The player runs a single one-second tick that drives the timer text, the progress
 * fill and the spoken cue together, so voice can never drift from the clock.
 */
enum class CueKind { ANNOUNCE, MIDPOINT, COUNT, END }

data class SessionCue(val kind: CueKind, val text: String)

/**
 * Cue for [secondsRemaining] of [phase], or null for a silent tick.
 *
 * Precedence is deliberate: entry wins over the countdown so a very short phase is
 * still named, and the countdown wins over the midpoint line so "two" is never
 * replaced by a coaching sentence on a phase whose half-way point is second 2.
 */
fun cueFor(phase: ExercisePhase, secondsRemaining: Int): SessionCue? {
    val duration = phase.durationSeconds
    return when {
        secondsRemaining >= duration ->
            SessionCue(CueKind.ANNOUNCE, "${phase.title}. ${phase.instruction}")

        secondsRemaining <= 0 -> SessionCue(CueKind.END, "")

        secondsRemaining <= 3 -> SessionCue(CueKind.COUNT, secondsRemaining.toString())

        // Only long enough phases get a mid-way line; on a short phase it would
        // land on top of the announcement that is still being spoken.
        duration >= 12 && secondsRemaining == duration / 2 ->
            SessionCue(CueKind.MIDPOINT, phase.physiologicalBenefit)

        else -> null
    }
}

/** Cue for a rest interval. Rest counts the last five seconds aloud, then ends. */
fun restCueFor(secondsRemaining: Int, nextPhaseTitle: String, restSeconds: Int): SessionCue? = when {
    secondsRemaining >= restSeconds ->
        SessionCue(CueKind.ANNOUNCE, "Rest. Next up, $nextPhaseTitle")

    secondsRemaining <= 0 -> SessionCue(CueKind.END, "")
    secondsRemaining <= 5 -> SessionCue(CueKind.COUNT, secondsRemaining.toString())
    else -> null
}
