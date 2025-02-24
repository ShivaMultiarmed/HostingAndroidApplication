package mikhail.shell.video.hosting.domain.services

import android.app.Notification
import android.app.NotificationManager
import android.app.Service
import android.content.ContentValues
import android.content.Intent
import android.content.pm.ServiceInfo
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.provider.MediaStore
import android.webkit.MimeTypeMap
import androidx.core.app.NotificationCompat
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import mikhail.shell.video.hosting.R
import mikhail.shell.video.hosting.domain.usecases.videos.DownloadVideo
import java.io.OutputStream
import javax.inject.Inject
import kotlin.math.roundToInt

@AndroidEntryPoint
class VideoDownloadingService : Service() {
    private val coroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private companion object {
        var NOTIFICATION_COUNT = 0
    }
    @Inject
    lateinit var downloadVideo: DownloadVideo
    private lateinit var notificationManager: NotificationManager
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        notificationManager = getSystemService(NotificationManager::class.java)
        val persistentNotification = createProcessNotification()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(++NOTIFICATION_COUNT, persistentNotification, ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
        } else {
            startForeground(++NOTIFICATION_COUNT, persistentNotification)
        }
        intent?.extras?.getLong("videoId")?.let { videoId ->
            var uri: Uri? = null
            var output: OutputStream? = null
            var progress = 0
            coroutineScope.launch {
                    downloadVideo(videoId) { mime, size, bytePartition ->
                        if (uri == null) {
                            val extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(mime)
                            val fileName = "$videoId.$extension"
                            val contentValues = ContentValues().apply {
                                put(MediaStore.Video.Media.DISPLAY_NAME, fileName)
                            }
                            uri = contentResolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, contentValues)
                            output = uri?.let { contentResolver.openOutputStream(it) }
                        }
                        output?.write(bytePartition.toByteArray())
                        progress += ((bytePartition.size.toFloat() / size) * 100).roundToInt()
                        val updatedNotification = createProcessNotification(progress)
                        notificationManager.notify(NOTIFICATION_COUNT, updatedNotification)
                    }.onSuccess { _ ->
                        onResult(createSuccessNotification())
                        output?.close()
                    }.onFailure { err ->
                        onResult(createFailureNotification())
                        output?.close()
                    }
            }
        }
        return START_STICKY
    }

    private fun onResult(notification: Notification) {
        stopForeground(NOTIFICATION_COUNT)
        notificationManager.notify(++NOTIFICATION_COUNT, notification)
        stopSelf()
    }

    private fun createSuccessNotification(): Notification {
        return NotificationCompat.Builder(this, "video_downloading")
            .setContentTitle("Видео успешно скачано")
            .setSmallIcon(R.drawable.icon)
            .build()
    }

    private fun createFailureNotification(): Notification {
        return NotificationCompat.Builder(this, "video_downloading")
            .setContentTitle("Ошибка при скачивании видео")
            .setSmallIcon(R.drawable.icon)
            .build()
    }

    private fun createProcessNotification(progress: Int = 0): Notification {
        return NotificationCompat.Builder(this, "video_downloading")
            .setContentTitle("Скачивание видео")
            .setContentText("Видео скачается в течение нескольких минут.")
            .setSmallIcon(R.drawable.icon)
            .setProgress(100, progress, false)
            .setOnlyAlertOnce(true)
            .setOngoing(true)
            .build()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}