package com.hello.android.ui.screens

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
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
class DiscoverScreenTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun setup() {
        hiltRule.inject()
    }

    @Test
    fun discoverScreen_showsDiscoverMoreAndComingSoon() {
        // Note: DiscoverScreen is not integrated in main navigation yet
        // This test verifies the screen content when accessed directly
        composeTestRule.onNodeWithText("发现更多内容").assertIsDisplayed()
        composeTestRule.onNodeWithText("功能开发中...").assertIsDisplayed()
    }
}
