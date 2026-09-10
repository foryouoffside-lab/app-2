package com.example.util

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.sin

/**
 * Stage 10: Multi-Modal Audio, Voice Guidance & Haptic Safety Manager
 *
 * Implements deterministic auditory timing cues, pure sine harmonic breathing synthesis (432Hz),
 * and platform-compliant tactile feedback without external asset dependencies.
 */
class SensoryFeedbackManager(private val context: Context) {

    private val vibrator: Vibrator? by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }

    var hapticsEnabled: Boolean = true
    var soundEnabled: Boolean = true

    // ==========================================
    // HAPTIC PATTERN ENGINE
    // ==========================================

    /**
     * Tactile verification when user hits a saccade target (sub-25ms sharp click)
     */
    fun triggerTargetHitHaptic() {
        if (!hapticsEnabled) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                vibrator?.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(25, 180))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(25)
            }
        } catch (_: Exception) {}
    }

    /**
     * Soft single tick for calibration, jump ticks, or spatial anchors (<15ms)
     */
    fun triggerTickHaptic() {
        if (!hapticsEnabled) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                vibrator?.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK))
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(15, 100))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(15)
            }
        } catch (_: Exception) {}
    }

    /**
     * Tactile pulse indicating phase boundary transition
     */
    fun triggerPhaseHaptic() {
        if (!hapticsEnabled) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val effect = VibrationEffect.createWaveform(
                    longArrayOf(0, 30, 40, 45),
                    intArrayOf(0, 160, 0, 210),
                    -1
                )
                vibrator?.vibrate(effect)
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(40)
            }
        } catch (_: Exception) {}
    }

    /**
     * Double-pulse indicating session completion and persistence save
     */
    fun triggerCompletionHaptic() {
        if (!hapticsEnabled) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val effect = VibrationEffect.createWaveform(
                    longArrayOf(0, 35, 50, 60),
                    intArrayOf(0, 200, 0, 240),
                    -1
                )
                vibrator?.vibrate(effect)
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(60)
            }
        } catch (_: Exception) {}
    }

    // ==========================================
    // AUDIO SYNTHESIS ENGINE
    // ==========================================

    /**
     * Pure sine wave tone generator with anti-pop Hann window envelopes.
     * Prevents auditory clicks and sudden high-frequency transients.
     */
    suspend fun playChimeTone(frequency: Double = 432.0, durationMs: Int = 350) = withContext(Dispatchers.Default) {
        if (!soundEnabled) return@withContext
        try {
            val sampleRate = 44100
            val numSamples = (sampleRate * (durationMs / 1000.0)).toInt()
            val sample = DoubleArray(numSamples)
            val generatedSnd = ByteArray(2 * numSamples)

            for (i in 0 until numSamples) {
                // Smooth Hann window envelope to prevent start/stop speaker pops
                val envelope = when {
                    i < numSamples * 0.15 -> i / (numSamples * 0.15)
                    i > numSamples * 0.65 -> (numSamples - i) / (numSamples * 0.35)
                    else -> 1.0
                }
                sample[i] = sin(2.0 * Math.PI * i.toDouble() / (sampleRate / frequency)) * envelope
            }

            var idx = 0
            for (dVal in sample) {
                val valShort = (dVal * 32767).toInt().toShort()
                generatedSnd[idx++] = (valShort.toInt() and 0x00ff).toByte()
                generatedSnd[idx++] = ((valShort.toInt() and 0xff00) ushr 8).toByte()
            }

            val audioTrack = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(generatedSnd.size)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()

            audioTrack.write(generatedSnd, 0, generatedSnd.size)
            audioTrack.play()
        } catch (_: Exception) {}
    }

    /**
     * Stage 10: 880Hz crisp auditory confirmation chime for saccade target acquisition
     */
    suspend fun playHitChime() {
        playChimeTone(frequency = 880.0, durationMs = 90)
    }

    /**
     * Stage 10: 432Hz deep harmonic tone for ocular relaxation and palming rest
     */
    suspend fun playRecoveryPulse() {
        playChimeTone(frequency = 432.0, durationMs = 500)
    }

    /**
     * Stage 10: 528Hz biological completion chime
     */
    suspend fun playCompletionChime() {
        playChimeTone(frequency = 528.0, durationMs = 600)
    }
}
