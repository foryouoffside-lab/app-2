package com.example

import com.example.util.BreakReminderSettings
import com.example.util.nextBreakReminderAt
import com.example.util.isBreakReminderActiveAt
import java.util.Calendar
import java.util.TimeZone
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class BreakReminderScheduleTest {
    private val zone = TimeZone.getTimeZone("Asia/Kolkata")

    @Test
    fun `disabled schedule has no next reminder`() {
        assertNull(nextBreakReminderAt(at(2026, 9, 16, 10, 0), zone, settings(enabled = false)))
    }

    @Test
    fun `active workday keeps the selected interval`() {
        assertEquals(
            at(2026, 9, 16, 10, 20),
            nextBreakReminderAt(at(2026, 9, 16, 10, 0), zone, settings())
        )
    }

    @Test
    fun `before active hours waits for the opening time`() {
        assertEquals(
            at(2026, 9, 16, 9, 0),
            nextBreakReminderAt(at(2026, 9, 16, 7, 0), zone, settings())
        )
    }

    @Test
    fun `end of Friday skips to Monday when weekdays only`() {
        assertEquals(
            at(2026, 9, 21, 9, 0),
            nextBreakReminderAt(at(2026, 9, 18, 17, 50), zone, settings())
        )
    }

    @Test
    fun `weekend option allows Saturday reminders`() {
        assertEquals(
            at(2026, 9, 19, 10, 20),
            nextBreakReminderAt(
                at(2026, 9, 19, 10, 0),
                zone,
                settings().copy(weekdaysOnly = false)
            )
        )
    }

    @Test
    fun `alarm delayed beyond work hours is not active`() {
        assertEquals(false, isBreakReminderActiveAt(at(2026, 9, 16, 18, 1), zone, settings()))
        assertEquals(true, isBreakReminderActiveAt(at(2026, 9, 16, 17, 59), zone, settings()))
    }

    @Test
    fun `invalid stored values are repaired before scheduling`() {
        val invalid = BreakReminderSettings(
            enabled = true,
            intervalMinutes = 17,
            startHour = -5,
            endHour = 99
        )
        assertEquals(
            at(2026, 9, 16, 10, 20),
            nextBreakReminderAt(at(2026, 9, 16, 10, 0), zone, invalid)
        )
    }

    private fun settings(enabled: Boolean = true) = BreakReminderSettings(enabled = enabled)

    private fun at(year: Int, month: Int, day: Int, hour: Int, minute: Int): Long =
        Calendar.getInstance(zone).apply {
            clear()
            set(year, month - 1, day, hour, minute, 0)
        }.timeInMillis
}
