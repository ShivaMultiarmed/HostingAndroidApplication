package mikhail.shell.video.hosting.presentation.settings

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@HiltAndroidTest
class SettingsScreenTests {
    @get:Rule(order = 0)
    var hiltAndroidRule = HiltAndroidRule(this)
    @get:Rule(order = 1)
    var composeTestRule = createComposeRule()
    @Before
    fun initialize () {
        hiltAndroidRule.inject()
    }
    @Test
    fun testSomeScreen() {
        composeTestRule.setContent {
            SettingsScreen()
        }
        composeTestRule.onNodeWithText("Язык").assertExists()
    }
}