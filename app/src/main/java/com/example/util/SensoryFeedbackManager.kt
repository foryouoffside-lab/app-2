package com.example.util

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
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
    var voiceEnabled: Boolean = true

    // ==========================================
    // VOICE COACH (platform TextToSpeech)
    // ==========================================

    private var tts: TextToSpeech? = null
    private var ttsReady = false

    // Utterances someone is waiting on. The engine reports completion on its own thread.
    private val awaiting = ConcurrentHashMap<String, CompletableDeferred<Unit>>()
    private val utteranceIds = AtomicLong(0)

    // Bumped by every stopVoice. A script started before the bump must not carry on
    // speaking after it -- see [speakSequence].
    private val speechEpoch = AtomicLong(0)

    // Only for [speak] to fall back onto when the engine is still warming up, so a cue
    // fired at cold start queues behind readiness instead of being silently dropped.
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    // Some OEM TTS engines start a fresh utterance at whatever the stream volume happens
    // to be, including silence, until something else (a tap, a volume key) nudges the
    // stream awake. Pinning both the audio attributes and an explicit full-volume param
    // is the standard fix: it stops the engine from depending on ambient stream state.
    private val speechParams = Bundle().apply { putFloat(TextToSpeech.Engine.KEY_PARAM_VOLUME, 1.0f) }

    private fun settle(utteranceId: String?) {
        utteranceId?.let { awaiting.remove(it)?.complete(Unit) }
    }

    /** Warms up the engine so the first cue of a session is not swallowed. */
    fun initVoice() {
        if (tts != null) return
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale.getDefault().takeIf {
                    tts?.isLanguageAvailable(it) == TextToSpeech.LANG_AVAILABLE
                } ?: Locale.US
                tts?.setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ASSISTANCE_ACCESSIBILITY)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .build()
                )
                // Every terminal state has to settle the waiter, or a drill that hits a
                // TTS error would sit on the tutorial forever.
                tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) = Unit
                    override fun onDone(utteranceId: String?) = settle(utteranceId)

                    @Deprecated("Required by the pre-21 interface", ReplaceWith(""))
                    override fun onError(utteranceId: String?) = settle(utteranceId)

                    override fun onError(utteranceId: String?, errorCode: Int) = settle(utteranceId)
                    override fun onStop(utteranceId: String?, interrupted: Boolean) = settle(utteranceId)
                })
                ttsReady = true
            }
        }
    }

    /**
     * Suspends until the engine is actually usable, or [timeoutMs] passes.
     *
     * TextToSpeech initialises asynchronously. Every speak call is a no-op until it
     * reports ready, so a tutorial that starts talking on a fixed short delay loses its
     * whole script on a cold start and the user just gets silence.
     */
    suspend fun awaitVoiceReady(timeoutMs: Long = 4_000): Boolean {
        if (!voiceEnabled) return false
        if (ttsReady) return true
        return withTimeoutOrNull(timeoutMs) {
            while (!ttsReady) delay(50)
            true
        } ?: false
    }

    /**
     * Speaks, and suspends until the engine says it has finished.
     *
     * The tutorial needs this. Fire-and-forget let the countdown start counting over the
     * instruction it was supposed to follow, so the two ran on top of each other. Returns
     * at once when voice is off, and gives up after [timeoutMs] so an engine that never
     * reports back cannot strand the user on the tutorial.
     */
    suspend fun speakAwait(text: String, timeoutMs: Long = 15_000) {
        if (!voiceEnabled || !ttsReady || text.isBlank()) return
        val epoch = speechEpoch.get()
        val id = "await-" + utteranceIds.incrementAndGet()
        val done = CompletableDeferred<Unit>()
        awaiting[id] = done
        try {
            tts?.speak(text, TextToSpeech.QUEUE_ADD, speechParams, id)
            withTimeoutOrNull(timeoutMs) { done.await() }
        } catch (_: Exception) {
        } finally {
            awaiting.remove(id)?.complete(Unit)
        }
        // Silently swallowed if we were stopped mid-line: the caller is mid-script and
        // would otherwise read the next line into whatever replaced it.
        if (speechEpoch.get() != epoch) throw SpeechInterrupted
    }

    /**
     * Speaks [lines] in order, each waiting for the last, and abandons the rest the
     * moment [stopVoice] is called.
     *
     * Without the abort, ending a tutorial early did not end its script. stopVoice settles
     * the line being spoken, which made the wait return normally, so the next line was
     * queued and spoke over the drill that had just started -- about ten seconds of
     * tutorial narration on top of an exercise meant to be silent.
     */
    suspend fun speakSequence(vararg lines: String) {
        try {
            lines.forEach { speakAwait(it) }
        } catch (_: SpeechInterrupted) {
        }
    }

    /** Thrown internally when a script is cut short. Never escapes [speakSequence]. */
    private object SpeechInterrupted : Exception() {
        private fun readResolve(): Any = SpeechInterrupted
        override fun fillInStackTrace(): Throwable = this
    }

    /**
     * Speaks a coaching cue. [interrupt] mirrors a spoken countdown cutting off a
     * longer instruction so the number still lands on its own second.
     *
     * If the engine is still warming up (a cold start, or right after [initVoice]) this
     * used to just drop the cue on the floor -- the caller had no way to know, and the
     * very first line of a drill would go out in silence. It now waits a short, bounded
     * moment for readiness instead, off the caller's own coroutine so nothing here blocks.
     */
    fun speak(text: String, interrupt: Boolean = false) {
        if (!voiceEnabled || text.isBlank()) return
        if (ttsReady) {
            speakNow(text, interrupt)
        } else {
            scope.launch { if (awaitVoiceReady()) speakNow(text, interrupt) }
        }
    }

    private fun speakNow(text: String, interrupt: Boolean) {
        try {
            tts?.speak(
                text,
                if (interrupt) TextToSpeech.QUEUE_FLUSH else TextToSpeech.QUEUE_ADD,
                speechParams,
                text.hashCode().toString()
            )
        } catch (_: Exception) {}
    }

    fun stopVoice() {
        speechEpoch.incrementAndGet()
        try { tts?.stop() } catch (_: Exception) {}
        // onStop is not guaranteed for queued utterances that never started.
        awaiting.keys.toList().forEach { settle(it) }
    }

    /** Must be called when the session leaves the screen, or the engine leaks. */
    fun release() {
        try { tts?.stop(); tts?.shutdown() } catch (_: Exception) {}
        awaiting.keys.toList().forEach { settle(it) }
        scope.cancel()
        tts = null
        ttsReady = false
    }

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
