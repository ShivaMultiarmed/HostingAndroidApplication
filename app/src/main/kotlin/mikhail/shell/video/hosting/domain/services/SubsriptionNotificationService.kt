package mikhail.shell.video.hosting.domain.services

import android.app.NotificationManager
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
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
        val channelTitle = data["channelTitle"]
        val videoTitle = data["videoTitle"]
        val notification = NotificationCompat.Builder(this, "channel_subscriptions")
            .setContentText(channelTitle)
            .setContentText(videoTitle)
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