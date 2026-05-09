package com.hello.android.ui.screens

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.assertIsEnabled
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
class SettingsScreenTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun setup() {
        hiltRule.inject()
    }

    @Test
    fun settingsScreen_showsSettingsTitle() {
        // Navigate to Settings via BottomNavigation
        composeTestRule.onNodeWithText("Settings").performClick()
        composeTestRule.onNodeWithText("Settings").assertIsDisplayed()
    }

    @Test
    fun settingsScreen_showsAppearanceSection() {
        composeTestRule.onNodeWithText("Settings").performClick()
        composeTestRule.onNodeWithText("Appearance").assertIsDisplayed()
    }

    @Test
    fun settingsScreen_showsLanguageSection() {
        composeTestRule.onNodeWithText("Settings").performClick()
        composeTestRule.onNodeWithText("Language").assertIsDisplayed()
    }

    @Test
    fun settingsScreen_showsAboutSection() {
        composeTestRule.onNodeWithText("Settings").performClick()
        composeTestRule.onNodeWithText("About").assertIsDisplayed()
    }

    @Test
    fun settingsScreen_showsThemeOptions() {
        composeTestRule.onNodeWithText("Settings").performClick()
        composeTestRule.onNodeWithText("Light").assertIsDisplayed()
        composeTestRule.onNodeWithText("Dark").assertIsDisplayed()
        composeTestRule.onNodeWithText("System").assertIsDisplayed()
    }

    @Test
    fun settingsScreen_showsDynamicColorToggle() {
        composeTestRule.onNodeWithText("Settings").performClick()
        composeTestRule.onNodeWithText("Dynamic Color").assertIsDisplayed()
    }
}
