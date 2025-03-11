package mikhail.shell.video.hosting.domain.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.edit
import androidx.test.core.app.ApplicationProvider
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.ServiceTestRule
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import kotlinx.coroutines.runBlocking
import mikhail.shell.video.hosting.domain.services.VideoDownloadingService
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test


//@HiltAndroidTest
////@RunWith(AndroidJUnit4::class)
//class VideoDownloadingTests {
//    @get:Rule
//    var hiltRule = HiltAndroidRule(this)
//    @Inject
//    lateinit var downloadVideo: DownloadVideo
//    @Before
//    fun before() {
//        hiltRule.inject()
//    }
//    @Test
//    fun downloadVideoPart_existingId_success() = runBlocking {
//        val latch = CountDownLatch(0)
//        val context = ApplicationProvider.getApplicationContext<HiltTestApplication>()
//        val videoId = 6L
//        Intent(context, VideoDownloadingService::class.java).also {
//            it.action = VideoDownloadingService.ACTION_LAUNCH_DOWNLOADING
//            it.putExtra("videoId", videoId)
//            context.startService(it)
//        }
//        val result = latch.await(100, TimeUnit.SECONDS)
//    }
//}

@HiltAndroidTest
class VideoDownloadingTests {
    private lateinit var uiDevice: UiDevice
    @get:Rule
    var hiltRule = HiltAndroidRule(this)
    @get:Rule
    private var serviceTestRule = ServiceTestRule()
    @Before
    fun initialize() {
        hiltRule.inject()
        uiDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        uiDevice.openNotification()
    }
    @Test
    fun videoId_launching_failure() = runBlocking {
        val videoId = 6L
        val context = ApplicationProvider.getApplicationContext<HiltTestApplication>()

        val notificationManager = context.getSystemService(NotificationManager::class.java)
        val videoDownloadingChannel = NotificationChannel(
            "video_downloading",
            "Скачивание видео",
            NotificationManager.IMPORTANCE_DEFAULT
        )
        notificationManager.createNotificationChannel(videoDownloadingChannel)

        Intent(context, VideoDownloadingService::class.java).also {
            it.action = VideoDownloadingService.ACTION_LAUNCH_DOWNLOADING
            it.putExtra("videoId", videoId)
            context.startService(it)
        }
        val selector = By.text("Ошибка при скачивании видео")
        uiDevice.wait(Until.hasObject(selector), 60000)
        val notification = uiDevice.findObject(selector)
        assertNotNull(notification)
    }
    @Test
    fun videoId_launching_success() {
        val videoId = 6L
        val context = ApplicationProvider.getApplicationContext<HiltTestApplication>()
        val sharedPreferences = context.getSharedPreferences("user_details", Context.MODE_PRIVATE)
        sharedPreferences.edit(true) {
            putString("token", "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiI2IiwiaWF0IjoxNzQxNzAyNTAyfQ.Mw9JjCLjLdRL7E8D0D9iI_oLT_PCIXDtAGbrU1w-D6k")
        }

        context.grantUriPermission(
            "com.android.providers.media.MediaProvider",
            Uri.parse("content://media/external/video/media"),
            Intent.FLAG_GRANT_WRITE_URI_PERMISSION
        )

        val notificationManager = context.getSystemService(NotificationManager::class.java)
        val videoDownloadingChannel = NotificationChannel(
            "video_downloading",
            "Скачивание видео",
            NotificationManager.IMPORTANCE_DEFAULT
        )
        notificationManager.createNotificationChannel(videoDownloadingChannel)

        Intent(context, VideoDownloadingService::class.java).also {
            it.action = VideoDownloadingService.ACTION_LAUNCH_DOWNLOADING
            it.putExtra("videoId", videoId)
            it.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            context.startService(it)
        }
        val selector = By.text("Ошибка при скачивании видео")
        uiDevice.wait(Until.hasObject(selector), 60000)
        val notification = uiDevice.findObject(selector)
        assertNotNull(notification)
    }
}