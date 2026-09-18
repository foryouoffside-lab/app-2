package com.example

import android.content.Context
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.AppDatabase
import com.example.model.ScreenTimeBand
import com.example.model.VisionCorrection
import com.example.model.WellnessProfile
import com.example.model.WellnessSymptom
import com.example.ui.AppPillarTab
import com.example.ui.MainAppContainer
import com.example.ui.theme.EyeRestTheme
import com.example.util.TargetColor
import com.example.util.ThemeMode
import com.example.util.BreakReminderSettings
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.After
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [36])
class TodayScreenScreenshotTest {

    @get:Rule val composeTestRule = createComposeRule()

    private val database = Room.inMemoryDatabaseBuilder(
        ApplicationProvider.getApplicationContext<Context>(),
        AppDatabase::class.java
    ).allowMainThreadQueries().build()

    @After
    fun closeDatabase() = database.close()

    @Test
    fun personalizedPlan_homeScreenshot() {
        val profile = WellnessProfile(
            age = 30,
            screenTime = ScreenTimeBand.FOUR_TO_EIGHT,
            correction = VisionCorrection.GLASSES,
            symptoms = setOf(
                WellnessSymptom.DRY_GRITTY,
                WellnessSymptom.TIRED_STRAIN,
                WellnessSymptom.TENSION_HEADACHE
            ),
            clinicalContexts = emptySet(),
            urgentSymptoms = false
        )

        composeTestRule.setContent {
            EyeRestTheme(themeMode = ThemeMode.DARK) {
                MainAppContainer(
                    sessionLogDao = database.sessionLogDao(),
                    profile = profile,
                    themeMode = ThemeMode.DARK,
                    voiceEnabled = true,
                    hapticsEnabled = true,
                    trueBlackEnabled = false,
                    targetColor = TargetColor.AMBER,
                    targetSpeed = 1f,
                    breakReminderSettings = BreakReminderSettings(),
                    onAgeChange = {},
                    onThemeChange = {},
                    onVoiceChange = {},
                    onHapticsChange = {},
                    onTrueBlackChange = {},
                    onTargetColorChange = {},
                    onTargetSpeedChange = {},
                    onBreakReminderSettingsChange = {},
                    onRetakeAssessment = {},
                    onStartProtocol = {},
                    activeTab = AppPillarTab.TODAY,
                    onTabChange = {}
                )
            }
        }

        composeTestRule.onRoot().captureRoboImage(
            filePath = "src/test/screenshots/today-home.png"
        )
    }
}
