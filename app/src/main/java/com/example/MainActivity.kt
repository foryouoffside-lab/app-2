package com.example

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.example.data.AppDatabase
import com.example.data.SessionLog
import com.example.model.ProgressionEngine
import com.example.model.Protocol
import com.example.ui.drill.DrillScreen
import com.example.ui.AppPillarTab
import com.example.ui.MainAppContainer
import com.example.ui.screens.OnboardingScreen
import com.example.ui.theme.AppTheme
import com.example.ui.theme.EyeRestTheme
import com.example.ui.theme.isDarkTheme
import com.example.util.UserPrefs
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = AppDatabase.getDatabase(this)
        val sessionLogDao = database.sessionLogDao()

        val prefs = UserPrefs(this)

        setContent {
            EyeRestTheme(themeMode = prefs.themeMode) {
                val coroutineScope = rememberCoroutineScope()
                var activeProtocol by remember { mutableStateOf<Protocol?>(null) }
                var editingWellnessProfile by remember { mutableStateOf(false) }
                // Lives out here so closing a drill returns to the tab it was started from.
                var activeTab by remember { mutableStateOf(AppPillarTab.TODAY) }
                val background = AppTheme.colors.bg

                // Without this the status bar keeps light icons and goes invisible on
                // the daylight palette.
                val view = LocalView.current
                val darkTheme = isDarkTheme(prefs.themeMode)
                SideEffect {
                    val window = (view.context as Activity).window
                    WindowCompat.getInsetsController(window, view).run {
                        isAppearanceLightStatusBars = !darkTheme
                        isAppearanceLightNavigationBars = !darkTheme
                    }
                }

                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(background),
                    color = background
                ) {
                    val savedProfile = prefs.wellnessProfile
                    if (savedProfile == null || editingWellnessProfile) {
                        OnboardingScreen(
                            initialProfile = savedProfile,
                            initialAge = prefs.age ?: 30,
                            onCompleted = {
                                prefs.updateWellnessProfile(it)
                                editingWellnessProfile = false
                            }
                        )
                        return@Surface
                    }
                    AnimatedContent(
                        targetState = activeProtocol,
                        transitionSpec = { fadeIn() togetherWith fadeOut() },
                        label = "screen_transition"
                    ) { protocol ->
                        if (protocol != null) {
                            DrillScreen(
                                protocol = protocol,
                                voiceDefault = prefs.voiceEnabled,
                                hapticsEnabled = prefs.hapticsEnabled,
                                targetColor = prefs.targetColor,
                                targetSpeed = prefs.targetSpeed,
                                onTargetSpeedChange = { prefs.updateTargetSpeed(it) },
                                onClose = { activeProtocol = null },
                                onCompleted = { protocolId, duration ->
                                    coroutineScope.launch {
                                        sessionLogDao.insertLog(
                                            SessionLog(
                                                protocolId = protocolId,
                                                protocolTitle = protocol.title,
                                                durationSeconds = duration
                                            )
                                        )
                                        ProgressionEngine.recordDrillCompletion(
                                            categoryId = "TRACKING",
                                            accuracy = 0.90f,
                                            sessionLogDao = sessionLogDao
                                        )
                                    }
                                    activeProtocol = null
                                }
                            )
                        } else {
                            MainAppContainer(
                                sessionLogDao = sessionLogDao,
                                profile = savedProfile,
                                themeMode = prefs.themeMode,
                                voiceEnabled = prefs.voiceEnabled,
                                hapticsEnabled = prefs.hapticsEnabled,
                                targetColor = prefs.targetColor,
                                targetSpeed = prefs.targetSpeed,
                                onAgeChange = { prefs.updateAge(it) },
                                onThemeChange = { prefs.updateThemeMode(it) },
                                onVoiceChange = { prefs.updateVoiceEnabled(it) },
                                onHapticsChange = { prefs.updateHapticsEnabled(it) },
                                onTargetColorChange = { prefs.updateTargetColor(it) },
                                onTargetSpeedChange = { prefs.updateTargetSpeed(it) },
                                onRetakeAssessment = { editingWellnessProfile = true },
                                onStartProtocol = { selected ->
                                    activeProtocol = selected
                                },
                                activeTab = activeTab,
                                onTabChange = { activeTab = it }
                            )
                        }
                    }
                }
            }
        }
    }
}
