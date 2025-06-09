package mikhail.shell.video.hosting.domain.services

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.ContentValues
import android.content.Intent
import android.content.pm.ServiceInfo
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.IBinder
import android.provider.MediaStore
import android.webkit.MimeTypeMap
import androidx.core.app.NotificationCompat
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
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

    companion object {
        private var NOTIFICATION_COUNT = 0
        val ROOT_PACKAGE = "mikhail.shell.video.hosting"
        val ACTION_LAUNCH_DOWNLOADING = "$ROOT_PACKAGE.ACTION_LAUNCH_DOWNLOADING"
        val ACTION_CANCEL_DOWNLOADING = "$ROOT_PACKAGE.ACTION_CANCEL_DOWNLOADING"
    }

    @Inject
    lateinit var downloadVideo: DownloadVideo
    private lateinit var notificationManager: NotificationManager
    private var process: Job? = null
    private var uri: Uri? = null
    override fun onCreate() {
        super.onCreate()
        notificationManager = getSystemService(NotificationManager::class.java)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        when (intent?.action) {
            ACTION_LAUNCH_DOWNLOADING -> startDownloading(intent)
            ACTION_CANCEL_DOWNLOADING -> cancelDownloading()
        }
        return START_STICKY
    }

    private fun startDownloading(intent: Intent?) {
        intent?.extras?.getLong("videoId")?.let { videoId ->
            val persistentNotification = createProcessNotification()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                startForeground(
                    ++NOTIFICATION_COUNT,
                    persistentNotification,
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
                )
            } else {
                startForeground(++NOTIFICATION_COUNT, persistentNotification)
            }
            var output: OutputStream? = null
            var progress = 0
            process = coroutineScope.launch {
                downloadVideo(videoId) { mime, size, bytePartition ->
                    if (uri == null) {
                        val extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(mime)
                        val fileName = "$videoId.$extension"
                        val contentValues = ContentValues().apply {
                            put(
                                MediaStore.Video.Media.RELATIVE_PATH,
                                Environment.DIRECTORY_MOVIES + "/" + getString(R.string.app_name)
                            )
                            put(
                                MediaStore.Video.Media.DISPLAY_NAME,
                                fileName
                            )
                        }
                        uri = contentResolver.insert(
                            MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                            contentValues
                        )
                        output = uri?.let { contentResolver.openOutputStream(it) }
                    }
                    output?.write(bytePartition.toByteArray())
                    progress += ((bytePartition.size.toFloat() / size) * 100).roundToInt()
                    val updatedNotification = createProcessNotification(progress)
                    notificationManager.notify(NOTIFICATION_COUNT, updatedNotification)
                }.onSuccess { _ ->
                    onResult(createSuccessNotification())
                    output?.close()
                }.onFailure { _ ->
                    onResult(createFailureNotification())
                    output?.close()
                }
            }
        }
    }

    private fun cancelDownloading() {
        if (process?.isCancelled != true) {
            process?.cancel()
            process = null
        }
        contentResolver.delete(uri!!, null,  null)
        onResult()
    }

    private fun onResult() {
        stopForeground(NOTIFICATION_COUNT)
        stopSelf()
    }

    private fun onResult(notification: Notification) {
        onResult()
        notificationManager.notify(++NOTIFICATION_COUNT, notification)
    }

    private fun createSuccessNotification(): Notification {
        return NotificationCompat.Builder(this, "video_downloading")
            .setContentTitle(getString(R.string.video_download_success))
            .setSmallIcon(R.mipmap.ic_launcher)
            .build()
    }

    private fun createFailureNotification(): Notification {
        return NotificationCompat.Builder(this, "video_downloading")
            .setContentTitle(getString(R.string.video_download_failure))
            .setSmallIcon(R.mipmap.ic_launcher)
            .build()
    }

    private fun createProcessNotification(progress: Int = 0): Notification {
        return NotificationCompat.Builder(this, "video_downloading")
            .setContentTitle(getString(R.string.video_download_progress_title))
            .setContentText(getString(R.string.video_download_progress_message))
            .setSmallIcon(R.mipmap.ic_launcher)
            .setProgress(100, progress, false)
            .setOnlyAlertOnce(true)
            .setOngoing(true)
            .addAction(createCancelAction())
            .build()
    }

    private fun createCancelAction(): NotificationCompat.Action {
        return NotificationCompat.Action(
            R.drawable.ic_launcher_monochrome,
            "Отменить",
            PendingIntent.getService(
                this,
                0,
                Intent(this, VideoDownloadingService::class.java).also {
                    it.action = ACTION_CANCEL_DOWNLOADING
                },
                PendingIntent.FLAG_IMMUTABLE
            )
        )
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}