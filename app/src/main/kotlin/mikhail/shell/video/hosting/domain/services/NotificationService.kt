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
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import mikhail.shell.video.hosting.R
import mikhail.shell.video.hosting.di.NotificationEntryPoint
import mikhail.shell.video.hosting.di.PresentationModule.HOST
import mikhail.shell.video.hosting.domain.errors.Error
import mikhail.shell.video.hosting.domain.errors.FileError
import mikhail.shell.video.hosting.domain.errors.UnexpectedError
import mikhail.shell.video.hosting.domain.errors.network.NetworkError
import mikhail.shell.video.hosting.domain.usecases.user.SubscribeToNotifications
import mikhail.shell.video.hosting.domain.usecases.user.ObserveUserDetails
import mikhail.shell.video.hosting.domain.validation.getNetworkErrorMessage
import mikhail.shell.video.hosting.presentation.activities.MainActivity

@AndroidEntryPoint
class NotificationService: FirebaseMessagingService() {

    private val coroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var NOTIFICATIONS_COUNT = 0
    lateinit var entryPoint: NotificationEntryPoint
    private lateinit var observeUserDetails: ObserveUserDetails
    private lateinit var subscribeToNotifications: SubscribeToNotifications
    private lateinit var notificationManager: NotificationManager
    private lateinit var fcm: FirebaseMessaging
    private lateinit var gson: Gson

    override fun onCreate() {
        notificationManager = getSystemService(NotificationManager::class.java)
        entryPoint = EntryPointAccessors.fromApplication(this, NotificationEntryPoint::class.java)
        observeUserDetails = entryPoint.observeUserDetails()
        subscribeToNotifications = entryPoint.getResubscribe()
        fcm = entryPoint.getFirebaseMessaging()
        gson = entryPoint.getGson()
    }

    override fun onMessageReceived(message: RemoteMessage) {
        if (observeUserDetails().value.userId == 0L) {
            return
        }
        val topic = message.from
        val data = message.data
        if (topic?.contains("subscribers") == true) {
            val videoId = data["video_id"]!!.toLong()
            val channelTitle = data["channel_title"]!!
            val videoTitle = data["video_title"]!!
            postNewVideoNotification(videoId, channelTitle, videoTitle)
        } else if (topic?.contains("uploads") == true) {
            if (data.contains("source_error")) {
                val sourceError =
                    try {
                        FileError.valueOf(data["source_error"]!!)
                    } catch (_: Exception) {
                        UnexpectedError
                    }
                postVideoUploadFailureNotification(sourceError)
            } else {
                val videoId = data["video_id"]!!.toLong()
                val channelTitle = data["channel_title"]!!
                val videoTitle = data["video_title"]!!
                postVideoUploadSuccessNotification(videoId, videoTitle)
            }
        }
    }

    @OptIn(UnstableApi::class)
    private fun postNewVideoNotification(
        videoId: Long,
        channelTitle: String,
        videoTitle: String
    ) {
        val intent = Intent(this, MainActivity::class.java).apply{
            data = "https://$HOST/videos/$videoId".toUri()
        }
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        val notification = NotificationCompat.Builder(this, "channel_subscriptions")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(getString(R.string.new_video_title, channelTitle))
            .setContentText(videoTitle)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
        notificationManager.notify(++NOTIFICATIONS_COUNT, notification)
    }

    @OptIn(UnstableApi::class)
    private fun postVideoUploadSuccessNotification(
        videoId: Long,
        videoTitle: String
    ) {
        val intent = Intent(this, MainActivity::class.java).apply{
            data = "https://$HOST/videos/$videoId".toUri()
        }
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        val notification = NotificationCompat.Builder(this, "channel_subscriptions")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(getString(R.string.video_upload_success))
            .setContentText(videoTitle)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
        notificationManager.notify(++NOTIFICATIONS_COUNT, notification)
    }

    private fun postVideoUploadFailureNotification(error: Error) {
        val errorText = when (error) {
            is NetworkError -> getNetworkErrorMessage(error)
            else -> getString(R.string.unexpected_error)
        }
        val notification = NotificationCompat.Builder(this, "video_uploading")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(getString(R.string.video_upload_failure))
            .setContentText(errorText)
            .setAutoCancel(true)
            .build()
        notificationManager.notify(++NOTIFICATIONS_COUNT, notification)
    }

    override fun onNewToken(token: String) {
        coroutineScope.launch {
            subscribeToNotifications()
        }
    }

    override fun onDestroy() {
        coroutineScope.cancel()
        super.onDestroy()
    }

}