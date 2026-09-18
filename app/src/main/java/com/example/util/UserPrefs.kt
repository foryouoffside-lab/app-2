package com.example.util

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.model.ClinicalContext
import com.example.model.ExerciseTimeBand
import com.example.model.ScreenTimeBand
import com.example.model.VisionCorrection
import com.example.model.WellnessProfile
import com.example.model.WellnessSymptom

enum class ThemeMode { SYSTEM, LIGHT, DARK }

/**
 * Colour of the drill target.
 *
 * Offered as a setting because the right answer is personal: a red-green colour
 * deficiency makes some targets vanish against the ground, and a target that is
 * comfortable for one person glares for another.
 *
 * [argb] is applied to every drill whose target is a plain mark. Drills that need
 * specific colours to work at all -- the red/cyan anaglyph pair, the three-depth Brock
 * string -- keep theirs, because there the colour IS the exercise.
 */
enum class TargetColor(val label: String, val argb: Long) {
    AMBER("Amber", 0xFFF59E0B),
    TEAL("Teal", 0xFF2DD4BF),
    WHITE("White", 0xFFF1F5F9),
    MAGENTA("Magenta", 0xFFE879F9)
}

/**
 * How fast a moving target travels, as a multiplier on its path rate.
 *
 * A free value rather than three named steps: the speed at which a target stays trackable
 * is personal, and the jump from one preset to the next skipped over whatever sat between
 * them.
 *
 * Deliberately does NOT change the drill's length. The dose stays the dose; this changes
 * how far the target travels within it.
 */
val TARGET_SPEED_RANGE = 0.4f..2.0f
const val DEFAULT_TARGET_SPEED = 1.0f

/** The multiplier as the user reads it: "1.0x" is the pace the drill was written at. */
fun targetSpeedLabel(speed: Float): String = String.format("%.1f×", speed)

/** Ages outside this range are almost certainly a typo, and the drill maths needs a sane input. */
val SUPPORTED_AGES = 8..100

/**
 * Age and theme, held in SharedPreferences and mirrored into Compose state so the UI
 * reacts without a observable-preferences dependency.
 */
class UserPrefs(context: Context) {

    private val prefs = context.applicationContext
        .getSharedPreferences("eyerest_prefs", Context.MODE_PRIVATE)

    /** Null until the user has told us, which is what gates onboarding. */
    var age by mutableStateOf(prefs.getInt(KEY_AGE, 0).takeIf { it in SUPPORTED_AGES })
        private set

    /** Null until the full safety and comfort check-in has been completed. */
    var wellnessProfile by mutableStateOf(loadWellnessProfile())
        private set

    /** Existing installs must answer the newly added availability question once. */
    var needsExerciseTimeSetup by mutableStateOf(
        prefs.getBoolean(KEY_PROFILE_COMPLETE, false) && !prefs.contains(KEY_EXERCISE_TIME)
    )
        private set

    var themeMode by mutableStateOf(
        runCatching { ThemeMode.valueOf(prefs.getString(KEY_THEME, null) ?: "") }
            .getOrDefault(ThemeMode.DARK)
    )
        private set

    /** Spoken guidance during drills. */
    var voiceEnabled by mutableStateOf(prefs.getBoolean(KEY_VOICE, true))
        private set

    /** Haptic cues at stage boundaries and on completion. */
    var hapticsEnabled by mutableStateOf(prefs.getBoolean(KEY_HAPTICS, true))
        private set

    /** Local notification schedule; no login, server or analytics identifier is needed. */
    var breakReminderSettings by mutableStateOf(
        BreakReminderSettings(
            enabled = prefs.getBoolean(KEY_BREAK_REMINDER_ENABLED, false),
            smartEnabled = prefs.getBoolean(KEY_BREAK_REMINDER_SMART, false),
            intervalMinutes = prefs.getInt(KEY_BREAK_REMINDER_INTERVAL, 20),
            startHour = prefs.getInt(KEY_BREAK_REMINDER_START, 9),
            endHour = prefs.getInt(KEY_BREAK_REMINDER_END, 18),
            weekdaysOnly = prefs.getBoolean(KEY_BREAK_REMINDER_WEEKDAYS, true)
        ).sanitized()
    )
        private set

    fun updateVoiceEnabled(enabled: Boolean) {
        voiceEnabled = enabled
        prefs.edit().putBoolean(KEY_VOICE, enabled).apply()
    }

    fun updateHapticsEnabled(enabled: Boolean) {
        hapticsEnabled = enabled
        prefs.edit().putBoolean(KEY_HAPTICS, enabled).apply()
    }

    fun updateBreakReminderSettings(value: BreakReminderSettings) {
        breakReminderSettings = value.sanitized()
        prefs.edit()
            .putBoolean(KEY_BREAK_REMINDER_ENABLED, breakReminderSettings.enabled)
            .putBoolean(KEY_BREAK_REMINDER_SMART, breakReminderSettings.smartEnabled)
            .putInt(KEY_BREAK_REMINDER_INTERVAL, breakReminderSettings.intervalMinutes)
            .putInt(KEY_BREAK_REMINDER_START, breakReminderSettings.startHour)
            .putInt(KEY_BREAK_REMINDER_END, breakReminderSettings.endHour)
            .putBoolean(KEY_BREAK_REMINDER_WEEKDAYS, breakReminderSettings.weekdaysOnly)
            .apply()
    }

    /** Colour of the drill target, for drills whose target colour is not part of the task. */
    var targetColor by mutableStateOf(
        runCatching { TargetColor.valueOf(prefs.getString(KEY_TARGET_COLOR, null) ?: "") }
            .getOrDefault(TargetColor.AMBER)
    )
        private set

    /** How fast a moving target travels. Does not change the drill's length. */
    var targetSpeed by mutableStateOf(
        prefs.getFloat(KEY_TARGET_SPEED, DEFAULT_TARGET_SPEED).coerceIn(TARGET_SPEED_RANGE)
    )
        private set

    fun updateTargetColor(value: TargetColor) {
        targetColor = value
        prefs.edit().putString(KEY_TARGET_COLOR, value.name).apply()
    }

    fun updateTargetSpeed(value: Float) {
        targetSpeed = value.coerceIn(TARGET_SPEED_RANGE)
        prefs.edit().putFloat(KEY_TARGET_SPEED, targetSpeed).apply()
    }

    /** Returns false and stores nothing if the value is out of range. */
    fun updateAge(value: Int): Boolean {
        if (value !in SUPPORTED_AGES) return false
        age = value
        wellnessProfile = wellnessProfile?.copy(age = value)
        prefs.edit().putInt(KEY_AGE, value).apply()
        return true
    }

    fun updateWellnessProfile(value: WellnessProfile): Boolean {
        if (value.age !in SUPPORTED_AGES) return false
        age = value.age
        wellnessProfile = value
        needsExerciseTimeSetup = false
        prefs.edit()
            .putInt(KEY_AGE, value.age)
            .putBoolean(KEY_PROFILE_COMPLETE, true)
            .putString(KEY_SCREEN_TIME, value.screenTime.name)
            .putString(KEY_CORRECTION, value.correction.name)
            .putStringSet(KEY_SYMPTOMS, value.symptoms.mapTo(mutableSetOf()) { it.name })
            .putStringSet(KEY_CLINICAL_CONTEXTS, value.clinicalContexts.mapTo(mutableSetOf()) { it.name })
            .putBoolean(KEY_URGENT_SYMPTOMS, value.urgentSymptoms)
            .putString(KEY_EXERCISE_TIME, value.exerciseTime.name)
            .apply()
        return true
    }

    fun updateThemeMode(mode: ThemeMode) {
        themeMode = mode
        prefs.edit().putString(KEY_THEME, mode.name).apply()
    }

    private fun loadWellnessProfile(): WellnessProfile? {
        if (!prefs.getBoolean(KEY_PROFILE_COMPLETE, false)) return null
        val storedAge = prefs.getInt(KEY_AGE, 0).takeIf { it in SUPPORTED_AGES } ?: return null
        return WellnessProfile(
            age = storedAge,
            screenTime = enumValueOrDefault(KEY_SCREEN_TIME, ScreenTimeBand.TWO_TO_FOUR),
            correction = enumValueOrDefault(KEY_CORRECTION, VisionCorrection.NONE),
            symptoms = enumSet(KEY_SYMPTOMS),
            clinicalContexts = enumSet(KEY_CLINICAL_CONTEXTS),
            urgentSymptoms = prefs.getBoolean(KEY_URGENT_SYMPTOMS, false),
            exerciseTime = enumValueOrDefault(KEY_EXERCISE_TIME, ExerciseTimeBand.FIVE_TO_TEN)
        )
    }

    private inline fun <reified T : Enum<T>> enumValueOrDefault(key: String, fallback: T): T =
        runCatching { enumValueOf<T>(prefs.getString(key, null).orEmpty()) }.getOrDefault(fallback)

    private inline fun <reified T : Enum<T>> enumSet(key: String): Set<T> =
        prefs.getStringSet(key, emptySet()).orEmpty().mapNotNullTo(mutableSetOf()) {
            runCatching { enumValueOf<T>(it) }.getOrNull()
        }

    private companion object {
        const val KEY_AGE = "age"
        const val KEY_THEME = "theme_mode"
        const val KEY_VOICE = "voice_enabled"
        const val KEY_HAPTICS = "haptics_enabled"
        const val KEY_BREAK_REMINDER_ENABLED = "break_reminder_enabled"
        const val KEY_BREAK_REMINDER_SMART = "break_reminder_smart_enabled"
        const val KEY_BREAK_REMINDER_INTERVAL = "break_reminder_interval_minutes"
        const val KEY_BREAK_REMINDER_START = "break_reminder_start_hour"
        const val KEY_BREAK_REMINDER_END = "break_reminder_end_hour"
        const val KEY_BREAK_REMINDER_WEEKDAYS = "break_reminder_weekdays_only"
        const val KEY_TARGET_COLOR = "target_color"
        const val KEY_TARGET_SPEED = "target_speed_x"
        const val KEY_PROFILE_COMPLETE = "wellness_profile_complete"
        const val KEY_SCREEN_TIME = "screen_time"
        const val KEY_CORRECTION = "vision_correction"
        const val KEY_SYMPTOMS = "wellness_symptoms"
        const val KEY_CLINICAL_CONTEXTS = "clinical_contexts"
        const val KEY_URGENT_SYMPTOMS = "urgent_eye_symptoms"
        const val KEY_EXERCISE_TIME = "exercise_time"
    }
}
