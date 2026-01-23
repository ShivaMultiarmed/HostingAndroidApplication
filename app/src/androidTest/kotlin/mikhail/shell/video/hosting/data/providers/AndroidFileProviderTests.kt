package mikhail.shell.video.hosting.data.providers

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import mikhail.shell.video.hosting.domain.providers.FileProvider
import mikhail.shell.video.hosting.test.R
import org.junit.Assert
import org.junit.Before
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class AndroidFileProviderTests {
    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var fileProvider: FileProvider

    val appContext = ApplicationProvider.getApplicationContext<HiltTestApplication>()

    @Before
    fun initialize() {
        hiltRule.inject()
    }
    @Test
    @Ignore("No valid file is available for the test")
    fun get_ValidFile_ReturnsUnit() {
        val uri = "android.resource://${appContext.packageName}/${R.drawable.test}"
        val file = fileProvider.get(uri)
        Assert.assertNotNull(
            "The file is null",
            file
        )
//        Assert.assertTrue(
//            "Not an image",
//            file!!.mimeType.startsWith("image")
//        )
    }
}