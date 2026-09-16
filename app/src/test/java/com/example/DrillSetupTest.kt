package com.example

import com.example.model.GeneralUserStatus
import com.example.model.Practice
import com.example.model.StudioDrillRepository
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * A drill that needs something the phone cannot supply -- a warm flannel, a pencil, a
 * window twenty feet away -- has to say what to fetch and what to do with it before the
 * timer starts. Without it the player drops the user straight into a four-minute compress
 * with no idea what to heat, how warm it should be, or which way to sweep.
 */
class DrillSetupTest {

    /** The drills a general user is actually offered and can actually run. */
    private val runnable = StudioDrillRepository.drills.filter {
        it.practice == Practice.GUIDED &&
            it.generalUserStatus in setOf(
                GeneralUserStatus.GENERAL_TRAINING,
                GeneralUserStatus.GENERAL_DEMONSTRATION
            )
    }

    @Test
    fun `a runnable drill that needs equipment walks the user through it`() {
        val needsKit = runnable.filterNot { it.equipment.startsWith("None", ignoreCase = true) }
        assertTrue("nothing in the library needs equipment, so this guard is dead", needsKit.isNotEmpty())
        needsKit.forEach { drill ->
            assertTrue(
                "${drill.id} needs '${drill.equipment}' but never says how to get or use it",
                drill.howTo.isNotEmpty()
            )
        }
    }

    @Test
    fun `every walkthrough step is one spoken instruction, not a paragraph`() {
        StudioDrillRepository.drills.forEach { drill ->
            drill.howTo.forEachIndexed { i, step ->
                // Spoken aloud one at a time and shown alone on the screen. Past roughly
                // this length it overruns the step card and outlasts the user's patience.
                assertTrue(
                    "${drill.id} step ${i + 1} is ${step.length} chars, too long to speak as one step",
                    step.length in 20..260
                )
            }
        }
    }

    @Test
    fun `the warm compress says what to heat, how warm, and which way to sweep`() {
        val steps = StudioDrillRepository.byId("warm_compress")!!.howTo.joinToString(" ").lowercase()
        listOf("flannel", "mask", "palms", "wrist", "reheat", "lashes").forEach {
            assertTrue("the warm compress walkthrough never mentions '$it'", it in steps)
        }
    }
}
