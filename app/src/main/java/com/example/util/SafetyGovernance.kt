package com.example.util

/**
 * Stage 13 & 14: Safety Governance, Regulatory Guardrails & Professional Validation Rules
 *
 * Enforces non-bypassable kinematic ceilings, red-flag triage detection, and strict non-diagnostic claims.
 */
object SafetyGovernance {

    // ==========================================
    // HARDWARE & KINEMATIC SAFETY LIMITS
    // ==========================================

    /** Upper velocity limit to prevent motion nausea, optokinetic fatigue or tracking-induced headache */
    const val MAX_ANGULAR_VELOCITY_DEG_PER_SEC: Float = 24.0f

    /** Default ergonomic tracking speed */
    const val DEFAULT_ANGULAR_VELOCITY_DEG_PER_SEC: Float = 14.0f

    /** Absolute minimum touch target size for motor interaction accessibility */
    const val MIN_TARGET_TOUCH_SIZE_DP: Int = 48

    /** Maximum continuous active eye exercise duration before mandatory rest */
    const val MAX_CONTINUOUS_TRAINING_SECONDS: Int = 300 // 5 minutes

    /** Strict zero-strobing mandate to eliminate photosensitive seizure risks */
    const val FLASHING_FREQUENCY_HZ: Float = 0.0f

    // ==========================================
    // RED-FLAG CLINICAL TRIAGE SYMPTOMS
    // ==========================================

    data class RedFlagSymptom(
        val title: String,
        val recommendation: String
    )

    val RED_FLAG_SYMPTOMS = listOf(
        RedFlagSymptom(
            title = "Sudden vision loss or darkening in one or both eyes",
            recommendation = "Immediate Emergency Care: Stop training immediately and seek urgent emergency medical or ophthalmologic evaluation."
        ),
        RedFlagSymptom(
            title = "Sudden onset of persistent double vision (diplopia)",
            recommendation = "Urgent Optometric/Ophthalmologic Consultation: Discontinue screen exercises and arrange professional evaluation."
        ),
        RedFlagSymptom(
            title = "Severe, deep, or throbbing eye pain",
            recommendation = "Urgent Medical Care: Deep ocular pain requires direct evaluation to rule out acute pathology."
        ),
        RedFlagSymptom(
            title = "Sudden shower of dark floaters or bright light flashes",
            recommendation = "Immediate Retinal Evaluation: Sudden flashes/floaters require prompt dilated retinal examination."
        ),
        RedFlagSymptom(
            title = "Persistent severe dizziness, nausea, or headache with visual aura",
            recommendation = "Neurological / General Physician Assessment: Cease ocular exercises and rest in a dim environment."
        )
    )

    // ==========================================
    // INTENDED USE & NON-DIAGNOSTIC STATEMENTS
    // ==========================================

    const val INTENDED_USE_LABEL: String = "Ergonomic Visual Break & Fatigue Pacing Tool"

    const val REGULATORY_CLASSIFICATION: String =
        "General Wellness Software (Non-Medical). Not intended to diagnose, treat, cure, or prevent any ocular disease, refractive error, or binocular vision disorder."

    const val PROFESSIONAL_REVIEW_STATUS: String =
        "Evidence-Informed Protocol (Stage 14 Review In Progress). For adult digital screen de-straining only."

    /**
     * Clamps orbital target speeds to the safe kinematic boundary.
     */
    fun clampSafeVelocity(requestedVelocity: Float): Float {
        return requestedVelocity.coerceIn(6.0f, MAX_ANGULAR_VELOCITY_DEG_PER_SEC)
    }
}
