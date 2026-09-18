package com.example.util

import java.util.Calendar
import java.util.TimeZone

val BREAK_INTERVAL_OPTIONS = listOf(20, 30, 45, 60)
val BREAK_START_HOUR_RANGE = 0..22
val BREAK_END_HOUR_RANGE = 1..23

/**
 * Local-only schedule for screen-break nudges.
 *
 * The end is exclusive: a 09:00-18:00 schedule can notify at 17:59, not 18:00.
 * Keeping this as wall-clock data also means a time-zone change does not turn a workday
 * reminder into a night-time reminder.
 */
data class BreakReminderSettings(
    val enabled: Boolean = false,
    /** Nudge after sustained use of a content app instead of on the clock alone. */
    val smartEnabled: Boolean = false,
    val intervalMinutes: Int = 20,
    val startHour: Int = 9,
    val endHour: Int = 18,
    val weekdaysOnly: Boolean = true
) {
    fun sanitized(): BreakReminderSettings {
        val start = startHour.coerceIn(BREAK_START_HOUR_RANGE)
        val end = endHour.coerceIn((start + 1)..BREAK_END_HOUR_RANGE.last)
        return copy(
            intervalMinutes = intervalMinutes.takeIf { it in BREAK_INTERVAL_OPTIONS } ?: 20,
            startHour = start,
            endHour = end
        )
    }
}

/** True only while a notification is still useful; delayed alarms never spill into off-hours. */
fun isBreakReminderActiveAt(
    nowMillis: Long,
    timeZone: TimeZone,
    rawSettings: BreakReminderSettings
): Boolean {
    if (!rawSettings.enabled) return false
    val settings = rawSettings.sanitized()
    val now = Calendar.getInstance(timeZone).apply { timeInMillis = nowMillis }
    if (settings.weekdaysOnly && now.isWeekend()) return false
    val minuteOfDay = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE)
    return minuteOfDay >= settings.startHour * 60 && minuteOfDay < settings.endHour * 60
}

/**
 * Calculates one alarm at a time so clock, time-zone and work-window changes are applied
 * before another reminder is queued. Android may deliver the inexact alarm later, never
 * earlier; the next alarm is therefore based on the actual delivery time.
 */
fun nextBreakReminderAt(
    nowMillis: Long,
    timeZone: TimeZone,
    rawSettings: BreakReminderSettings
): Long? {
    if (!rawSettings.enabled) return null
    val settings = rawSettings.sanitized()
    val candidate = Calendar.getInstance(timeZone).apply {
        timeInMillis = nowMillis
        add(Calendar.MINUTE, settings.intervalMinutes)
    }
    val candidateMinute = candidate.get(Calendar.HOUR_OF_DAY) * 60 + candidate.get(Calendar.MINUTE)

    if ((!settings.weekdaysOnly || !candidate.isWeekend()) &&
        candidateMinute >= settings.startHour * 60 && candidateMinute < settings.endHour * 60
    ) {
        return candidate.timeInMillis
    }

    if (candidateMinute >= settings.endHour * 60) candidate.add(Calendar.DAY_OF_MONTH, 1)
    candidate.apply {
        set(Calendar.HOUR_OF_DAY, settings.startHour)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    while (settings.weekdaysOnly && candidate.isWeekend()) {
        candidate.add(Calendar.DAY_OF_MONTH, 1)
    }
    return candidate.timeInMillis
}

private fun Calendar.isWeekend(): Boolean =
    get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY || get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY

/** The "20 seconds" of 20-20-20. */
const val BREAK_SECONDS = 20

/**
 * What the voice says at each tick. Zero ends on a word rather than "zero", so the last
 * thing a person hears is the break being over, not another number to wait through.
 */
fun spokenCount(secondsLeft: Int): String = when {
    secondsLeft <= 0 -> "Done. Back to what you were doing."
    else -> secondsLeft.toString()
}
