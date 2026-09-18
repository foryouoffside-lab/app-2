package com.example

import com.example.data.SessionLog
import com.example.model.calculateHabitStats
import com.example.model.localDayOrdinal
import java.util.Calendar
import java.util.TimeZone
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HabitStatsTest {
    private val zone = TimeZone.getTimeZone("Asia/Kolkata")
    private val now = at(2026, 9, 16, 12)

    @Test
    fun `many sessions on one day earn only one care day`() {
        val stats = calculateHabitStats(
            listOf(log(at(2026, 9, 16, 9)), log(at(2026, 9, 16, 11))),
            now,
            zone
        )

        assertTrue(stats.todayComplete)
        assertEquals(1, stats.currentStreak)
        assertEquals(1, stats.totalCareDays)
        assertEquals(2, stats.lastSevenDays.last().sessions)
    }

    @Test
    fun `streak stays alive during an unfinished today`() {
        val stats = calculateHabitStats(
            listOf(log(at(2026, 9, 14, 9)), log(at(2026, 9, 15, 9))),
            now,
            zone
        )

        assertFalse(stats.todayComplete)
        assertEquals(2, stats.currentStreak)
    }

    @Test
    fun `missed yesterday resets current streak but preserves best`() {
        val stats = calculateHabitStats(
            listOf(
                log(at(2026, 9, 10, 9)),
                log(at(2026, 9, 11, 9)),
                log(at(2026, 9, 12, 9)),
                log(at(2026, 9, 16, 9))
            ),
            now,
            zone
        )

        assertEquals(1, stats.currentStreak)
        assertEquals(3, stats.longestStreak)
        assertEquals(7, stats.nextMilestone)
    }

    @Test
    fun `weekly rhythm allows two rest days`() {
        val logs = (12..16).map { day -> log(at(2026, 9, day, 9)) }
        val stats = calculateHabitStats(logs, now, zone)

        assertEquals(5, stats.activeDaysLastSeven)
        assertTrue(stats.weeklyGoalReached)
    }

    @Test
    fun `local day ordinal remains consecutive across new year`() {
        val dec31 = localDayOrdinal(at(2026, 12, 31, 9), zone)
        val jan1 = localDayOrdinal(at(2027, 1, 1, 9), zone)
        assertEquals(dec31 + 1, jan1)
    }

    private fun log(timestamp: Long) = SessionLog(
        protocolId = "blink",
        protocolTitle = "Blink Reset",
        durationSeconds = 60,
        timestamp = timestamp
    )

    private fun at(year: Int, month: Int, day: Int, hour: Int): Long =
        Calendar.getInstance(zone).apply {
            clear()
            set(year, month - 1, day, hour, 0, 0)
        }.timeInMillis
}
