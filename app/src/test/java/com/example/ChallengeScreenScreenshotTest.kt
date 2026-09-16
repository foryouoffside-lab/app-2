package com.example

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import com.example.ui.screens.ChallengeScreen
import com.example.ui.theme.EyeRestTheme
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [36])
class ChallengeScreenScreenshotTest {

    @get:Rule val composeTestRule = createComposeRule()

    @Test
    fun `challenges and tests library fits a phone`() {
        composeTestRule.setContent { EyeRestTheme { ChallengeScreen(age = 30) } }
        composeTestRule.onRoot().captureRoboImage(
            filePath = "src/test/screenshots/challenges-and-tests.png"
        )
    }

    @Test
    fun `generated colour plate is usable on a phone`() {
        composeTestRule.setContent { EyeRestTheme { ChallengeScreen(age = 30) } }
        composeTestRule.onNodeWithTag("challenge_ishihara_style_plates").performScrollTo().performClick()
        composeTestRule.onNodeWithText("Begin").performScrollTo().performClick()
        composeTestRule.onRoot().captureRoboImage(
            filePath = "src/test/screenshots/ishihara-style-plate.png"
        )
    }
}
