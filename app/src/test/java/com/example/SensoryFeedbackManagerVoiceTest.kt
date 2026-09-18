package com.example

import android.os.Looper
import android.speech.tts.TextToSpeech
import androidx.test.core.app.ApplicationProvider
import com.example.util.SensoryFeedbackManager
import java.util.concurrent.TimeUnit
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Shadows.shadowOf
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowTextToSpeech

/**
 * Reproduces the drill how-to-steps bug: a step's voiceover was queued while the TTS
 * engine was still warming up, the user moved to a different step before it finished
 * warming up, and the stale line spoke anyway on top of the step actually on screen.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class SensoryFeedbackManagerVoiceTest {

    @Test
    fun `a cue queued before the engine is ready is dropped once a later step supersedes it`() {
        val coach = SensoryFeedbackManager(ApplicationProvider.getApplicationContext())
        coach.initVoice()
        // Engine is mid cold-start: onInit has not fired yet, so speak() has to queue.
        coach.speak("Step one text")

        // The user backs out of step one before the engine ever became ready.
        coach.stopVoice()
        coach.speak("Step two text")

        // Now the engine finishes warming up, and the queued waits wake up.
        val tts = ShadowTextToSpeech.getLastTextToSpeechInstance()
        shadowOf(tts).onInitListener.onInit(TextToSpeech.SUCCESS)
        shadowOf(Looper.getMainLooper()).idleFor(500, TimeUnit.MILLISECONDS)

        val spoken = shadowOf(tts).spokenTextList
        assertFalse("stale step-one cue must not survive the step change", spoken.contains("Step one text"))
        assertEquals(listOf("Step two text"), spoken)
    }
}
