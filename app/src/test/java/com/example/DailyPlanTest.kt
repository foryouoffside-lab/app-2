package com.example

import com.example.model.ClinicalContext
import com.example.model.DailyPlanRepository
import com.example.model.ExerciseTimeBand
import com.example.model.GeneralUserStatus
import com.example.model.Practice
import com.example.model.ScreenTimeBand
import com.example.model.VisionCorrection
import com.example.model.WellnessProfile
import com.example.model.WellnessSymptom
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DailyPlanTest {

    @Test
    fun `available time limits drill count without changing evidence doses`() {
        val symptoms = setOf(
            WellnessSymptom.DRY_GRITTY,
            WellnessSymptom.TIRED_STRAIN,
            WellnessSymptom.TENSION_HEADACHE
        )
        val short = DailyPlanRepository.forDay(
            profile(symptoms = symptoms, exerciseTime = ExerciseTimeBand.FIVE_TO_TEN),
            dayKey = 100
        )
        val long = DailyPlanRepository.forDay(
            profile(symptoms = symptoms, exerciseTime = ExerciseTimeBand.FIFTEEN_TO_TWENTY),
            dayKey = 100
        )

        assertEquals(3, short.guided.size)
        assertEquals(3, long.guided.size)
        assertEquals(short.guided.first().protocol.totalSeconds, long.guided.first().protocol.totalSeconds)
    }

    @Test
    fun `dry high-screen profile gets blink and distance-break core work`() {
        val plan = DailyPlanRepository.forDay(
            profile(
                screenTime = ScreenTimeBand.FOUR_TO_EIGHT,
                correction = VisionCorrection.CONTACTS,
                symptoms = setOf(WellnessSymptom.DRY_GRITTY)
            ),
            dayKey = 100
        )

        assertEquals(
            setOf("complete_blink_squeeze", "screen_break_20_20_20"),
            plan.guided.filter { it.isCore }.map { it.drill.id }.toSet()
        )
    }

    @Test
    fun `glasses change advice and do not invent an eye exercise`() {
        val plan = DailyPlanRepository.forDay(
            profile(screenTime = ScreenTimeBand.UNDER_TWO, correction = VisionCorrection.GLASSES),
            dayKey = 101
        )

        assertTrue(plan.guided.isEmpty())
        assertEquals(listOf("working_distance_correction"), plan.habits.map { it.drill.id })
    }

    @Test
    fun `optional comfort drill rotates on consecutive days`() {
        val input = profile(symptoms = setOf(WellnessSymptom.TENSION_HEADACHE))
        val first = DailyPlanRepository.forDay(input, 200).guided.single().drill.id
        val second = DailyPlanRepository.forDay(input, 201).guided.single().drill.id

        assertTrue(first in setOf("palming", "eye_range_of_motion"))
        assertTrue(second in setOf("palming", "eye_range_of_motion"))
        assertFalse("optional work should not repeat on adjacent days", first == second)
    }

    @Test
    fun `rotating habit does not repeat while alternatives are available`() {
        val input = profile(
            screenTime = ScreenTimeBand.FOUR_TO_EIGHT,
            correction = VisionCorrection.GLASSES,
            symptoms = setOf(WellnessSymptom.DRY_GRITTY)
        )
        val ids = (300..302).map { day ->
            DailyPlanRepository.forDay(input, day).habits.single { !it.isCore }.drill.id
        }

        assertEquals(3, ids.toSet().size)
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
            ),
            dayKey = 100
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
            val plan = DailyPlanRepository.forDay(input, 100)
            assertTrue(plan.isPausedForSafety)
            assertTrue(plan.guided.isEmpty())
            assertTrue(plan.habits.isEmpty())
        }
    }

    @Test
    fun `children get outdoor prevention as a habit not an eyesight drill`() {
        val plan = DailyPlanRepository.forDay(profile(age = 12), dayKey = 100)
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
        urgentSymptoms: Boolean = false,
        exerciseTime: ExerciseTimeBand = ExerciseTimeBand.FIFTEEN_TO_TWENTY
    ) = WellnessProfile(age, screenTime, correction, symptoms, clinicalContexts, urgentSymptoms, exerciseTime)
}
