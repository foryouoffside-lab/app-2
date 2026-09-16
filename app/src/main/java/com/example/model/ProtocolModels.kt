package com.example.model

import kotlin.math.roundToInt

/**
 * Drill set design notes (sources are listed against each rule).
 *
 * What the evidence actually supports, and what it does not:
 *
 *  - Blink training reduces dry-eye symptoms, incomplete blinks and conjunctival
 *    staining. The optimised routine is close 2s / squeeze 2s / open 2s, 15 repeats,
 *    three times a day for two weeks (Contact Lens & Anterior Eye, 2025). A smartphone
 *    cue every ~5s prompting a forceful double blink improved tear break-up time and
 *    OSDI over 30 days (npj Digital Medicine, 2025).
 *  - Vergence/accommodative therapy improves near point of convergence and positive
 *    fusional vergence in diagnosed convergence insufficiency (CITT). Note CITT-ART
 *    found symptom relief no better than placebo, so we claim comfort practice, never
 *    treatment.
 *  - The 20-20-20 rule has mixed evidence: it eases some symptoms but a 2-week trial
 *    found no change in accommodative posture, vergence or dry-eye signs.
 *  - No eye exercise corrects refractive error. Nothing here may imply otherwise.
 *
 * Ordering follows standard vision-therapy sequencing (amplitude and awareness first,
 * then facility, then binocular/dynamic work) wrapped in the warm-up / core / cool-down
 * arc used by the sports-vision RCT that reduced digital eye strain.
 */

enum class ExerciseType {
    SMOOTH_PURSUIT,
    SACCADE_JUMP,
    RAPID_BLINK,
    PALMING_BREATH,
    ACCOMMODATION_SHIFT,
    PERIPHERAL_DETECTION,
    VISUAL_SEARCH
}

/** Where a drill sits in the session arc. */
enum class DrillBlock { WARM_UP, CORE, COOL_DOWN }

data class ExercisePhase(
    val title: String,
    /** One short imperative line. Keep it glanceable — it is read mid-drill. */
    val instruction: String,
    val durationSeconds: Int,
    val type: ExerciseType,
    val eyesClosed: Boolean = false,
    val physiologicalBenefit: String,
    val block: DrillBlock = DrillBlock.CORE
)

data class Protocol(
    val id: String,
    val title: String,
    val tag: String,
    val totalSeconds: Int,
    val description: String,
    val phases: List<ExercisePhase>,
    val targetSymptom: String,
    /**
     * Whether the user may set the length before starting.
     *
     * Prebuilt sets are fixed: their doses come from the trial protocols and letting
     * them be edited would quietly break that. Single drills picked from Train are the
     * user's own call, so those are adjustable.
     */
    val userAdjustable: Boolean = false,
    /**
     * Id of the [StudioDrill] holding this drill's evidence record, or null when the
     * drill has none. The player shows an evidence button only when it is set.
     */
    val evidenceId: String? = null
)

// ---------------------------------------------------------------------------
// Age model
// ---------------------------------------------------------------------------

/**
 * Hofstetter's minimum expected amplitude of accommodation, in dioptres.
 *
 * Still the standard clinical estimator, though later work finds it optimistic at the
 * young end; using the *minimum* line rather than the average keeps the drill inside
 * what a typical user of that age can actually do.
 */
fun minAmplitudeDiopters(age: Int): Double = (15.0 - 0.25 * age).coerceAtLeast(0.0)

/**
 * Near-target distance in cm for focus drills, or null when accommodation has declined
 * far enough that a near-far drill is no longer meaningful unaided.
 *
 * Sustained near work is kept inside about half the available amplitude, so the drill
 * asks for A/2 dioptres rather than the full amplitude. A 20-year-old gets ~20cm; a
 * 45-year-old ~50cm; past the mid-fifties the drill is dropped rather than set up to fail.
 */
fun nearTargetCm(age: Int): Int? {
    val halfAmplitude = minAmplitudeDiopters(age) / 2.0
    if (halfAmplitude < 1.0) return null
    return (100.0 / halfAmplitude).roundToInt()
}

enum class AgeBand {
    /** Under 18: breaks and comfort only; therapy belongs with a clinician. */
    YOUTH,

    /** Full accommodative range available. */
    ADULT,

    /** Amplitude falling; near targets move out. */
    EARLY_PRESBYOPIC,

    /** Little usable unaided accommodation; distance rest and blink work instead. */
    PRESBYOPIC
}

fun ageBandFor(age: Int): AgeBand = when {
    age < 18 -> AgeBand.YOUTH
    age < 40 -> AgeBand.ADULT
    age < 55 -> AgeBand.EARLY_PRESBYOPIC
    else -> AgeBand.PRESBYOPIC
}

// ---------------------------------------------------------------------------
// Drills
// ---------------------------------------------------------------------------

private fun blinkDrill(seconds: Int, block: DrillBlock) = ExercisePhase(
    title = "Blink Reset",
    // The squeeze step is the part that carried the symptom benefit in the 2025 trial.
    instruction = "Close 2s · squeeze 2s · open",
    durationSeconds = seconds,
    type = ExerciseType.RAPID_BLINK,
    physiologicalBenefit = "Restores the tear film after low-blink screen time.",
    block = block
)

// ---------------------------------------------------------------------------
// Sets
// ---------------------------------------------------------------------------

object ProtocolsRepository {

    /**
     * One drill while the drill environment is being designed.
     *
     * The age maths above is kept because it still decides near-target distance, but the
     * old fixed sets are gone: the set is being rebuilt as user-customisable drills.
     */
    val defaultProtocols: List<Protocol> get() = forAge(30)

    fun forAge(age: Int): List<Protocol> = listOf(blinkSet())

    /**
     * 15 close-squeeze-open cycles at 6s each, the dose the 2025 trial landed on.
     *
     * Read off the evidence record rather than written here twice, so correcting the
     * trial dose in one place corrects the drill everywhere it is offered.
     */
    private fun blinkSet(): Protocol {
        val dose = StudioDrillRepository.byId("complete_blink_squeeze")!!.dose
        val phases = listOf(blinkDrill(dose.totalSeconds, DrillBlock.CORE))
        return Protocol(
            id = "blink_reset",
            title = "Blink Reset",
            tag = "${dose.reps} reps",
            totalSeconds = phases.sumOf { it.durationSeconds },
            description = "Close, squeeze, open.",
            phases = phases,
            targetSymptom = "Dryness",
            evidenceId = "complete_blink_squeeze"
        )
    }
}
