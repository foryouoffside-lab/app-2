package com.example

import com.example.model.AgeBand
import com.example.model.DrillBlock
import com.example.model.ProtocolsRepository
import com.example.model.ageBandFor
import com.example.model.minAmplitudeDiopters
import com.example.model.nearTargetCm
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AgeAdaptationTest {

    @Test
    fun `Hofstetter minimum amplitude matches the clinical formula`() {
        assertEquals(10.0, minAmplitudeDiopters(20), 0.001)  // 15 - 0.25*20
        assertEquals(5.0, minAmplitudeDiopters(40), 0.001)
        assertEquals(0.0, minAmplitudeDiopters(60), 0.001)
        // Never negative, however old the user says they are.
        assertEquals(0.0, minAmplitudeDiopters(95), 0.001)
    }

    @Test
    fun `near target moves further out with age`() {
        val young = nearTargetCm(20)!!
        val middle = nearTargetCm(45)!!
        assertEquals(20, young)
        assertTrue("near target should recede with age", middle > young)
    }

    @Test
    fun `near far drill is dropped once accommodation is gone`() {
        assertNotNull(nearTargetCm(45))
        assertNull("a 56-year-old cannot hold an unaided near target", nearTargetCm(56))
    }

    @Test
    fun `age bands split at the clinical boundaries`() {
        assertEquals(AgeBand.YOUTH, ageBandFor(14))
        assertEquals(AgeBand.ADULT, ageBandFor(30))
        assertEquals(AgeBand.EARLY_PRESBYOPIC, ageBandFor(45))
        assertEquals(AgeBand.PRESBYOPIC, ageBandFor(60))
    }

    @Test
    fun `every set opens on a warm up and closes on a cool down`() {
        for (age in listOf(16, 30, 45, 62)) {
            for (protocol in ProtocolsRepository.forAge(age)) {
                val blocks = protocol.phases.map { it.block }
                assertEquals(
                    "${protocol.id} at age $age should not start mid-session",
                    blocks.sortedBy { it.ordinal },
                    blocks
                )
                assertEquals(
                    "${protocol.id} total must match its phases",
                    protocol.phases.sumOf { it.durationSeconds },
                    protocol.totalSeconds
                )
            }
        }
    }

    @Test
    fun `the blink drill keeps the trial dose of fifteen cycles`() {
        val set = ProtocolsRepository.forAge(30).single()
        val blink = set.phases.first { it.block == DrillBlock.CORE }
        // 15 repeats of a 6-second close-squeeze-open cycle.
        assertEquals(90, blink.durationSeconds)
    }
}
