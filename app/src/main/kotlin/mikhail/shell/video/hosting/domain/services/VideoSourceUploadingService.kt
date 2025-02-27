package mikhail.shell.video.hosting.domain.services

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.annotation.OptIn
import androidx.core.app.NotificationCompat
import androidx.media3.common.util.UnstableApi
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import mikhail.shell.video.hosting.MainActivity
import mikhail.shell.video.hosting.R
import mikhail.shell.video.hosting.di.VideoUploadingEntryPoint
import mikhail.shell.video.hosting.domain.errors.CompoundError
import mikhail.shell.video.hosting.domain.errors.UploadVideoError
import mikhail.shell.video.hosting.domain.models.Video
import mikhail.shell.video.hosting.domain.usecases.videos.UploadVideo

@AndroidEntryPoint
class VideoSourceUploadingService : Service() {
    private lateinit var videoUploadingEntryPoint: VideoUploadingEntryPoint
    private lateinit var _uploadVideo: UploadVideo
    private var NOTIFICATION_COUNT = 0
    private lateinit var notificationManager: NotificationManager
    private val coroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    override fun onCreate() {
        notificationManager = getSystemService(NotificationManager::class.java)
        videoUploadingEntryPoint = EntryPointAccessors.fromApplication(this, VideoUploadingEntryPoint::class.java)
        _uploadVideo = videoUploadingEntryPoint.getUploadVideo()
    }
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val input = intent?.extras
        input?.let {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                startForeground(++NOTIFICATION_COUNT, createProgressNotification(), ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
            } else {
                startForeground(++NOTIFICATION_COUNT, createProgressNotification())
            }
            coroutineScope.launch {
                _uploadVideo(
                    video = Video(
                        channelId = it.getLong("channelId"),
                        title = it.getString("title")!!
                    ),
                    source = it.getString("source")!!,
                    cover = it.getString("cover"),
                ) {
                    val progress = (it * 100).toInt()
                    updateProgressNotification(progress)
                }.onSuccess { vid ->
                    displaySuccessNotification(vid)
                    stopForeground(STOP_FOREGROUND_REMOVE)
                    stopSelf()
                }.onFailure { err ->
                    displayFailureNotification(err)
                    stopForeground(STOP_FOREGROUND_REMOVE)
                    stopSelf()
                }
            }
        }
        return START_REDELIVER_INTENT
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    @OptIn(UnstableApi::class)
    private fun displaySuccessNotification(vid: Video) {
        val deepLinkIntent = Intent(this, MainActivity::class.java).also {
            it.putExtra("videoId", vid.videoId)
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            deepLinkIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val notification = NotificationCompat.Builder(this, "video_uploading")
            .setSmallIcon(R.drawable.icon)
            .setContentTitle("Видео успешно загружено")
            .setContentText("Нажмите, чтобы перейти к нему.")
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
        notificationManager.notify(++NOTIFICATION_COUNT, notification)
    }

    private fun displayFailureNotification(err: CompoundError<UploadVideoError>) {
        val notification = NotificationCompat.Builder(this, "video_uploading")
            .setSmallIcon(R.drawable.icon)
            .setContentTitle("Ошибка при загрузке видео")
            .setContentText("Нажмите и попробуйте ещё раз.")
            .setAutoCancel(true)
            .build()
        notificationManager.notify(++NOTIFICATION_COUNT, notification)
    }

    private fun updateProgressNotification(progress: Int = 0) {
        val notification = createProgressNotification(progress)
        notificationManager.notify(NOTIFICATION_COUNT, notification)
    }
    private fun createProgressNotification(progress: Int = 0): Notification {
        return NotificationCompat.Builder(this, "video_uploading")
            .setSmallIcon(R.drawable.icon)
            .setContentTitle("Видео загружается на сервер")
            .setContentText("Оно появится на канале в течение пары минут.")
            .setSilent(true)
            .setProgress(100, progress, false)
            .setOngoing(true)
            .build()
    }
}