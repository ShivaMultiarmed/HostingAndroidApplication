package mikhail.shell.video.hosting

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
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
        ImageLoader.Builder(this)
            .okHttpClient(httpClient)
            .build()
    }
    override fun onCreate() {
        super.onCreate()
        Coil.setImageLoader(imageLoader)
        val notificationManager = getSystemService(NotificationManager::class.java)
        val videoUploadingChannel = NotificationChannel(
            "video_uploading",
            getString(R.string.nc_video_uploading),
            NotificationManager.IMPORTANCE_DEFAULT
        )
        notificationManager.createNotificationChannel(videoUploadingChannel)

        val videoDownloadingChannel = NotificationChannel(
            "video_downloading",
            getString(R.string.nc_video_downloading),
            NotificationManager.IMPORTANCE_DEFAULT
        )
        notificationManager.createNotificationChannel(videoDownloadingChannel)

        val channelSubscriptionChannel = NotificationChannel(
            "channel_subscriptions",
            getString(R.string.nc_channel_subscriptions),
            NotificationManager.IMPORTANCE_DEFAULT
        )
        notificationManager.createNotificationChannel(channelSubscriptionChannel)
    }
}