package com.hello.android.ui.screens

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertExists
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.waitUntilDoesNotExist
import androidx.compose.ui.test.waitUntilExists
import androidx.compose.ui.test.onNodeWithTag
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
class HomeScreenTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun setup() {
        hiltRule.inject()
    }

    @Test
    fun homeScreen_showsHelloWorld_andLoadPostsButton() {
        composeTestRule.onNodeWithText("Hello World!").assertIsDisplayed()
        composeTestRule.onNodeWithText("Load Posts").assertIsDisplayed()
        composeTestRule.onNodeWithText("Load Posts").assertIsEnabled()
    }

    @Test
    fun homeScreen_loadPostsButton_triggersLoading() {
        composeTestRule.onNodeWithText("Load Posts").performClick()
        // Button should become disabled during loading
        composeTestRule.onNodeWithText("Load Posts").assertIsNotEnabled()
    }
}
