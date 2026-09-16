package com.example

import com.example.model.StudioDrillRepository
import com.example.model.StudioStimulus
import com.example.model.TargetFunction
import com.example.model.DrillGuidance
import com.example.model.guidance
import com.example.model.prefersLandscape
import com.example.model.targetFunction
import com.example.util.DEFAULT_TARGET_SPEED
import com.example.util.TARGET_SPEED_RANGE
import com.example.util.targetSpeedLabel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Two things the drill player asks of a stimulus before it changes what the user sees:
 * whether turning the phone is worth suggesting, and how fast the target should travel.
 *
 * Getting the first wrong is worse than it sounds -- telling someone to rotate for a drill
 * they do with their eyes shut is advice that cannot possibly help, and it arrives spoken.
 */
class TargetPresentationTest {

    @Test
    fun `only a drill that moves something on screen asks the user to rotate`() {
        StudioStimulus.entries.filter { it.prefersLandscape }.forEach { stimulus ->
            assertTrue(
                "$stimulus asks for the long edge but has no moving target",
                stimulus.targetFunction != TargetFunction.NONE
            )
        }
    }

    @Test
    fun `a drill done off the screen is never told to turn the phone`() {
        StudioDrillRepository.drills
            .filter { it.guidance == DrillGuidance.NON_VISUAL }
            .forEach { drill ->
                assertFalse(
                    "${drill.id} is done off the screen, so rotating it cannot help",
                    drill.stimulus.prefersLandscape
                )
            }
    }

    @Test
    fun `the still targets stay put`() {
        // Fixation and the fusion pair sit in the middle of the screen: the long edge
        // gives them nothing, and the prompt would just be noise.
        listOf(
            StudioStimulus.FIXATION,
            StudioStimulus.STEREOGRAM,
            StudioStimulus.APERTURE,
            StudioStimulus.SACCADE_VERTICAL
        ).forEach { assertFalse("$it does not need the long edge", it.prefersLandscape) }
    }

    @Test
    fun `the speed bar reads back what it is set to`() {
        assertTrue(DEFAULT_TARGET_SPEED in TARGET_SPEED_RANGE)
        assertEquals("1.0×", targetSpeedLabel(DEFAULT_TARGET_SPEED))
        assertEquals("0.4×", targetSpeedLabel(TARGET_SPEED_RANGE.start))
        assertEquals("2.0×", targetSpeedLabel(TARGET_SPEED_RANGE.endInclusive))
        // A slider hands back whatever the finger lands on; the label must not round it
        // into a lie about a speed the target is not travelling at.
        assertEquals("1.3×", targetSpeedLabel(1.25f))
    }
}
