package mikhail.shell.video.hosting.domain.services

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import androidx.annotation.OptIn
import androidx.core.app.NotificationCompat
import androidx.core.net.toUri
import androidx.media3.common.util.UnstableApi
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import mikhail.shell.video.hosting.R
import mikhail.shell.video.hosting.di.NotificationEntryPoint
import mikhail.shell.video.hosting.di.PresentationModule.HOST
import mikhail.shell.video.hosting.domain.models.ActionModel
import mikhail.shell.video.hosting.domain.models.CommentWithUser
import mikhail.shell.video.hosting.domain.providers.UserDetailsProvider
import mikhail.shell.video.hosting.domain.repositories.CommentRepository
import mikhail.shell.video.hosting.domain.usecases.channels.SubscribeToChannelNotifications
import mikhail.shell.video.hosting.presentation.activities.MainActivity

@AndroidEntryPoint
class NotificationService: FirebaseMessagingService() {

    private val coroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var NOTIFICATIONS_COUNT = 0
    lateinit var entryPoint: NotificationEntryPoint
    private lateinit var userDetailsProvider: UserDetailsProvider
    private lateinit var subscribeToChannelNotifications: SubscribeToChannelNotifications
    private lateinit var notificationManager: NotificationManager
    private lateinit var commentRepository: CommentRepository
    private lateinit var fcm: FirebaseMessaging
    private lateinit var gson: Gson

    override fun onCreate() {
        notificationManager = getSystemService(NotificationManager::class.java)
        entryPoint = EntryPointAccessors.fromApplication(this, NotificationEntryPoint::class.java)
        userDetailsProvider = entryPoint.getUserDetailsProvider()
        subscribeToChannelNotifications = entryPoint.getResubscribe()
        commentRepository = entryPoint.getCommentRepository()
        fcm = entryPoint.getFirebaseMessaging()
        gson = entryPoint.getGson()
    }

    override fun onMessageReceived(message: RemoteMessage) {
        val topic = message.from
        val data = message.data
        if (topic?.startsWith(CHANNEL_TOPIC_PREFIX) == true) {
            val videoId = data["videoId"]
            val channelTitle = data["channelTitle"]
            val videoTitle = data["videoTitle"]
            showNotification(videoId!!.toLong(), channelTitle, videoTitle)
        } else if(COMMENTS_TOPIC_REGEX.matches(topic?: "")) {
            val type = object : TypeToken<ActionModel<CommentWithUser>>() {}.type
            val actionModel = gson.fromJson<ActionModel<CommentWithUser>>(data["actionModel"]?: return, type)
            coroutineScope.launch {
                commentRepository.receive(actionModel)
            }
        }
    }

    @OptIn(UnstableApi::class)
    private fun showNotification(videoId: Long, channelTitle: String?, videoTitle: String?) {
        val intent = Intent(this, MainActivity::class.java).apply{
            data = "https://$HOST/videos/$videoId".toUri()
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val notification = NotificationCompat.Builder(this, "channel_subscriptions")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(getString(R.string.new_video_title, channelTitle))
            .setContentText(videoTitle)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
        notificationManager.notify(++NOTIFICATIONS_COUNT, notification)
    }

    override fun onNewToken(token: String) {
        val userId = userDetailsProvider.getUserId()
        if (userId != 0L) {
            coroutineScope.launch {
                subscribeToChannelNotifications(userId)
            }
        }
    }

    private inline fun <reified T> String.fromJson(): T {
        return gson.fromJson(this, T::class.java)
    }

    private fun resolveTemplate(template: String, map: Map<String, Any>): String {
        var result = template
        for ((key, value) in map.entries) {
            result = result.replace("{$key}", value.toString())
        }
        return result
    }

    private companion object {
        const val CHANNEL_TOPIC_PREFIX = "/topics/channels"
        const val COMMENTS_TOPIC_TEMPLATE = "videos.{video_id}.comments"
        val COMMENTS_TOPIC_REGEX = "^/topics/videos\\..+\\.comments$".toRegex()
    }
}