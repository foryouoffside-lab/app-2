package com.example.model

import kotlin.math.ceil
import kotlin.math.pow
import kotlin.math.abs

/**
 * The two divisions, in the order they matter.
 *
 * A vision check can surface something worth showing a professional. A performance
 * challenge cannot: it scores a task on this screen and nothing else. Keeping them
 * apart is the only division the library needs, because it is the one that changes
 * what a result means.
 */
enum class ChallengeSection { VISION_CHECK, PERFORMANCE_CHALLENGE }

enum class ChallengeKind {
    NEAR_CLARITY,
    ASTIGMATISM_FAN,
    READING_CLARITY,
    RED_GREEN_BALANCE,
    CENTRAL_GRID,
    ISHIHARA_STYLE_PLATES,
    COVER_ALIGNMENT,
    NEAR_POINT_CONVERGENCE,
    CONTRAST_SPOTTING,
    COLOR_DISCRIMINATION,
    AMBLYOPIA_PLAY,
    PERIPHERAL_AWARENESS,
    VISUAL_REACTION
}

/**
 * One check or challenge in the library.
 *
 * [durationMinutes] is derived, never typed. It used to be a hand-written number that
 * drifted from what the runner actually presents: four tasks claimed one minute while
 * running eight to twelve scored rounds. Stating [steps] and [secondsPerStep] beside the
 * runner's real round count makes the quoted time follow the task instead.
 */
data class VisionChallenge(
    val id: String,
    val title: String,
    val description: String,
    val section: ChallengeSection,
    val kind: ChallengeKind,
    val measures: String,
    val evidenceLabel: String,
    /** Scored presentations the runner shows: plates, trials, or one per eye. */
    val steps: Int,
    /** Seconds one step takes in practice, including reading its prompt. */
    val secondsPerStep: Int,
    /** Seconds to read the setup screen and get into position before the first step. */
    val setupSeconds: Int,
    /**
     * The steps, in order, walked one at a time before the test starts.
     *
     * Empty for every check the runner can talk through on its own header text (most of
     * them). Only a check whose setup has to happen exactly right before the first
     * measurement means anything -- distance, which eye, which hand -- needs this instead
     * of a bulleted intro nobody reads twice.
     */
    val howTo: List<String> = emptyList()
) {
    val durationMinutes: Int = ceil((setupSeconds + steps * secondsPerStep) / 60.0).toInt()
}

/**
 * Evidence-grounded tasks that a phone can present without pretending to be an eye exam.
 *
 * Design sources:
 * - WHO Vision and eye screening implementation handbook (2024): screening must have a
 *   referral pathway and is distinct from a comprehensive eye examination.
 * - ISO 8596 / FDA K220090: acuity needs controlled distance, luminance and verified
 *   optotype sizing. This app therefore calls its near task a repeatable self-check, not
 *   a prescription or diagnosis.
 * - National Eye Institute AMD guidance: an Amsler grid is viewed at normal reading
 *   distance, with reading glasses if used, and with each eye tested separately.
 * - AOA adult examination guideline: confrontation/peripheral checks have useful
 *   specificity but limited sensitivity; our peripheral task is consequently a game only.
 *
 * Order within each section is by consequence, not by how the list happened to grow.
 * The grid comes first because a new central distortion is the one result here that can
 * mean a same-week appointment; the two needing a helper and a ruler come last because
 * nobody can run them alone.
 */
object VisionChallengeRepository {
    val challenges = listOf(
        VisionChallenge(
            id = "central_grid",
            title = "Central vision grid",
            description = "Check each eye for new waves, gaps or dark areas.",
            section = ChallengeSection.VISION_CHECK,
            kind = ChallengeKind.CENTRAL_GRID,
            measures = "Self-reported central distortion",
            evidenceLabel = "Amsler-style observation",
            steps = 2, secondsPerStep = 25, setupSeconds = 45,
            howTo = listOf(
                "Use your usual reading glasses, at your normal reading distance.",
                "We'll check your right eye first. Cover your left eye with your palm, without pressing on it.",
                "Keep looking at the red dot in the centre. Without chasing the lines, notice if any area looks wavy, missing or dark.",
                "Once your right eye is checked, cover it instead and repeat these same steps for your left eye."
            )
        ),
        VisionChallenge(
            id = "near_clarity",
            title = "Near clarity check",
            description = "Match the gap direction, one eye at a time.",
            section = ChallengeSection.VISION_CHECK,
            kind = ChallengeKind.NEAR_CLARITY,
            measures = "Repeatable near-detail threshold",
            evidenceLabel = "Screen-size dependent",
            // The staircase stops early once two of three are missed, so this is the
            // typical run rather than the 42-presentation ceiling.
            steps = 24, secondsPerStep = 3, setupSeconds = 75,
            howTo = listOf(
                "Sit somewhere with bright, even light, and hold the phone about 40 cm away — roughly a forearm's length from your nose.",
                "Match the line below to the short edge of a real bank card. This scales the test to your screen.",
                "We'll test your right eye first. Cover your left eye with your palm, without pressing on it.",
                "Keep your right eye open. A ring like this will appear, with a small gap on one side.",
                "Tap the arrow that matches where the gap is. Guess if you're unsure — don't skip it.",
                "Once your right eye is finished, cover it instead and repeat these same steps for your left eye."
            )
        ),
        VisionChallenge(
            id = "reading_clarity",
            title = "Reading clarity",
            description = "Find the smallest text that stays comfortable.",
            section = ChallengeSection.VISION_CHECK,
            kind = ChallengeKind.READING_CLARITY,
            measures = "Near-reading comfort on this device",
            evidenceLabel = "40 cm self-check",
            steps = 5, secondsPerStep = 6, setupSeconds = 25,
            howTo = listOf(
                "Use your normal reading glasses, or the reading zone of progressive lenses.",
                "Sit under bright, even light and hold the phone 40 cm away.",
                "Read from the largest line down. Tap the smallest line you can read without squinting."
            )
        ),
        VisionChallenge(
            id = "astigmatism_fan",
            title = "Astigmatism fan",
            description = "Compare radial line sharpness with each eye.",
            section = ChallengeSection.VISION_CHECK,
            kind = ChallengeKind.ASTIGMATISM_FAN,
            measures = "Self-reported directional blur",
            evidenceLabel = "Qualitative observation",
            steps = 2, secondsPerStep = 15, setupSeconds = 40,
            howTo = listOf(
                "Use your usual glasses, in bright, even light, at a comfortable distance.",
                "We'll check your right eye first. Cover your left eye with your palm, without pressing on it.",
                "Look at the centre of the fan. All lines are drawn equally — notice if any look darker or sharper than the rest.",
                "Once your right eye is checked, cover it instead and repeat these same steps for your left eye."
            )
        ),
        VisionChallenge(
            id = "ishihara_style_plates",
            title = "Ishihara-style colour plates",
            description = "Identify numbers formed by coloured dots.",
            section = ChallengeSection.VISION_CHECK,
            kind = ChallengeKind.ISHIHARA_STYLE_PLATES,
            measures = "Pseudoisochromatic plate responses",
            evidenceLabel = "Original, non-diagnostic plates",
            steps = 6, secondsPerStep = 10, setupSeconds = 45,
            howTo = listOf(
                "Turn off night mode, colour filters and any extra-dim display setting.",
                "Use neutral, daylight-like room lighting and your normal screen brightness.",
                "At reading distance, choose the number formed by the coloured dots — or choose \"No number\" if you can't see one."
            )
        ),
        VisionChallenge(
            id = "red_green_balance",
            title = "Red–green balance",
            description = "Compare identical marks on red and green.",
            section = ChallengeSection.VISION_CHECK,
            kind = ChallengeKind.RED_GREEN_BALANCE,
            measures = "Subjective red–green clarity balance",
            evidenceLabel = "Observation, not refraction",
            steps = 1, secondsPerStep = 20, setupSeconds = 35,
            howTo = listOf(
                "Use your usual near correction, in softly lit surroundings, and hold the phone 40 cm away.",
                "Compare the identical dark rings on the red and green halves.",
                "Tap whichever side's rings look darker or clearer — or say they look the same."
            )
        ),
        VisionChallenge(
            id = "cover_alignment",
            title = "Cover alignment observation",
            description = "A helper watches each uncovered eye for movement.",
            section = ChallengeSection.VISION_CHECK,
            kind = ChallengeKind.COVER_ALIGNMENT,
            measures = "Observed fixation movement",
            evidenceLabel = "Helper-assisted check",
            steps = 2, secondsPerStep = 25, setupSeconds = 60,
            howTo = listOf(
                "This check needs a helper to watch your eyes while you fixate on a target.",
                "Fixate on a small target about 33 cm away, in bright light.",
                "Your helper covers your left eye for 2 seconds, without pressing it, and watches only your right eye for movement.",
                "They repeat on the other side: cover your right eye and watch your left eye."
            )
        ),
        VisionChallenge(
            id = "near_point_convergence",
            title = "Near convergence log",
            description = "Record three ruler-assisted convergence break points.",
            section = ChallengeSection.VISION_CHECK,
            kind = ChallengeKind.NEAR_POINT_CONVERGENCE,
            measures = "Near-point distance baseline",
            evidenceLabel = "Helper and ruler required",
            steps = 3, secondsPerStep = 25, setupSeconds = 75,
            howTo = listOf(
                "Use your normal near correction, and have a helper and a centimetre ruler ready.",
                "Hold a detailed pen target about 50 cm away, then move it toward the bridge of your nose over about 10 seconds.",
                "Your helper measures the distance when it first looks double, or one eye visibly stops following.",
                "Enter that measured distance, then repeat for a total of three trials."
            )
        ),
        VisionChallenge(
            id = "contrast_spotting",
            title = "Contrast spotting",
            description = "Find the faint circle as contrast steps down.",
            section = ChallengeSection.PERFORMANCE_CHALLENGE,
            kind = ChallengeKind.CONTRAST_SPOTTING,
            measures = "On-device contrast performance",
            evidenceLabel = "Compare only on this device",
            steps = 10, secondsPerStep = 5, setupSeconds = 35,
            howTo = listOf(
                "Set a comfortable, fixed screen brightness before you start.",
                "Keep the phone at your usual reading distance.",
                "Tap the circle that looks slightly stronger — more solid — than the other three."
            )
        ),
        VisionChallenge(
            id = "peripheral_awareness",
            title = "Peripheral awareness",
            description = "Hold the centre while a brief edge cue appears.",
            section = ChallengeSection.PERFORMANCE_CHALLENGE,
            kind = ChallengeKind.PERIPHERAL_AWARENESS,
            measures = "Fixation and cue awareness",
            evidenceLabel = "Training challenge",
            steps = 8, secondsPerStep = 7, setupSeconds = 35,
            howTo = listOf(
                "Use both eyes and hold the phone at reading distance.",
                "Keep looking at the centre cross the whole time — do not chase the cue with your eyes.",
                "A brief dot will flash near one edge. After it disappears, tap the direction where it appeared."
            )
        ),
        VisionChallenge(
            id = "visual_reaction",
            title = "Visual reaction",
            description = "Tap each target as soon as it appears.",
            section = ChallengeSection.PERFORMANCE_CHALLENGE,
            kind = ChallengeKind.VISUAL_REACTION,
            measures = "Median tap response time",
            evidenceLabel = "Performance, not eye health",
            steps = 10, secondsPerStep = 4, setupSeconds = 35,
            howTo = listOf(
                "Rest the phone on a stable surface, or hold it steadily.",
                "Keep your attention near the centre of the screen.",
                "Tap the amber target the instant it appears, anywhere on the screen."
            )
        ),
        VisionChallenge(
            id = "color_discrimination",
            title = "Colour discrimination",
            description = "Find the tile with a slightly different hue.",
            section = ChallengeSection.PERFORMANCE_CHALLENGE,
            kind = ChallengeKind.COLOR_DISCRIMINATION,
            measures = "On-device hue discrimination",
            evidenceLabel = "Not a colour-blindness test",
            steps = 6, secondsPerStep = 5, setupSeconds = 35,
            howTo = listOf(
                "Use a comfortable fixed brightness, without a colour filter or night mode.",
                "Keep the phone at reading distance.",
                "Tap the circle whose hue looks different from the other three."
            )
        ),
        VisionChallenge(
            id = "amblyopia_play",
            title = "Lazy-eye target hunt",
            description = "Find matching symbols in a playful visual search.",
            section = ChallengeSection.PERFORMANCE_CHALLENGE,
            kind = ChallengeKind.AMBLYOPIA_PLAY,
            measures = "Close-up visual search accuracy",
            evidenceLabel = "Play activity, not treatment",
            steps = 12, secondsPerStep = 5, setupSeconds = 40,
            howTo = listOf(
                "Wear your prescribed glasses and use both eyes, unless a professional has prescribed patching for this activity.",
                "A target symbol appears at the top of the screen.",
                "Find and tap the same symbol in the grid below it."
            )
        )
    )
}

/** Optotype diameter at 40 cm for a five-arc-minute 0.0 logMAR symbol. */
fun optotypeMillimetresAt40Cm(logMar: Double): Double = 0.5818 * 10.0.pow(logMar)

fun nearClarityLabel(logMar: Double?): String = when (logMar) {
    null -> "Larger than 20/100"
    else -> when {
        logMar <= 0.01 -> "20/20 equivalent"
        logMar <= 0.11 -> "20/25 equivalent"
        logMar <= 0.21 -> "20/32 equivalent"
        logMar <= 0.31 -> "20/40 equivalent"
        logMar <= 0.41 -> "20/50 equivalent"
        logMar <= 0.51 -> "20/63 equivalent"
        else -> "20/100 equivalent"
    }
}

/** Two 0.1-logMAR steps is the practical flag; a one-step difference is common retest noise. */
fun acuityEyesDiffer(rightLogMar: Double?, leftLogMar: Double?): Boolean = when {
    rightLogMar == null && leftLogMar == null -> false
    rightLogMar == null || leftLogMar == null -> true
    else -> abs(rightLogMar - leftLogMar) >= 0.19
}

/**
 * What today's two thresholds are actually worth doing about, in the order that matters:
 * a missing result first, then a gap between the eyes, then how small the smallest gap
 * found was, and only then reassurance. Never a diagnosis -- only what to try next.
 */
fun nearClarityRecommendation(rightLogMar: Double?, leftLogMar: Double?): String = when {
    rightLogMar == null && leftLogMar == null ->
        "No threshold was recorded for either eye. Repeat in brighter, even light, and check the on-screen line still matches a bank card's edge."
    acuityEyesDiffer(rightLogMar, leftLogMar) ->
        "One eye read smaller gaps than the other today. A single step of difference is common retest noise, but a gap that repeats is worth mentioning at a comprehensive eye exam."
    listOfNotNull(rightLogMar, leftLogMar).max() > 0.21 ->
        "Both eyes matched, but the smallest gap either found was larger than a 20/32 equivalent. If near tasks already feel effortful, a comprehensive eye exam can check whether your correction needs updating."
    else ->
        "Both eyes found a similar, comfortably small gap today. Keep using your usual correction and repeat this occasionally under the same light and distance."
}

fun medianMillis(samples: List<Long>): Long? {
    if (samples.isEmpty()) return null
    val sorted = samples.sorted()
    val middle = sorted.size / 2
    return if (sorted.size % 2 == 1) sorted[middle]
    else (sorted[middle - 1] + sorted[middle]) / 2
}

fun accuracyPercent(correct: Int, total: Int): Int =
    if (total <= 0) 0 else ((correct * 100f) / total).toInt().coerceIn(0, 100)
