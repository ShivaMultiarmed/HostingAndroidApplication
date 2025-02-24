package mikhail.shell.video.hosting.domain.services

import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.ServiceTestRule
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import kotlinx.coroutines.runBlocking
import mikhail.shell.video.hosting.HostingApplication
import mikhail.shell.video.hosting.domain.services.VideoDownloadingService
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class VideoDownloadingTests {
    private lateinit var uiDevice: UiDevice
    @get:Rule
    private val serviceTestRule = ServiceTestRule()
    @Before
    fun initialize() {
        uiDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        uiDevice.openNotification()
    }
    @Test
    fun videoId_launching_successNotification() = runBlocking {
        val videoId = 6
        val context = ApplicationProvider.getApplicationContext<HostingApplication>()
        Intent(context, VideoDownloadingService::class.java).also {
            it.putExtra("videoId", videoId)
            context.startService(it)
        }
        val selector = By.text("Видео успешно скачано")
        uiDevice.wait(Until.hasObject(selector), 60000)
        val notification = uiDevice.findObject(By.text("Успешно"))
        assertNotNull(notification)
    }
}