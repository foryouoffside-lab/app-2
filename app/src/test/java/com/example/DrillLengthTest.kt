package com.example

import com.example.model.ProtocolsRepository
import com.example.ui.drill.MAX_REPS
import com.example.ui.drill.MIN_REPS
import com.example.ui.drill.formatLength
import com.example.ui.drill.repLabel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class DrillLengthTest {

    @Test
    fun `prebuilt sets are not user adjustable`() {
        // Their doses come from the trial protocols; editing them would break that.
        ProtocolsRepository.forAge(30).forEach {
            assertFalse("${it.id} must stay fixed", it.userAdjustable)
        }
    }

    @Test
    fun `rep bounds are sane`() {
        assertEquals(5, MIN_REPS)
        assertEquals(30, MAX_REPS)
    }

    @Test
    fun `a drill row reads as reps, or as a clock when it is one held interval`() {
        assertEquals("15 Reps", repLabel(15, 6))
        assertEquals("20 Reps", repLabel(20, 4))
        // 20-20-20 is a single 20-second look-away, not "1 Rep".
        assertEquals("00:20", repLabel(1, 20))
        assertEquals("01:30", repLabel(1, 90))
    }

    @Test
    fun `length reads as seconds under a minute and minutes above`() {
        assertEquals("30s", formatLength(30))
        assertEquals("59s", formatLength(59))
        assertEquals("1m", formatLength(60))
        assertEquals("1m 30s", formatLength(90))
        assertEquals("2m", formatLength(120))
        assertEquals("2m 30s", formatLength(150))
    }
}
