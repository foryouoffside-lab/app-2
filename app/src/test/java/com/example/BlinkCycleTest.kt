package com.example

import com.example.ui.drill.BlinkStage
import com.example.ui.drill.blinkCycleState
import com.example.ui.drill.blinkStageFor
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BlinkCycleTest {

    @Test
    fun `cycle runs close then squeeze then open`() {
        // Start of the cycle: eye wide, no squeeze.
        val (openStart, squeezeStart) = blinkCycleState(0f)
        assertEquals(1f, openStart, 0.01f)
        assertEquals(0f, squeezeStart, 0.01f)

        // End of the closing third: shut.
        val (openClosed, _) = blinkCycleState(0.33f)
        assertTrue("lid should be nearly shut", openClosed < 0.05f)

        // Middle of the squeeze third: shut and squeezing hardest.
        val (openSqueeze, squeezePeak) = blinkCycleState(0.5f)
        assertEquals(0f, openSqueeze, 0.01f)
        assertEquals(1f, squeezePeak, 0.02f)

        // End of the opening third: wide again, squeeze released.
        val (openEnd, squeezeEnd) = blinkCycleState(0.99f)
        assertTrue("lid should be reopening", openEnd > 0.9f)
        assertEquals(0f, squeezeEnd, 0.01f)
    }

    @Test
    fun `cycle wraps so a long drill never jumps`() {
        val (a, b) = blinkCycleState(0.25f)
        val (c, d) = blinkCycleState(3.25f)
        assertEquals(a, c, 0.001f)
        assertEquals(b, d, 0.001f)
    }

    @Test
    fun `negative progress is handled rather than throwing`() {
        val (open, _) = blinkCycleState(-0.5f)
        assertTrue(open in 0f..1f)
    }

    @Test
    fun `cue never says close while the eye is opening`() {
        assertEquals(BlinkStage.CLOSE, blinkStageFor(0.1f))
        assertEquals(BlinkStage.SQUEEZE, blinkStageFor(0.5f))
        // Early in the opening third the lid is still mostly shut, but the cue must
        // already say Open or it contradicts the animation.
        assertEquals(BlinkStage.OPEN, blinkStageFor(0.70f))
        assertEquals(BlinkStage.OPEN, blinkStageFor(0.99f))
    }
}
