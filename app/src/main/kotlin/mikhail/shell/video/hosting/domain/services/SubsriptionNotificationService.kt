package mikhail.shell.video.hosting.domain.services

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import androidx.annotation.OptIn
import androidx.core.app.NotificationCompat
import androidx.media3.common.util.UnstableApi
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import mikhail.shell.video.hosting.presentation.activities.MainActivity
import mikhail.shell.video.hosting.R
import mikhail.shell.video.hosting.di.ChannelNotificationEntryPoint
import mikhail.shell.video.hosting.domain.providers.UserDetailsProvider
import mikhail.shell.video.hosting.domain.usecases.channels.Resubscribe

@AndroidEntryPoint
class SubsriptionNotificationService: FirebaseMessagingService() {

    private val coroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var NOTIFICATIONS_COUNT = 0
    lateinit var entryPoint: ChannelNotificationEntryPoint
    private lateinit var userDetailsProvider: UserDetailsProvider
    private lateinit var resubscribe: Resubscribe
    private lateinit var notificationManager: NotificationManager

    override fun onCreate() {
        notificationManager = getSystemService(NotificationManager::class.java)
        entryPoint = EntryPointAccessors.fromApplication(this, ChannelNotificationEntryPoint::class.java)
        userDetailsProvider = entryPoint.getUserDetailsProvider()
        resubscribe = entryPoint.getResubscribe()
    }

    override fun onMessageReceived(message: RemoteMessage) {
        val data = message.data
        val videoId = data["videoId"]
        val channelTitle = data["channelTitle"]
        val videoTitle = data["videoTitle"]
        showNotification(videoId!!.toLong(), channelTitle, videoTitle)
    }

    @OptIn(UnstableApi::class)
    private fun showNotification(videoId: Long, channelTitle: String?, videoTitle: String?) {
        val intent = Intent(this, MainActivity::class.java).also {
            it.putExtra("videoId", videoId)
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val notification = NotificationCompat.Builder(this, "channel_subscriptions")
            .setSmallIcon(R.drawable.icon)
            .setContentTitle("Новое видео на канале $channelTitle")
            .setContentText(videoTitle)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
        notificationManager.notify(++NOTIFICATIONS_COUNT, notification)
    }

    override fun onNewToken(token: String) {
        val userId = userDetailsProvider.getUserId()
        coroutineScope.launch {
            resubscribe(userId)
        }
    }
}