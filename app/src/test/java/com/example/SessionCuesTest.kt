package com.example

import com.example.model.CueKind
import com.example.model.ExercisePhase
import com.example.model.ExerciseType
import com.example.model.cueFor
import com.example.model.restCueFor
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SessionCuesTest {

    private fun phase(duration: Int) = ExercisePhase(
        title = "Horizontal Smooth Pursuit",
        instruction = "Follow the target.",
        durationSeconds = duration,
        type = ExerciseType.SMOOTH_PURSUIT,
        physiologicalBenefit = "Halfway there."
    )

    @Test
    fun `announces on entry and counts the last three seconds`() {
        val p = phase(20)
        assertEquals(CueKind.ANNOUNCE, cueFor(p, 20)!!.kind)
        assertNull(cueFor(p, 19))
        assertEquals(CueKind.MIDPOINT, cueFor(p, 10)!!.kind)
        assertNull(cueFor(p, 4))
        assertEquals("3", cueFor(p, 3)!!.text)
        assertEquals("1", cueFor(p, 1)!!.text)
        assertEquals(CueKind.END, cueFor(p, 0)!!.kind)
    }

    @Test
    fun `countdown wins over a midpoint that lands inside it`() {
        // duration 6 -> midpoint second 3 would collide with the spoken "3".
        val p = phase(6)
        assertEquals(CueKind.COUNT, cueFor(p, 3)!!.kind)
    }

    @Test
    fun `a phase shorter than the countdown is still announced on entry`() {
        val p = phase(2)
        assertEquals(CueKind.ANNOUNCE, cueFor(p, 2)!!.kind)
        assertEquals(CueKind.COUNT, cueFor(p, 1)!!.kind)
    }

    @Test
    fun `short phases get no midpoint line`() {
        val p = phase(10)
        assertNull(cueFor(p, 5))
    }

    @Test
    fun `rest announces then counts the last five seconds`() {
        assertEquals(CueKind.ANNOUNCE, restCueFor(15, "Palming", 15)!!.kind)
        assertNull(restCueFor(6, "Palming", 15))
        assertEquals("5", restCueFor(5, "Palming", 15)!!.text)
        assertEquals(CueKind.END, restCueFor(0, "Palming", 15)!!.kind)
    }
}
