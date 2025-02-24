package mikhail.shell.video.hosting.data

import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.runBlocking
import mikhail.shell.video.hosting.data.api.VideoApi
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import javax.inject.Inject

@HiltAndroidTest
@RunWith(RobolectricTestRunner::class)
class VideoDownloadingTests {
    @get:Rule
    private val hiltRule = HiltAndroidRule(this)
    @Inject
    lateinit var videoApi: VideoApi
    @Before
    fun before () {
        hiltRule.inject()
    }
    @Test
    fun downloadVideoPart_existingId_success() = runBlocking {
        val videoId = 6L
        val partSize = 10 * 1024 * 1024
        val response = videoApi.downloadVideo(videoId, "bytes=0-${partSize - 1}")
        //Assert.assertNotEquals(response.size, 0)
    }
}