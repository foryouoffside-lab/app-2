package com.example.model

/**
 * What a target has to do for the eye.
 *
 * Drills are grouped by this rather than tuned one by one. Every pursuit drill wants the
 * same thing from its target -- a mark the eye can lock onto that never changes speed
 * abruptly -- so "how a pursuit target behaves" is decided once here and inherited by all
 * of them. Fixing a target then fixes it everywhere it is used.
 */
enum class TargetFunction {
    /** Hold still and be looked at. */
    FIXATE,

    /** Move continuously, slowly enough that the eye can stay locked on. */
    PURSUE,

    /** Sit still, then jump, so the eye makes a discrete flick rather than a drift. */
    SACCADE,

    /** Move in depth, driving the two eyes together or apart. */
    VERGE,

    /** Present as a pair the two eyes have to combine into one. */
    FUSE,

    /** Appear at scattered positions to be found in order. */
    SCAN,

    /** No target at all -- the drill is not about looking at something on screen. */
    NONE
}

/**
 * How a target of a given [TargetFunction] should be drawn and moved.
 *
 * [radius] is a fraction of the canvas's shorter edge, so a target keeps its proportions
 * from a 56dp list thumbnail to a full-screen landscape drill.
 */
data class TargetSpec(
    val radius: Float,
    /**
     * Whether the target eases through direction reversals.
     *
     * A pure triangle wave reverses at infinite acceleration, which no eye can track --
     * pursuit breaks down into catch-up saccades at the turn. Saccadic drills deliberately
     * do NOT ease: the jump is the point.
     */
    val easeTurns: Boolean,
    /** Fraction of each cycle the target holds still before it moves. */
    val dwell: Float
)

/** The one place a target's behaviour is decided, for every drill that asks the same of it. */
fun specFor(function: TargetFunction): TargetSpec = when (function) {
    // Small and static: a fixation target wants a precise point to hold, not a big disc.
    TargetFunction.FIXATE -> TargetSpec(radius = 0.040f, easeTurns = false, dwell = 1f)

    // Continuous and eased, so velocity never steps.
    TargetFunction.PURSUE -> TargetSpec(radius = 0.052f, easeTurns = true, dwell = 0f)

    // Hold, then flick. The dwell is what makes it a saccade rather than a slow drift.
    TargetFunction.SACCADE -> TargetSpec(radius = 0.052f, easeTurns = false, dwell = 0.42f)

    // Depth changes have to be gradual or the eyes lose fusion and the image doubles.
    TargetFunction.VERGE -> TargetSpec(radius = 0.058f, easeTurns = true, dwell = 0.12f)

    // Big enough to hold detail in both eyes, with a pause at the extremes to fuse.
    TargetFunction.FUSE -> TargetSpec(radius = 0.062f, easeTurns = true, dwell = 0.18f)

    // Small, so finding it is the task rather than seeing it.
    TargetFunction.SCAN -> TargetSpec(radius = 0.038f, easeTurns = false, dwell = 0.30f)

    TargetFunction.NONE -> TargetSpec(radius = 0.050f, easeTurns = true, dwell = 0f)
}

/** Which target behaviour a stimulus needs. */
val StudioStimulus.targetFunction: TargetFunction
    get() = when (this) {
        StudioStimulus.FIXATION -> TargetFunction.FIXATE

        StudioStimulus.PURSUIT,
        StudioStimulus.HEMIFIELD_PURSUIT,
        StudioStimulus.PURSUIT_CIRCULAR,
        StudioStimulus.PURSUIT_FIGURE_EIGHT -> TargetFunction.PURSUE

        StudioStimulus.SACCADE,
        StudioStimulus.SACCADE_VERTICAL,
        StudioStimulus.ANTI_SACCADE -> TargetFunction.SACCADE

        StudioStimulus.NEAR_TARGET,
        StudioStimulus.BROCK_STRING,
        StudioStimulus.FOCUS_SHIFT,
        StudioStimulus.VERGENCE_STEP -> TargetFunction.VERGE

        StudioStimulus.DISPARITY,
        StudioStimulus.DICHOPTIC,
        StudioStimulus.STEREOGRAM,
        StudioStimulus.APERTURE -> TargetFunction.FUSE

        StudioStimulus.SCANNING -> TargetFunction.SCAN

        // Hold, then move to the next extreme -- the same shape as a saccade, just with
        // eight stops instead of two.
        StudioStimulus.EYE_ROM -> TargetFunction.SACCADE

        // Nothing on screen is being looked at: the eyes are shut or covered, and the
        // canvas shows the step rather than a target.
        StudioStimulus.BLINK,
        StudioStimulus.BREAK_REMINDER,
        StudioStimulus.PALMING,
        StudioStimulus.WARM_COMPRESS,
        StudioStimulus.ACUPRESSURE -> TargetFunction.NONE
    }

/**
 * Whether the drill is worth turning the phone for.
 *
 * A target that travels left to right has a portrait screen's short edge to work with,
 * which is where a pursuit sweep or a saccade jump gets cramped: the same drill on the
 * long edge asks for a real eye movement rather than a flick across 6cm. Drills that stay
 * put, or that the user does with the eyes off the screen, gain nothing from it and must
 * not be told to rotate.
 */
val StudioStimulus.prefersLandscape: Boolean
    get() = when (this) {
        StudioStimulus.PURSUIT,
        StudioStimulus.HEMIFIELD_PURSUIT,
        StudioStimulus.PURSUIT_CIRCULAR,
        StudioStimulus.PURSUIT_FIGURE_EIGHT,
        StudioStimulus.SACCADE,
        StudioStimulus.ANTI_SACCADE,
        StudioStimulus.SCANNING,
        StudioStimulus.NEAR_TARGET,
        StudioStimulus.VERGENCE_STEP -> true
        else -> false
    }

/**
 * Motion applied to a target over one cycle, honouring its spec.
 *
 * Returns 0..1 along whatever path the stimulus draws. The dwell is spent at the start,
 * so a saccadic target is genuinely stationary before it moves rather than creeping.
 */
fun targetProgress(spec: TargetSpec, cycle: Float): Float {
    val t = ((cycle % 1f) + 1f) % 1f
    // Out on the first half of the moving part, back on the second.
    val moving = if (spec.dwell >= 1f) 0f else ((t - spec.dwell) / (1f - spec.dwell)).coerceIn(0f, 1f)
    val swept = if (moving < .5f) moving * 2f else 2f - moving * 2f
    return if (spec.easeTurns) smoothStep(swept) else swept
}

/** Ease in and out, so a reversal has finite acceleration. */
private fun smoothStep(x: Float): Float = x * x * (3f - 2f * x)
