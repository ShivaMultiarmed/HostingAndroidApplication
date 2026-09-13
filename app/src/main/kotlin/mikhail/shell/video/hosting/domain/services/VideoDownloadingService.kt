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
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import mikhail.shell.video.hosting.R
import mikhail.shell.video.hosting.di.VideoDownloadingEntryPoint
import mikhail.shell.video.hosting.domain.managers.NotificationOrchestrator
import mikhail.shell.video.hosting.domain.models.dataOrNull
import mikhail.shell.video.hosting.domain.usecases.videos.DownloadVideo
import mikhail.shell.video.hosting.domain.usecases.videos.GetVideo
import java.io.OutputStream
import javax.inject.Inject
import kotlin.math.roundToInt

@AndroidEntryPoint
class VideoDownloadingService : Service() {
    private val coroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private lateinit var entryPoint: VideoDownloadingEntryPoint
    private lateinit var notificationOrchestrator: NotificationOrchestrator
    private var NOTIFICATION_ID: Int? = null

    companion object {
        private const val ROOT_PACKAGE = "mikhail.shell.video.hosting"
        const val ACTION_LAUNCH_DOWNLOADING = "$ROOT_PACKAGE.ACTION_LAUNCH_DOWNLOADING"
        const val ACTION_CANCEL_DOWNLOADING = "$ROOT_PACKAGE.ACTION_CANCEL_DOWNLOADING"
    }

    @Inject
    lateinit var downloadVideo: DownloadVideo

    @Inject
    lateinit var getVideo: GetVideo
    private lateinit var notificationManager: NotificationManager
    private var downloadJob: Job? = null
    private var uri: Uri? = null
    override fun onCreate() {
        super.onCreate()
        entryPoint =
            EntryPointAccessors.fromApplication(this, VideoDownloadingEntryPoint::class.java)
        notificationOrchestrator = entryPoint.getNotificationOrchestrator()
        NOTIFICATION_ID = notificationOrchestrator.incrementId()
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
            downloadJob = coroutineScope.launch {
                try {
                    val title = getVideo(videoId).dataOrNull()?.title
                    if (title == null) {
                        cancelDownloading()
                        return@launch
                    }

                    val persistentNotification = createProcessNotification(title)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        startForeground(
                            NOTIFICATION_ID!!,
                            persistentNotification,
                            ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
                        )
                    } else {
                        startForeground(NOTIFICATION_ID!!, persistentNotification)
                    }
                    var output: OutputStream? = null
                    var progress = 0

                    downloadVideo(videoId) { mime, size, bytePartition ->
                        if (uri == null) {
                            val extension =
                                MimeTypeMap.getSingleton().getExtensionFromMimeType(mime)
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
                        output?.write(bytePartition)
                        progress += ((bytePartition.size.toFloat() / size) * 100).roundToInt()
                        val updatedNotification = createProcessNotification(title, progress)
                        notificationManager.notify(NOTIFICATION_ID!!, updatedNotification)
                    }.onSuccess { _ ->
                        onResult(createSuccessNotification(title))
                    }.onFailure { _ ->
                        onResult(createFailureNotification(title))
                    }
                    output?.close()
                } catch (_: CancellationException) {
                }
            }
        }
    }

    private fun cancelDownloading() {
        if (downloadJob?.isCancelled != true) {
            downloadJob?.cancel()
        }
        if (uri != null) {
            contentResolver.delete(uri!!, null, null)
        }
        onResult()
    }

    private fun onResult() {
        stopForeground(NOTIFICATION_ID!!)
        stopSelf()
    }

    private fun onResult(notification: Notification) {
        onResult()
        notificationManager.notify(NOTIFICATION_ID!!, notification)
    }

    private fun createSuccessNotification(title: String): Notification {
        return NotificationCompat.Builder(this, "video_downloading")
            .setContentTitle(getString(R.string.video_download_success))
            .setContentText(title)
            .setSmallIcon(R.mipmap.ic_launcher)
            .build()
    }

    private fun createFailureNotification(title: String): Notification {
        return NotificationCompat.Builder(this, "video_downloading")
            .setContentTitle(getString(R.string.video_download_failure))
            .setContentText(title)
            .setSmallIcon(R.mipmap.ic_launcher)
            .build()
    }

    private fun createProcessNotification(title: String, progress: Int = 0): Notification {
        return NotificationCompat.Builder(this, "video_downloading")
            .setContentTitle(getString(R.string.video_download_progress_title))
            .setContentText(title)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setProgress(100, progress, false)
            .setOnlyAlertOnce(true)
            .setOngoing(true)
            .addAction(createCancelAction())
            .build()
    }

    private fun createCancelAction(): NotificationCompat.Action = NotificationCompat.Action(
        R.drawable.ic_launcher_monochrome,
        getString(R.string.cancel_button),
        PendingIntent.getService(
            this,
            0,
            Intent(this, VideoDownloadingService::class.java).also {
                it.action = ACTION_CANCEL_DOWNLOADING
            },
            PendingIntent.FLAG_IMMUTABLE
        )
    )

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}