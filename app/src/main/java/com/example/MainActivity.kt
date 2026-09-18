package com.example

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Build
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
import androidx.core.content.ContextCompat
import androidx.core.app.ActivityCompat
import com.example.data.AppDatabase
import com.example.data.SessionLog
import com.example.model.Protocol
import com.example.ui.drill.DrillScreen
import com.example.ui.AppPillarTab
import com.example.ui.MainAppContainer
import com.example.ui.screens.OnboardingScreen
import com.example.ui.theme.AppTheme
import com.example.ui.theme.EyeRestTheme
import com.example.ui.theme.isDarkTheme
import com.example.util.UserPrefs
import com.example.util.BreakReminderScheduler
import com.example.util.ScreenUseWatchService
import com.example.util.BreakReminderSettings
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private lateinit var prefs: UserPrefs
    private var pendingReminderSettings: BreakReminderSettings? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = AppDatabase.getDatabase(this)
        val sessionLogDao = database.sessionLogDao()

        prefs = UserPrefs(this)

        BreakReminderScheduler.createChannel(this)
        BreakReminderScheduler.update(this, prefs.breakReminderSettings)
        ScreenUseWatchService.sync(this)

        setContent {
            EyeRestTheme(themeMode = prefs.themeMode, trueBlackEnabled = prefs.trueBlackEnabled) {
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
                                breakReminderSettings = prefs.breakReminderSettings,
                                onAgeChange = { prefs.updateAge(it) },
                                onThemeChange = { prefs.updateThemeMode(it) },
                                onVoiceChange = { prefs.updateVoiceEnabled(it) },
                                onHapticsChange = { prefs.updateHapticsEnabled(it) },
                                trueBlackEnabled = prefs.trueBlackEnabled,
                                onTrueBlackChange = { prefs.updateTrueBlackEnabled(it) },
                                onTargetColorChange = { prefs.updateTargetColor(it) },
                                onTargetSpeedChange = { prefs.updateTargetSpeed(it) },
                                onBreakReminderSettingsChange = ::updateBreakReminders,
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

    private fun updateBreakReminders(settings: BreakReminderSettings) {
        val permissionNeeded = settings.enabled &&
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        if (permissionNeeded) {
            pendingReminderSettings = settings
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                NOTIFICATION_PERMISSION_REQUEST
            )
        } else {
            prefs.updateBreakReminderSettings(settings)
            BreakReminderScheduler.update(this, settings)
            ScreenUseWatchService.sync(this)
        }
    }

    @Suppress("DEPRECATION", "OVERRIDE_DEPRECATION")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode != NOTIFICATION_PERMISSION_REQUEST) return
        val requested = pendingReminderSettings
        pendingReminderSettings = null
        if (grantResults.firstOrNull() == PackageManager.PERMISSION_GRANTED && requested != null) {
            prefs.updateBreakReminderSettings(requested)
            BreakReminderScheduler.update(this, requested)
            ScreenUseWatchService.sync(this)
        }
    }

    private companion object {
        const val NOTIFICATION_PERMISSION_REQUEST = 2020
    }
}
