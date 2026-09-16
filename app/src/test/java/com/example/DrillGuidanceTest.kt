package com.example

import com.example.model.DrillGuidance
import com.example.model.StudioDrillRepository
import com.example.model.StudioStimulus
import com.example.model.guidance
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Guidance decides whether a drill talks while it runs. Getting it backwards is the worst
 * kind of bug here: a watched drill that natters over the target, or a drill done with the
 * eyes shut that goes completely silent and leaves the user with no idea what to do.
 */
class DrillGuidanceTest {

    @Test
    fun `drills done with the eyes off the screen are guided without it`() {
        // Blinking shuts the eyes, the break sends them 20 feet away, the
        // accommodative rock alternates to a real far target, and palming and the warm
        // compress are both done with the eyes closed.
        listOf("complete_blink_squeeze", "screen_break_20_20_20", "accommodative_rock", "palming", "warm_compress").forEach { id ->
            val drill = StudioDrillRepository.byId(id)!!
            assertEquals("$id must be guided without the screen", DrillGuidance.NON_VISUAL, drill.guidance)
        }
    }

    @Test
    fun `drills the user watches stay visual`() {
        listOf("horizontal_saccades", "smooth_pursuit", "fixation_stability", "brock_string").forEach { id ->
            val drill = StudioDrillRepository.byId(id)!!
            assertEquals("$id is watched, so it must not talk over itself", DrillGuidance.VISUAL, drill.guidance)
        }
    }

    @Test
    fun `guidance follows the stimulus for every drill, with no gaps`() {
        val offScreen = setOf(
            StudioStimulus.BLINK, StudioStimulus.BREAK_REMINDER, StudioStimulus.FOCUS_SHIFT,
            StudioStimulus.PALMING, StudioStimulus.WARM_COMPRESS, StudioStimulus.ACUPRESSURE
        )
        StudioDrillRepository.drills.forEach { drill ->
            val expected =
                if (drill.stimulus in offScreen) DrillGuidance.NON_VISUAL else DrillGuidance.VISUAL
            assertEquals(drill.id, expected, drill.guidance)
        }
        // Both branches have to be reachable, or the split is doing nothing.
        val byGuidance = StudioDrillRepository.drills.groupBy { it.guidance }
        assertTrue(byGuidance[DrillGuidance.NON_VISUAL].orEmpty().isNotEmpty())
        assertTrue(byGuidance[DrillGuidance.VISUAL].orEmpty().isNotEmpty())
    }
}
