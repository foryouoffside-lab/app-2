package com.example

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.util.BREAK_SECONDS
import com.example.util.SensoryFeedbackManager
import com.example.util.UserPrefs
import com.example.util.spokenCount
import com.example.ui.theme.AppTheme
import com.example.ui.theme.EyeRestTheme
import kotlinx.coroutines.delay

/**
 * The 20 seconds of "20-20-20", as its own task.
 *
 * Own task (see the manifest) is the whole trick behind returning the user to what they
 * were doing: finishing this activity pops only this task, so the app underneath -- the
 * one they were reading when the nudge fired -- resumes exactly where they left it. That
 * needs no knowledge of which app it was and no package-launch intent that would restart it.
 *
 * The count is spoken, not just drawn, because a break spent watching our countdown is not
 * a break. The screen is a fallback for muted phones, not the main channel.
 */
class BreakActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Arrives over a lock screen: the break is worthless if it waits for an unlock.
        setShowWhenLocked(true)
        setTurnScreenOn(true)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        val prefs = UserPrefs(this)
        setContent {
            EyeRestTheme(themeMode = prefs.themeMode, trueBlackEnabled = prefs.trueBlackEnabled) {
                BreakCountdown(
                    voiceEnabled = prefs.voiceEnabled,
                    hapticsEnabled = prefs.hapticsEnabled,
                    onFinished = ::finish
                )
            }
        }
    }
}

@Composable
private fun BreakCountdown(
    voiceEnabled: Boolean,
    hapticsEnabled: Boolean,
    onFinished: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val feedback = remember {
        SensoryFeedbackManager(context).apply {
            this.voiceEnabled = voiceEnabled
            this.hapticsEnabled = hapticsEnabled
        }
    }
    var remaining by remember { mutableIntStateOf(BREAK_SECONDS) }

    DisposableEffect(Unit) {
        feedback.initVoice()
        onDispose { feedback.release() }
    }

    LaunchedEffect(Unit) {
        // Nothing to say until the engine is up, and nothing worth delaying the break for.
        feedback.awaitVoiceReady()
        feedback.speak("Look at something far away.")
        while (remaining > 0) {
            delay(1_000)
            remaining -= 1
            feedback.speak(spokenCount(remaining), interrupt = true)
        }
        feedback.triggerTargetHitHaptic()
        delay(900)
        onFinished()
    }

    Box(
        Modifier.fillMaxSize().background(AppTheme.colors.bg).testTag("break_countdown"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            Text(
                "LOOK 20 FEET AWAY",
                color = AppTheme.colors.amber,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.6.sp
            )
            Text(
                "$remaining",
                color = AppTheme.colors.textHigh,
                fontSize = 96.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                "You will go back automatically.",
                color = AppTheme.colors.textMuted,
                fontSize = 13.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}
