package mikhail.shell.video.hosting.presentation.settings

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("Some sort of text")
            }
        }
        composeTestRule.onNodeWithText("Some sort of text").assertExists()
    }
}