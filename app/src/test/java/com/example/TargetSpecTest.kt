package com.example

import com.example.model.StudioDrillRepository
import com.example.model.StudioStimulus
import com.example.model.TargetFunction
import com.example.model.specFor
import com.example.model.targetFunction
import com.example.model.targetProgress
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Targets are grouped by what they ask of the eye, so that drills asking the same thing
 * behave the same. These guard the two properties that grouping is for: the grouping
 * itself, and the motion each group produces.
 */
class TargetSpecTest {

    @Test
    fun `a saccadic target holds still before it moves`() {
        val spec = specFor(TargetFunction.SACCADE)
        assertTrue("a saccade needs a dwell or it is just a slow drift", spec.dwell > 0.2f)
        // Nothing moves during the dwell.
        assertEquals(0f, targetProgress(spec, 0f), 0.001f)
        assertEquals(0f, targetProgress(spec, spec.dwell * 0.9f), 0.001f)
        // Then it does.
        assertTrue(targetProgress(spec, 0.7f) > 0.5f)
    }

    @Test
    fun `a pursuit target never reverses at infinite acceleration`() {
        val spec = specFor(TargetFunction.PURSUE)
        assertTrue("pursuit must ease its turns", spec.easeTurns)

        // Near the turnaround the step between successive frames has to shrink, or the
        // eye cannot stay locked on and the drill degrades into catch-up saccades.
        fun stepAt(t: Float) =
            kotlin.math.abs(targetProgress(spec, t + 0.001f) - targetProgress(spec, t))

        assertTrue("velocity must fall off at the turn", stepAt(0.499f) < stepAt(0.25f))
        assertTrue("velocity must fall off at the start", stepAt(0.001f) < stepAt(0.25f))
    }

    @Test
    fun `a fixation target does not move at all`() {
        val spec = specFor(TargetFunction.FIXATE)
        listOf(0f, 0.25f, 0.5f, 0.75f, 0.99f).forEach {
            assertEquals("fixation must stay put at $it", 0f, targetProgress(spec, it), 0.001f)
        }
    }

    @Test
    fun `progress stays in range for any phase, including wrapped and negative`() {
        TargetFunction.entries.forEach { function ->
            val spec = specFor(function)
            listOf(-2f, -0.3f, 0f, 0.5f, 1f, 3.7f).forEach { t ->
                val p = targetProgress(spec, t)
                assertTrue("$function at $t produced $p", p in 0f..1f)
            }
        }
    }

    @Test
    fun `every stimulus maps to a target function, and pursuit differs from saccade`() {
        StudioStimulus.entries.forEach { stimulus ->
            // Exhaustive `when`, so this only fails if a new stimulus is added without
            // deciding what its target has to do.
            stimulus.targetFunction
        }
        assertNotEquals(
            specFor(TargetFunction.PURSUE).dwell,
            specFor(TargetFunction.SACCADE).dwell
        )
        assertEquals(TargetFunction.PURSUE, StudioStimulus.PURSUIT_CIRCULAR.targetFunction)
        assertEquals(TargetFunction.SACCADE, StudioStimulus.ANTI_SACCADE.targetFunction)
        assertEquals(TargetFunction.NONE, StudioStimulus.BLINK.targetFunction)
    }

    @Test
    fun `drills sharing a target function share its spec`() {
        val bySpec = StudioDrillRepository.drills.groupBy { specFor(it.stimulus.targetFunction) }
        // The whole point of the grouping: pursuit drills are one entry, not four.
        val pursuit = StudioDrillRepository.drills
            .filter { it.stimulus.targetFunction == TargetFunction.PURSUE }
        assertTrue("expected several pursuit drills", pursuit.size >= 3)
        assertEquals(1, pursuit.map { specFor(it.stimulus.targetFunction) }.distinct().size)
        assertTrue(bySpec.isNotEmpty())
    }
}
