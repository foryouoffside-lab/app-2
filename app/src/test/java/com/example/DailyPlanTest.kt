package com.example

import com.example.model.ClinicalContext
import com.example.model.DailyPlanRepository
import com.example.model.GeneralUserStatus
import com.example.model.Practice
import com.example.model.ScreenTimeBand
import com.example.model.VisionCorrection
import com.example.model.WellnessProfile
import com.example.model.WellnessSymptom
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DailyPlanTest {

    @Test
    fun `typical screen-use profile gets a guided set of about four minutes`() {
        val plan = DailyPlanRepository.forDay(
            profile(screenTime = ScreenTimeBand.FOUR_TO_EIGHT, symptoms = setOf(WellnessSymptom.TIRED_STRAIN))
        )

        assertEquals(
            listOf("complete_blink_squeeze", "screen_break_20_20_20", "palming"),
            plan.guided.map { it.drill.id }
        )
        // 90s trial-dosed blink practice + 20s break + 120s comfort rest = 3:50.
        assertEquals(230, plan.guided.sumOf { it.protocol.totalSeconds })
    }

    @Test
    fun `dry high-screen profile gets blink and distance-break core work`() {
        val plan = DailyPlanRepository.forDay(
            profile(
                screenTime = ScreenTimeBand.FOUR_TO_EIGHT,
                correction = VisionCorrection.CONTACTS,
                symptoms = setOf(WellnessSymptom.DRY_GRITTY)
            )
        )

        assertEquals(
            setOf("complete_blink_squeeze", "screen_break_20_20_20"),
            plan.guided.filter { it.isCore }.map { it.drill.id }.toSet()
        )
    }

    @Test
    fun `glasses change advice and do not invent an eye exercise`() {
        val plan = DailyPlanRepository.forDay(
            profile(screenTime = ScreenTimeBand.UNDER_TWO, correction = VisionCorrection.GLASSES)
        )

        assertTrue(plan.guided.isEmpty())
        assertEquals(listOf("working_distance_correction"), plan.habits.map { it.drill.id })
    }

    @Test
    fun `optional comfort drill is the same every day for the same profile`() {
        val input = profile(symptoms = setOf(WellnessSymptom.TENSION_HEADACHE))
        val first = DailyPlanRepository.forDay(input).guided.single { !it.isCore }.drill.id
        val second = DailyPlanRepository.forDay(input).guided.single { !it.isCore }.drill.id

        assertEquals("palming", first)
        assertEquals(first, second)
    }

    @Test
    fun `supporting habit is picked by clinical priority, not date`() {
        val input = profile(
            screenTime = ScreenTimeBand.FOUR_TO_EIGHT,
            correction = VisionCorrection.GLASSES,
            symptoms = setOf(WellnessSymptom.DRY_GRITTY)
        )
        val ids = (1..3).map { DailyPlanRepository.forDay(input).habits.single { !it.isCore }.drill.id }

        // Dryness outranks glasses/screen-setup, so the same habit wins every time.
        assertEquals(listOf("blink_awareness", "blink_awareness", "blink_awareness"), ids)
    }

    @Test
    fun `clinical diagnoses never unlock clinical drills automatically`() {
        val plan = DailyPlanRepository.forDay(
            profile(
                clinicalContexts = setOf(
                    ClinicalContext.CONVERGENCE_INSUFFICIENCY,
                    ClinicalContext.AMBLYOPIA_OR_EYE_TURN,
                    ClinicalContext.NEURO_RECOVERY
                )
            )
        )

        assertTrue(plan.guided.isEmpty())
        assertTrue(plan.safetyMessage!!.contains("not added automatically"))
        (plan.guided + plan.habits).forEach {
            assertEquals(GeneralUserStatus.GENERAL_TRAINING, it.drill.generalUserStatus)
        }
    }

    @Test
    fun `urgent answers and recent surgery pause all recommendations`() {
        listOf(
            profile(urgentSymptoms = true),
            profile(clinicalContexts = setOf(ClinicalContext.RECENT_SURGERY_OR_INJURY))
        ).forEach { input ->
            val plan = DailyPlanRepository.forDay(input)
            assertTrue(plan.isPausedForSafety)
            assertTrue(plan.guided.isEmpty())
            assertTrue(plan.habits.isEmpty())
        }
    }

    @Test
    fun `children get outdoor prevention as a habit not an eyesight drill`() {
        val plan = DailyPlanRepository.forDay(profile(age = 12))
        val outdoor = plan.habits.single { it.drill.id == "outdoor_daylight" }

        assertTrue(outdoor.isCore)
        assertEquals(Practice.HABIT, outdoor.drill.practice)
        assertTrue(outdoor.drill.unprovenClaims.contains("Reverses myopia"))
    }

    private fun profile(
        age: Int = 30,
        screenTime: ScreenTimeBand = ScreenTimeBand.UNDER_TWO,
        correction: VisionCorrection = VisionCorrection.NONE,
        symptoms: Set<WellnessSymptom> = emptySet(),
        clinicalContexts: Set<ClinicalContext> = emptySet(),
        urgentSymptoms: Boolean = false
    ) = WellnessProfile(age, screenTime, correction, symptoms, clinicalContexts, urgentSymptoms)
}
