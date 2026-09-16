package com.example.model

import java.util.Calendar
import java.util.TimeZone

/** Daily screen exposure is a selector for break habits, not a diagnosis. */
enum class ScreenTimeBand(val label: String) {
    UNDER_TWO("Under 2 hours"),
    TWO_TO_FOUR("2-4 hours"),
    FOUR_TO_EIGHT("4-8 hours"),
    OVER_EIGHT("More than 8 hours");

    val sustained: Boolean get() = this != UNDER_TWO
    val high: Boolean get() = this == FOUR_TO_EIGHT || this == OVER_EIGHT
}

enum class VisionCorrection(val label: String) {
    NONE("None"),
    GLASSES("Glasses"),
    CONTACTS("Contact lenses"),
    GLASSES_AND_CONTACTS("Both")
}

enum class WellnessSymptom(val label: String) {
    DRY_GRITTY("Dry, gritty or burning"),
    TIRED_STRAIN("Tired or strained after near work"),
    TENSION_HEADACHE("Ache around the eyes or headache"),
    FOCUS_BLUR("Blur when switching near and far"),
    DOUBLE_VISION("Double vision or words splitting")
}

/**
 * These answers restrict an automatic plan. They do not unlock condition-specific
 * exercises: those stay in Training Studio for use under an eye-care professional.
 */
enum class ClinicalContext(val label: String) {
    DRY_EYE_OR_LID_DISEASE("Dry eye, blepharitis or blocked lid glands"),
    CONVERGENCE_INSUFFICIENCY("Convergence insufficiency"),
    AMBLYOPIA_OR_EYE_TURN("Amblyopia, lazy eye or an eye turn"),
    NEURO_RECOVERY("Concussion, stroke or other brain-injury recovery"),
    RECENT_SURGERY_OR_INJURY("Recent eye surgery or eye injury")
}

data class WellnessProfile(
    val age: Int,
    val screenTime: ScreenTimeBand,
    val correction: VisionCorrection,
    val symptoms: Set<WellnessSymptom>,
    val clinicalContexts: Set<ClinicalContext>,
    /**
     * Sudden vision change/loss, new flashes or curtain, severe pain, a painful red eye,
     * or sudden double vision. A yes answer pauses all automatic drills.
     */
    val urgentSymptoms: Boolean
)

data class DailyRecommendation(
    val drill: StudioDrill,
    val reason: String,
    /** Core recommendations may repeat because the studied regimen itself repeats. */
    val isCore: Boolean
) {
    val protocol: Protocol
        get() = drill.toProtocol().copy(
            description = reason,
            tag = if (isCore) "Core for you" else "Rotates daily"
        )
}

data class DailyPlan(
    val guided: List<DailyRecommendation>,
    val habits: List<DailyRecommendation>,
    val basedOn: List<String>,
    val safetyMessage: String? = null,
    val isPausedForSafety: Boolean = false
)

/** A stable local-calendar key so a plan changes at local midnight, not at UTC midnight. */
fun localDayKey(
    timeMillis: Long = System.currentTimeMillis(),
    timeZone: TimeZone = TimeZone.getDefault()
): Int = Calendar.getInstance(timeZone).run {
    this.timeInMillis = timeMillis
    get(Calendar.YEAR) * 400 + get(Calendar.DAY_OF_YEAR)
}

/**
 * Builds a conservative comfort plan from the user's answers.
 *
 * Selection rules follow the claims already attached to the Studio records:
 * - dry-eye symptoms select the 2025 blink-training dose (PMID 40467388);
 * - sustained screen work selects a real-world distance break (PMID 35963776);
 * - diagnosed lid disease may add warming/lid care (TFOS DEWS II, PMID 28736343);
 * - glasses change the correction/ergonomics advice, not the eye exercise;
 * - age gates child outdoor guidance and excludes self-directed clinical therapy;
 * - condition-specific/neuro/amblyopia drills are never automatically prescribed.
 *
 * Core items are allowed to repeat when their evidence-based regimen calls for it. Only
 * optional comfort work and one non-core daily habit rotate, so variety never displaces
 * the intervention selected for the person's actual complaint.
 */
object DailyPlanRepository {

    fun forDay(profile: WellnessProfile, dayKey: Int = localDayKey()): DailyPlan {
        val basedOn = buildList {
            add(profile.screenTime.label.lowercase() + " of screen time")
            if (profile.symptoms.isEmpty()) add("no regular symptoms reported")
            else add(profile.symptoms.joinToString { it.label.lowercase() })
            if (profile.correction != VisionCorrection.NONE) add(profile.correction.label.lowercase())
            add("age ${profile.age}")
        }

        if (profile.urgentSymptoms) {
            return DailyPlan(
                guided = emptyList(),
                habits = emptyList(),
                basedOn = basedOn,
                safetyMessage = "Do not start eye drills. Sudden vision change, new flashes or a curtain, severe eye pain, a painful red eye, or sudden double vision needs urgent medical assessment.",
                isPausedForSafety = true
            )
        }

        if (ClinicalContext.RECENT_SURGERY_OR_INJURY in profile.clinicalContexts) {
            return DailyPlan(
                guided = emptyList(),
                habits = emptyList(),
                basedOn = basedOn,
                safetyMessage = "Your automatic plan is paused after recent eye surgery or injury. Follow your eye-care team's aftercare instructions before doing any drill.",
                isPausedForSafety = true
            )
        }

        if (WellnessSymptom.DOUBLE_VISION in profile.symptoms) {
            return DailyPlan(
                guided = emptyList(),
                habits = emptyList(),
                basedOn = basedOn,
                safetyMessage = "Double vision should be assessed before training. The app will not choose convergence or eye-teaming exercises from a symptom answer alone.",
                isPausedForSafety = true
            )
        }

        val guided = mutableListOf<DailyRecommendation>()
        val dryNeed = WellnessSymptom.DRY_GRITTY in profile.symptoms ||
            ClinicalContext.DRY_EYE_OR_LID_DISEASE in profile.clinicalContexts
        val screenNeed = profile.screenTime.sustained ||
            WellnessSymptom.TIRED_STRAIN in profile.symptoms ||
            WellnessSymptom.FOCUS_BLUR in profile.symptoms

        if (dryNeed) addGuided(
            guided,
            "complete_blink_squeeze",
            if (profile.correction.usesContacts) {
                "Chosen for reported dryness; contact-lens wear is a secondary ocular-surface factor, not a diagnosis."
            } else {
                "Chosen for reported dryness and incomplete blinking during concentrated screen work."
            },
            isCore = true
        )
        if (screenNeed) addGuided(
            guided,
            "screen_break_20_20_20",
            "Chosen to interrupt sustained near viewing; look at a real distant target.",
            isCore = true
        )

        val optionalIds = buildList {
            if (WellnessSymptom.TENSION_HEADACHE in profile.symptoms) {
                add("palming")
                add("eye_range_of_motion")
            }
            if (ClinicalContext.DRY_EYE_OR_LID_DISEASE in profile.clinicalContexts) {
                add("warm_compress")
            }
        }
        pick(optionalIds, dayKey)?.let { id ->
            addGuided(
                guided,
                id,
                if (id == "warm_compress") {
                    "Rotating lid-care support because you reported diagnosed dry-eye or lid disease."
                } else {
                    "Today's optional comfort variation for tension; evidence is for short-term comfort, not stronger eyesight."
                },
                isCore = false
            )
        }

        val coreHabits = mutableListOf<DailyRecommendation>()
        if (profile.age < 18) addHabit(
            coreHabits,
            "outdoor_daylight",
            "Daily outdoor daylight is the age-relevant prevention habit; it does not reverse myopia.",
            isCore = true
        )
        if (ClinicalContext.DRY_EYE_OR_LID_DISEASE in profile.clinicalContexts) addHabit(
            coreHabits,
            "lid_hygiene",
            "Daily lid care is included only because you reported a diagnosed lid-surface condition.",
            isCore = true
        )

        val rotatingHabitIds = buildList {
            if (screenNeed) add("screen_setup")
            if (dryNeed) add("blink_awareness")
            if (profile.correction.usesGlasses || WellnessSymptom.FOCUS_BLUR in profile.symptoms) {
                add("working_distance_correction")
            }
        }
        pickNonCoreHabit(rotatingHabitIds, dayKey + 1)?.let { id ->
            addHabit(
                coreHabits,
                id,
                when (id) {
                    "screen_setup" -> "Today's rotating focus: reduce glare, leaning and unnecessarily close viewing."
                    "blink_awareness" -> "Today's rotating focus: carry complete blinks into real screen work."
                    else -> "Today's rotating focus: make sure correction matches the distance you actually use."
                },
                isCore = false
            )
        }

        val restrictedContext = profile.clinicalContexts.any {
            it == ClinicalContext.CONVERGENCE_INSUFFICIENCY ||
                it == ClinicalContext.AMBLYOPIA_OR_EYE_TURN ||
                it == ClinicalContext.NEURO_RECOVERY
        }
        val safety = when {
            restrictedContext -> "Condition-specific exercises were not added automatically. Use only the plan given by your eye-care or rehabilitation professional."
            WellnessSymptom.FOCUS_BLUR in profile.symptoms -> "Recurring blur when switching distance can come from an uncorrected prescription or binocular/focus problem. Arrange an eye examination if it persists."
            else -> null
        }

        return DailyPlan(
            guided = guided.take(3),
            habits = coreHabits,
            basedOn = basedOn,
            safetyMessage = safety
        )
    }

    private val VisionCorrection.usesContacts: Boolean
        get() = this == VisionCorrection.CONTACTS || this == VisionCorrection.GLASSES_AND_CONTACTS

    private val VisionCorrection.usesGlasses: Boolean
        get() = this == VisionCorrection.GLASSES || this == VisionCorrection.GLASSES_AND_CONTACTS

    private fun pick(ids: List<String>, dayKey: Int): String? =
        ids.takeIf { it.isNotEmpty() }?.let { it[Math.floorMod(dayKey, it.size)] }

    /**
     * A one-item pool is not honestly "rotating", so it appears only every third day.
     * Two items alternate, then take a rest day. Three or more use each option before
     * any can recur.
     */
    private fun pickNonCoreHabit(ids: List<String>, dayKey: Int): String? {
        if (ids.isEmpty()) return null
        val cycleLength = maxOf(3, ids.size)
        val slot = Math.floorMod(dayKey, cycleLength)
        return ids.getOrNull(slot)
    }

    private fun addGuided(
        destination: MutableList<DailyRecommendation>,
        id: String,
        reason: String,
        isCore: Boolean
    ) {
        StudioDrillRepository.byId(id)?.let { drill ->
            if (drill.practice == Practice.GUIDED &&
                drill.generalUserStatus == GeneralUserStatus.GENERAL_TRAINING
            ) destination += DailyRecommendation(drill, reason, isCore)
        }
    }

    private fun addHabit(
        destination: MutableList<DailyRecommendation>,
        id: String,
        reason: String,
        isCore: Boolean
    ) {
        StudioDrillRepository.byId(id)?.let { drill ->
            if (drill.practice == Practice.HABIT &&
                drill.generalUserStatus == GeneralUserStatus.GENERAL_TRAINING
            ) destination += DailyRecommendation(drill, reason, isCore)
        }
    }
}
