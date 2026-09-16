package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.SessionLogDao
import com.example.model.Protocol
import com.example.model.WellnessProfile
import com.example.util.TargetColor
import com.example.util.ThemeMode
import com.example.model.ProtocolsRepository
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.ProgressScreen
import com.example.ui.screens.ChallengeScreen
import com.example.ui.screens.TodayScreen
import com.example.ui.screens.TrainScreen
import com.example.ui.theme.AppTheme
import com.example.ui.theme.BiologicalTeal
import com.example.ui.theme.CharcoalSurface
import com.example.ui.theme.MutedBorder
import com.example.ui.theme.ObsidianBg
import com.example.ui.theme.TextMediumEmphasis

enum class AppPillarTab { TODAY, TRAIN, CHALLENGE, PROGRESS, PROFILE }

@Composable
fun MainAppContainer(
    sessionLogDao: SessionLogDao,
    profile: WellnessProfile,
    themeMode: ThemeMode,
    voiceEnabled: Boolean,
    hapticsEnabled: Boolean,
    targetColor: TargetColor,
    targetSpeed: Float,
    onAgeChange: (Int) -> Unit,
    onThemeChange: (ThemeMode) -> Unit,
    onVoiceChange: (Boolean) -> Unit,
    onHapticsChange: (Boolean) -> Unit,
    onTargetColorChange: (TargetColor) -> Unit,
    onTargetSpeedChange: (Float) -> Unit,
    onRetakeAssessment: () -> Unit,
    onStartProtocol: (Protocol) -> Unit,
    /**
     * Which tab is showing, owned by the caller.
     *
     * Hoisted because a running drill replaces this whole container, so a tab remembered
     * in here would be rebuilt as Today every time a drill closed. Starting a drill from
     * Train has to come back to Train.
     */
    activeTab: AppPillarTab,
    onTabChange: (AppPillarTab) -> Unit
) {
    // Back steps home first; only then does it fall through and leave the app.
    BackHandler(enabled = activeTab != AppPillarTab.TODAY) {
        onTabChange(AppPillarTab.TODAY)
    }
    val tabs = listOf(
        Triple(AppPillarTab.TODAY, Icons.Default.Visibility, "Today"),
        Triple(AppPillarTab.TRAIN, Icons.Default.FitnessCenter, "Train"),
        Triple(AppPillarTab.CHALLENGE, Icons.Default.EmojiEvents, "Challenges & Tests"),
        Triple(AppPillarTab.PROGRESS, Icons.Default.Insights, "Progress"),
        Triple(AppPillarTab.PROFILE, Icons.Default.Person, "Profile")
    )

    Scaffold(
        modifier = Modifier.fillMaxSize().background(AppTheme.colors.bg),
        bottomBar = {
            Surface(
                color = AppTheme.colors.surface,
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, AppTheme.colors.border),
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            ) {
                NavigationBar(containerColor = AppTheme.colors.surface, tonalElevation = 0.dp, modifier = Modifier.navigationBarsPadding()) {
                    tabs.forEach { (tab, icon, label) ->
                        NavigationBarItem(
                            selected = activeTab == tab,
                            onClick = { onTabChange(tab) },
                            icon = { Icon(icon, contentDescription = label) },
                            label = { Text(label) },
                            alwaysShowLabel = false,
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = AppTheme.colors.teal,
                                selectedTextColor = AppTheme.colors.teal,
                                indicatorColor = AppTheme.colors.teal.copy(alpha = 0.14f),
                                unselectedIconColor = AppTheme.colors.textMedium,
                                unselectedTextColor = AppTheme.colors.textMedium
                            ),
                            modifier = Modifier.testTag("nav_${tab.name.lowercase()}")
                        )
                    }
                }
            }
        }
    ) { _ ->
        AnimatedContent(
            targetState = activeTab,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "screen_transition",
            modifier = Modifier
                .fillMaxSize()
                .background(AppTheme.colors.bg)
                .statusBarsPadding()
        ) { tab ->
            when (tab) {
                AppPillarTab.TODAY -> TodayScreen(sessionLogDao, profile, onStartProtocol)
                AppPillarTab.TRAIN -> TrainScreen(onStartProtocol, sessionLogDao, targetColor, targetSpeed)
                AppPillarTab.CHALLENGE -> ChallengeScreen(profile.age)
                AppPillarTab.PROGRESS -> ProgressScreen(sessionLogDao)
                AppPillarTab.PROFILE -> ProfileScreen(
                    age = profile.age,
                    wellnessProfile = profile,
                    themeMode = themeMode,
                    voiceEnabled = voiceEnabled,
                    hapticsEnabled = hapticsEnabled,
                    targetColor = targetColor,
                    targetSpeed = targetSpeed,
                    onAgeChange = onAgeChange,
                    onThemeChange = onThemeChange,
                    onVoiceChange = onVoiceChange,
                    onHapticsChange = onHapticsChange,
                    onTargetColorChange = onTargetColorChange,
                    onTargetSpeedChange = onTargetSpeedChange,
                    onRetakeAssessment = onRetakeAssessment
                )
            }
        }
    }
}
