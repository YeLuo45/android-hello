package com.hello.android.ui.screens

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertEquals
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.hello.android.MainActivity
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class CounterScreenTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun setup() {
        hiltRule.inject()
    }

    @Test
    fun counterScreen_incrementIncreasesCount() {
        // Navigate to Counter Screen via BottomNavigation
        composeTestRule.onNodeWithText("Counter").performClick()

        val initialCount = composeTestRule.onNodeWithText("0")
        initialCount.assertIsDisplayed()

        // Click +1 button
        composeTestRule.onNodeWithText("+1").performClick()
        composeTestRule.onNodeWithText("1").assertIsDisplayed()

        // Click +1 again
        composeTestRule.onNodeWithText("+1").performClick()
        composeTestRule.onNodeWithText("2").assertIsDisplayed()
    }

    @Test
    fun counterScreen_resetSetsCountToZero() {
        // Navigate to Counter Screen
        composeTestRule.onNodeWithText("Counter").performClick()

        // Increment a few times
        composeTestRule.onNodeWithText("+1").performClick()
        composeTestRule.onNodeWithText("+1").performClick()
        composeTestRule.onNodeWithText("+1").performClick()

        // Verify count is 3
        composeTestRule.onNodeWithText("3").assertIsDisplayed()

        // Click Reset
        composeTestRule.onNodeWithText("Reset").performClick()

        // Verify count is 0
        composeTestRule.onNodeWithText("0").assertIsDisplayed()
    }
}
