package mikhail.shell.video.hosting

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.provider.Settings
import coil.Coil
import coil.ImageLoader
import dagger.hilt.android.HiltAndroidApp
import okhttp3.OkHttpClient
import javax.inject.Inject

@HiltAndroidApp
class HostingApplication: Application() {
    @Inject
    lateinit var httpClient: OkHttpClient
    private val imageLoader: ImageLoader by lazy {
        ImageLoader.Builder(this).okHttpClient(httpClient).build()
    }
    override fun onCreate() {
        super.onCreate()
        Coil.setImageLoader(
            imageLoader
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val videoUploadingChannel = NotificationChannel(
                "video_uploading",
                "Загрузка видео",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(videoUploadingChannel)
        }
    }
}