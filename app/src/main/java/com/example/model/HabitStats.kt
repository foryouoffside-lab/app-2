package com.example.model

import com.example.data.SessionLog
import java.util.TimeZone
import kotlin.math.floor

private const val DAY_MILLIS = 86_400_000L
val CARE_MILESTONES = listOf(1, 3, 7, 14, 30, 60, 100, 180, 365)
const val WEEKLY_CARE_GOAL = 5

data class CareDay(
    val dayOrdinal: Long,
    val sessions: Int,
    val seconds: Int,
    val isToday: Boolean
) {
    val completed: Boolean get() = sessions > 0
}

data class HabitStats(
    val todayComplete: Boolean,
    /** Still alive before today's deadline when the last completion was yesterday. */
    val currentStreak: Int,
    val longestStreak: Int,
    val totalCareDays: Int,
    val activeDaysLastSeven: Int,
    val lastSevenDays: List<CareDay>,
    val nextMilestone: Int?
) {
    val weeklyGoalReached: Boolean get() = activeDaysLastSeven >= WEEKLY_CARE_GOAL
}

/**
 * Converts an instant to a contiguous local-day number without java.time (the app
 * supports Android 7). Applying the offset before division preserves local midnight and
 * remains stable across time-zone and daylight-saving boundaries.
 */
fun localDayOrdinal(timeMillis: Long, timeZone: TimeZone = TimeZone.getDefault()): Long =
    floor((timeMillis + timeZone.getOffset(timeMillis)).toDouble() / DAY_MILLIS).toLong()

fun calculateHabitStats(
    logs: List<SessionLog>,
    nowMillis: Long = System.currentTimeMillis(),
    timeZone: TimeZone = TimeZone.getDefault()
): HabitStats {
    val today = localDayOrdinal(nowMillis, timeZone)
    val byDay = logs.groupBy { localDayOrdinal(it.timestamp, timeZone) }
    val activeDays = byDay.keys.sorted()
    val activeSet = activeDays.toSet()

    val streakEnd = when {
        today in activeSet -> today
        today - 1 in activeSet -> today - 1
        else -> null
    }
    var currentStreak = 0
    var cursor = streakEnd
    while (cursor != null && cursor in activeSet) {
        currentStreak++
        cursor--
    }

    var longestStreak = 0
    var run = 0
    var previous: Long? = null
    activeDays.forEach { day ->
        run = if (previous != null && day == previous!! + 1) run + 1 else 1
        longestStreak = maxOf(longestStreak, run)
        previous = day
    }

    val lastSeven = (6 downTo 0).map { daysAgo ->
        val ordinal = today - daysAgo
        val dayLogs = byDay[ordinal].orEmpty()
        CareDay(
            dayOrdinal = ordinal,
            sessions = dayLogs.size,
            seconds = dayLogs.sumOf { it.durationSeconds },
            isToday = ordinal == today
        )
    }

    return HabitStats(
        todayComplete = today in activeSet,
        currentStreak = currentStreak,
        longestStreak = longestStreak,
        totalCareDays = activeDays.size,
        activeDaysLastSeven = lastSeven.count { it.completed },
        lastSevenDays = lastSeven,
        nextMilestone = CARE_MILESTONES.firstOrNull { it > longestStreak }
    )
}
