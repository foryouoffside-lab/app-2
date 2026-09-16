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
    val setupSeconds: Int
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
            steps = 2, secondsPerStep = 25, setupSeconds = 45
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
            steps = 24, secondsPerStep = 3, setupSeconds = 75
        ),
        VisionChallenge(
            id = "reading_clarity",
            title = "Reading clarity",
            description = "Find the smallest text that stays comfortable.",
            section = ChallengeSection.VISION_CHECK,
            kind = ChallengeKind.READING_CLARITY,
            measures = "Near-reading comfort on this device",
            evidenceLabel = "40 cm self-check",
            steps = 5, secondsPerStep = 6, setupSeconds = 25
        ),
        VisionChallenge(
            id = "astigmatism_fan",
            title = "Astigmatism fan",
            description = "Compare radial line sharpness with each eye.",
            section = ChallengeSection.VISION_CHECK,
            kind = ChallengeKind.ASTIGMATISM_FAN,
            measures = "Self-reported directional blur",
            evidenceLabel = "Qualitative observation",
            steps = 2, secondsPerStep = 15, setupSeconds = 40
        ),
        VisionChallenge(
            id = "ishihara_style_plates",
            title = "Ishihara-style colour plates",
            description = "Identify numbers formed by coloured dots.",
            section = ChallengeSection.VISION_CHECK,
            kind = ChallengeKind.ISHIHARA_STYLE_PLATES,
            measures = "Pseudoisochromatic plate responses",
            evidenceLabel = "Original, non-diagnostic plates",
            steps = 6, secondsPerStep = 10, setupSeconds = 45
        ),
        VisionChallenge(
            id = "red_green_balance",
            title = "Red–green balance",
            description = "Compare identical marks on red and green.",
            section = ChallengeSection.VISION_CHECK,
            kind = ChallengeKind.RED_GREEN_BALANCE,
            measures = "Subjective red–green clarity balance",
            evidenceLabel = "Observation, not refraction",
            steps = 1, secondsPerStep = 20, setupSeconds = 35
        ),
        VisionChallenge(
            id = "cover_alignment",
            title = "Cover alignment observation",
            description = "A helper watches each uncovered eye for movement.",
            section = ChallengeSection.VISION_CHECK,
            kind = ChallengeKind.COVER_ALIGNMENT,
            measures = "Observed fixation movement",
            evidenceLabel = "Helper-assisted check",
            steps = 2, secondsPerStep = 25, setupSeconds = 60
        ),
        VisionChallenge(
            id = "near_point_convergence",
            title = "Near convergence log",
            description = "Record three ruler-assisted convergence break points.",
            section = ChallengeSection.VISION_CHECK,
            kind = ChallengeKind.NEAR_POINT_CONVERGENCE,
            measures = "Near-point distance baseline",
            evidenceLabel = "Helper and ruler required",
            steps = 3, secondsPerStep = 25, setupSeconds = 75
        ),
        VisionChallenge(
            id = "contrast_spotting",
            title = "Contrast spotting",
            description = "Find the faint circle as contrast steps down.",
            section = ChallengeSection.PERFORMANCE_CHALLENGE,
            kind = ChallengeKind.CONTRAST_SPOTTING,
            measures = "On-device contrast performance",
            evidenceLabel = "Compare only on this device",
            steps = 10, secondsPerStep = 5, setupSeconds = 35
        ),
        VisionChallenge(
            id = "peripheral_awareness",
            title = "Peripheral awareness",
            description = "Hold the centre while a brief edge cue appears.",
            section = ChallengeSection.PERFORMANCE_CHALLENGE,
            kind = ChallengeKind.PERIPHERAL_AWARENESS,
            measures = "Fixation and cue awareness",
            evidenceLabel = "Training challenge",
            steps = 8, secondsPerStep = 7, setupSeconds = 35
        ),
        VisionChallenge(
            id = "visual_reaction",
            title = "Visual reaction",
            description = "Tap each target as soon as it appears.",
            section = ChallengeSection.PERFORMANCE_CHALLENGE,
            kind = ChallengeKind.VISUAL_REACTION,
            measures = "Median tap response time",
            evidenceLabel = "Performance, not eye health",
            steps = 10, secondsPerStep = 4, setupSeconds = 35
        ),
        VisionChallenge(
            id = "color_discrimination",
            title = "Colour discrimination",
            description = "Find the tile with a slightly different hue.",
            section = ChallengeSection.PERFORMANCE_CHALLENGE,
            kind = ChallengeKind.COLOR_DISCRIMINATION,
            measures = "On-device hue discrimination",
            evidenceLabel = "Not a colour-blindness test",
            steps = 6, secondsPerStep = 5, setupSeconds = 35
        ),
        VisionChallenge(
            id = "amblyopia_play",
            title = "Lazy-eye target hunt",
            description = "Find matching symbols in a playful visual search.",
            section = ChallengeSection.PERFORMANCE_CHALLENGE,
            kind = ChallengeKind.AMBLYOPIA_PLAY,
            measures = "Close-up visual search accuracy",
            evidenceLabel = "Play activity, not treatment",
            steps = 12, secondsPerStep = 5, setupSeconds = 40
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

fun medianMillis(samples: List<Long>): Long? {
    if (samples.isEmpty()) return null
    val sorted = samples.sorted()
    val middle = sorted.size / 2
    return if (sorted.size % 2 == 1) sorted[middle]
    else (sorted[middle - 1] + sorted[middle]) / 2
}

fun accuracyPercent(correct: Int, total: Int): Int =
    if (total <= 0) 0 else ((correct * 100f) / total).toInt().coerceIn(0, 100)
