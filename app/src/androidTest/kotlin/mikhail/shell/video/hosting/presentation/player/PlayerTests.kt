package mikhail.shell.video.hosting.presentation.player

import androidx.activity.ComponentActivity
import androidx.compose.runtime.retain.retain
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.media3.exoplayer.ExoPlayer
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.CountDownLatch

@RunWith(AndroidJUnit4::class)
class PlayerTests {
    @get:Rule
    private val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun player_RegularOperations_RunSmoothly() {
        val latch = CountDownLatch(1)
        composeRule.setContent {
            val context = LocalContext.current
            val player = retain {
                ExoPlayer.Builder(composeRule.activity).build()
            }
            PlayerComponent(
                playerProvider = { player },
                onFullscreen = {
                    latch.countDown()
                }
            )
        }
        latch.await()
    }
}