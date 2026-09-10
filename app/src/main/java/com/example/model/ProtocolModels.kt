package com.example.model

enum class ExerciseType {
    SMOOTH_PURSUIT,
    SACCADE_JUMP,
    RAPID_BLINK,
    PALMING_BREATH,
    ACCOMMODATION_SHIFT
}

data class ExercisePhase(
    val title: String,
    val instruction: String,
    val durationSeconds: Int,
    val type: ExerciseType,
    val eyesClosed: Boolean = false,
    val physiologicalBenefit: String
)

data class Protocol(
    val id: String,
    val title: String,
    val tag: String,
    val totalSeconds: Int,
    val description: String,
    val phases: List<ExercisePhase>,
    val targetSymptom: String
)

object ProtocolsRepository {
    val defaultProtocols: List<Protocol> = listOf(
        Protocol(
            id = "instant_screen_reset",
            title = "Instant Screen Reset",
            tag = "Most Popular",
            totalSeconds = 60,
            description = "Rapid 60-second focal release breaking ciliary muscle spasm and re-wetting dry cornea.",
            targetSymptom = "Screen Fatigue & Distance Blur",
            phases = listOf(
                ExercisePhase(
                    title = "Accommodative Release",
                    instruction = "Gaze at an object 20 feet away. Allow your focal muscles to completely relax.",
                    durationSeconds = 20,
                    type = ExerciseType.ACCOMMODATION_SHIFT,
                    physiologicalBenefit = "Relaxes the ciliary muscle ring contracted by near-distance monitors."
                ),
                ExercisePhase(
                    title = "Full Conscious Blinks",
                    instruction = "Blink gently and fully to clear the surface. Squeeze gently for half a second.",
                    durationSeconds = 15,
                    type = ExerciseType.RAPID_BLINK,
                    physiologicalBenefit = "Forces meibomian glands to express lipid oil over tear film."
                ),
                ExercisePhase(
                    title = "Horizontal Smooth Pursuit",
                    instruction = "Follow the smooth moving target softly across your field of view without turning your head.",
                    durationSeconds = 25,
                    type = ExerciseType.SMOOTH_PURSUIT,
                    physiologicalBenefit = "Stimulates lateral extraocular muscles and reduces focal stiffness."
                )
            )
        ),
        Protocol(
            id = "dry_eye_hydrate",
            title = "Dry Eye Re-Hydration",
            tag = "Dryness Relief",
            totalSeconds = 45,
            description = "Designed for screen workers whose blink rate drops 70% during concentration.",
            targetSymptom = "Burning, Grittiness & Dryness",
            phases = listOf(
                ExercisePhase(
                    title = "Deep Lubricating Blinks",
                    instruction = "Close your eyes completely for 2 seconds. Open and relax. Repeat smoothly.",
                    durationSeconds = 20,
                    type = ExerciseType.RAPID_BLINK,
                    physiologicalBenefit = "Replenishes the evaporative tear layer to prevent corneal dry spots."
                ),
                ExercisePhase(
                    title = "Eyes-Closed Micro Palming",
                    instruction = "Close your eyes. Cover them softly with your warm palms. Breathe deeply.",
                    durationSeconds = 25,
                    type = ExerciseType.PALMING_BREATH,
                    eyesClosed = true,
                    physiologicalBenefit = "Warmth dilates eyelid glands and parasympathetic tone slows tear evaporation."
                )
            )
        ),
        Protocol(
            id = "accommodative_reset",
            title = "Accommodative Flexibility",
            tag = "Near-Far Jump",
            totalSeconds = 90,
            description = "Dynamic near-to-far jumps preventing late-afternoon pseudomyopia.",
            targetSymptom = "Afternoon Focal Blur",
            phases = listOf(
                ExercisePhase(
                    title = "Near-Far Accommodation",
                    instruction = "Hold your thumb 10 inches away. Focus on your thumb, then look 20 feet beyond.",
                    durationSeconds = 40,
                    type = ExerciseType.ACCOMMODATION_SHIFT,
                    physiologicalBenefit = "Alternates between ciliary contraction and full zonular stretch."
                ),
                ExercisePhase(
                    title = "Cross-Quadrant Saccades",
                    instruction = "Shift your gaze instantly between the jumping target markers.",
                    durationSeconds = 30,
                    type = ExerciseType.SACCADE_JUMP,
                    physiologicalBenefit = "Calibrates rapid oculomotor saccadic targeting without fatigue."
                ),
                ExercisePhase(
                    title = "Horizon Rest",
                    instruction = "Look at the furthest visible horizon or out the window. Let your gaze soften.",
                    durationSeconds = 20,
                    type = ExerciseType.ACCOMMODATION_SHIFT,
                    physiologicalBenefit = "Achieves infinite-focal optical rest."
                )
            )
        ),
        Protocol(
            id = "headache_tension_soothe",
            title = "Tension & Headache Soothe",
            tag = "Deep Release",
            totalSeconds = 120,
            description = "De-escalate brow tension, forehead clamping, and optic nerve sensory overload.",
            targetSymptom = "Frontal Tension Headache",
            phases = listOf(
                ExercisePhase(
                    title = "Eyes-Closed Palming",
                    instruction = "Warm your palms together. Cup them over your closed eyes blocking all light.",
                    durationSeconds = 50,
                    type = ExerciseType.PALMING_BREATH,
                    eyesClosed = true,
                    physiologicalBenefit = "Total optical darkness halts retinal stimulation and calms visual cortex."
                ),
                ExercisePhase(
                    title = "Gentle Circular Orbit",
                    instruction = "Keep your gaze soft and trace a broad circular loop without strain.",
                    durationSeconds = 40,
                    type = ExerciseType.SMOOTH_PURSUIT,
                    physiologicalBenefit = "Releases spastic tension in the rectus and oblique eye muscles."
                ),
                ExercisePhase(
                    title = "Diaphragmatic Wind-Down",
                    instruction = "Slow 4-second inhale, 6-second exhale. Feel the muscles behind your eyebrows let go.",
                    durationSeconds = 30,
                    type = ExerciseType.PALMING_BREATH,
                    eyesClosed = true,
                    physiologicalBenefit = "Activates vagal parasympathetic relaxation."
                )
            )
        )
    )
}
