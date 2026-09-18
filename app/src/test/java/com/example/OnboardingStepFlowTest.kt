package com.example

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.example.model.ScreenTimeBand
import com.example.model.WellnessProfile
import com.example.ui.screens.ONBOARDING_REVIEW_STEP
import com.example.ui.screens.OnboardingScreen
import com.example.ui.theme.EyeRestTheme
import com.example.util.AgeRange
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** The intake asks one question per screen; nothing else may be on screen with it. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class OnboardingStepFlowTest {

    @get:Rule
    val compose = createComposeRule()

    private var saved: WellnessProfile? = null

    /** The test viewport is short, so only options near the top of a card are tappable. */
    private val pickedBand = ScreenTimeBand.UNDER_TWO
    private val pickedAgeRange = AgeRange.AGE_18_24

    private fun start() {
        compose.setContent {
            EyeRestTheme { OnboardingScreen(onCompleted = { saved = it }) }
        }
    }

    private fun awaitText(text: String) = compose.waitUntil(5_000) {
        compose.onAllNodesWithText(text).fetchSemanticsNodes().isNotEmpty()
    }

    @Test
    fun `only the current question is on screen`() {
        start()
        compose.onNodeWithText("What is your age?").assertIsDisplayed()
        compose.onNodeWithText("How much screen time on most days?").assertDoesNotExist()

        compose.onNodeWithTag("onboarding_continue").performClick()
        compose.onNodeWithText("How much screen time on most days?").assertIsDisplayed()
        compose.onNodeWithText("What is your age?").assertDoesNotExist()
    }

    @Test
    fun `answering a single-choice question advances by itself`() {
        start()
        compose.onNodeWithText(pickedAgeRange.label).performClick()
        awaitText("How much screen time on most days?")
        compose.onNodeWithText(pickedBand.label).performClick()
        awaitText("What vision correction do you use?")
    }

    @Test
    fun `walking every step saves the answers picked along the way`() {
        start()
        compose.onNodeWithTag("onboarding_continue").performClick()   // Q1 age: keep default
        compose.onNodeWithText(pickedBand.label).performClick()       // Q2 advances by itself
        awaitText("What vision correction do you use?")
        repeat(ONBOARDING_REVIEW_STEP - 2) { compose.onNodeWithTag("onboarding_continue").performClick() }

        compose.onNodeWithText("YOUR ANSWERS").assertIsDisplayed()
        compose.onNodeWithTag("onboarding_continue").performClick()

        assertNotNull("walking the whole flow must save a profile", saved)
        assertEquals(30, saved!!.age)
        assertEquals(pickedBand, saved!!.screenTime)
    }
}
