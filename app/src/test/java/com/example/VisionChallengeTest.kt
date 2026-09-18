package com.example

import com.example.model.ChallengeSection
import com.example.model.VisionChallengeRepository
import com.example.model.accuracyPercent
import com.example.model.acuityEyesDiffer
import com.example.model.medianMillis
import com.example.model.nearClarityLabel
import com.example.model.nearClarityRecommendation
import com.example.model.optotypeMillimetresAt40Cm
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class VisionChallengeTest {

    @Test
    fun `catalog has unique ids and both honest sections`() {
        val items = VisionChallengeRepository.challenges
        assertEquals(13, items.size)
        assertEquals(items.size, items.map { it.id }.toSet().size)
        assertTrue(items.any { it.section == ChallengeSection.VISION_CHECK })
        assertTrue(items.any { it.section == ChallengeSection.PERFORMANCE_CHALLENGE })
    }

    @Test
    fun `quoted duration is derived from the steps the runner presents`() {
        // The four game durations used to be typed by hand and said "1 min" while running
        // eight to twelve scored rounds. Deriving them means the only way to get the time
        // wrong now is to state the wrong step count next to the runner.
        VisionChallengeRepository.challenges.forEach {
            assertTrue("${it.id}: a challenge with no steps runs nothing", it.steps > 0)
            assertTrue("${it.id}: a step takes some time", it.secondsPerStep > 0)
            val seconds = it.setupSeconds + it.steps * it.secondsPerStep
            assertEquals("${it.id}: quoted minutes must cover the task", (seconds + 59) / 60, it.durationMinutes)
        }
        // The three runners that count rounds off the model, spot-checked so a silent
        // edit to one cannot shorten the task and the quoted time together.
        assertEquals(12, VisionChallengeRepository.challenges.single { it.id == "amblyopia_play" }.steps)
        assertEquals(8, VisionChallengeRepository.challenges.single { it.id == "peripheral_awareness" }.steps)
        assertEquals(10, VisionChallengeRepository.challenges.single { it.id == "visual_reaction" }.steps)
    }

    @Test
    fun `the section that can surface something comes first`() {
        // A vision check can send somebody to a professional; a game cannot. The library
        // renders in list order, so that division has to hold in the data.
        val sections = VisionChallengeRepository.challenges.map { it.section }
        assertEquals(sections.sortedBy { it.ordinal }, sections)
    }

    @Test
    fun `acuity sizing follows logMAR progression`() {
        assertTrue(optotypeMillimetresAt40Cm(0.5) > optotypeMillimetresAt40Cm(0.0))
        assertEquals("20/20 equivalent", nearClarityLabel(0.0))
        assertEquals("20/40 equivalent", nearClarityLabel(0.3))
        assertEquals("Larger than 20/100", nearClarityLabel(null))
    }

    @Test
    fun `challenge scores use robust median and bounded percent`() {
        assertEquals(280L, medianMillis(listOf(900, 260, 300, 280, 250)))
        assertEquals(75, accuracyPercent(6, 8))
        assertEquals(0, accuracyPercent(1, 0))
    }

    @Test
    fun `eye comparison ignores one line of retest noise`() {
        assertTrue(!acuityEyesDiffer(0.2, 0.1))
        assertTrue(acuityEyesDiffer(0.3, 0.1))
        assertTrue(acuityEyesDiffer(null, 0.7))
    }

    @Test
    fun `near clarity walkthrough is one instruction per step, not a paragraph`() {
        val steps = VisionChallengeRepository.challenges.single { it.id == "near_clarity" }.howTo
        assertTrue("near_clarity needs a one-eye-at-a-time walkthrough", steps.isNotEmpty())
        assertTrue("the walkthrough never says to cover an eye", steps.any { "cover" in it.lowercase() })
        steps.forEachIndexed { i, step ->
            assertTrue(
                "near_clarity step ${i + 1} is ${step.length} chars, too long for one screen",
                step.length in 20..260
            )
        }
    }

    @Test
    fun `near clarity walkthrough tests the right eye before the left, same order as the runner`() {
        // The runner's first round is eye = 0, headed "RIGHT EYE", scored by covering the
        // left. The walkthrough has to say that in the same order or it teaches the
        // opposite of what the round it leads into actually does.
        val steps = VisionChallengeRepository.challenges.single { it.id == "near_clarity" }.howTo
        val rightFirst = steps.indexOfFirst { "right eye" in it.lowercase() }
        val leftSecond = steps.indexOfFirst { "left eye" in it.lowercase() && "cover your left eye" in it.lowercase() }
        assertTrue("no step names the right eye", rightFirst >= 0)
        assertTrue("no step hands the test over to the left eye", steps.any { "left eye" in it.lowercase() })
        assertTrue("the right eye must be introduced before the left eye takes over", rightFirst < steps.lastIndex)
        assertEquals("naming the right eye and covering the left to test it belong in the same step", rightFirst, leftSecond)
    }

    @Test
    fun `near clarity feedback names the actual thing to do next`() {
        assertTrue("no threshold" in nearClarityRecommendation(null, null).lowercase())
        assertTrue("exam" in nearClarityRecommendation(0.3, 0.1).lowercase())
        assertTrue("exam" in nearClarityRecommendation(0.3, 0.3).lowercase())
        assertTrue("usual correction" in nearClarityRecommendation(0.0, 0.1).lowercase())
    }
}
