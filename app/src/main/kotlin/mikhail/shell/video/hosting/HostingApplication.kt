package mikhail.shell.video.hosting

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
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
            val notificationManager = getSystemService(NotificationManager::class.java)
            val videoUploadingChannel = NotificationChannel(
                "video_uploading",
                "Загрузка видео",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(videoUploadingChannel)

            val videoDownloadingChannel = NotificationChannel(
                "video_downloading",
                "Скачивание видео",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(videoDownloadingChannel)

            val channelSubscriptionChannel = NotificationChannel(
                "channel_subscriptions",
                "Подписки на канал",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channelSubscriptionChannel)
        }
    }
}