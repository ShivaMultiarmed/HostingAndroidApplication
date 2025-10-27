package mikhail.shell.video.hosting.domain.services

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.net.Uri
import android.os.Build
import android.os.IBinder
import androidx.annotation.OptIn
import androidx.core.app.NotificationCompat
import androidx.core.net.toUri
import androidx.media3.common.util.UnstableApi
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.launch
import mikhail.shell.video.hosting.R
import mikhail.shell.video.hosting.di.PresentationModule.HOST
import mikhail.shell.video.hosting.di.VideoUploadingEntryPoint
import mikhail.shell.video.hosting.domain.errors.Error
import mikhail.shell.video.hosting.domain.errors.network.NetworkError
import mikhail.shell.video.hosting.domain.models.Video
import mikhail.shell.video.hosting.domain.usecases.videos.ConfirmVideoUpload
import mikhail.shell.video.hosting.domain.usecases.videos.DeleteVideo
import mikhail.shell.video.hosting.domain.usecases.videos.UploadSource
import mikhail.shell.video.hosting.domain.validation.constructNetworkErrorMessage
import mikhail.shell.video.hosting.presentation.activities.MainActivity
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@AndroidEntryPoint
class VideoUploadingService : Service() {
    private lateinit var videoUploadingEntryPoint: VideoUploadingEntryPoint
    private lateinit var uploadSource: UploadSource
    private lateinit var confirmUpload: ConfirmVideoUpload
    private lateinit var removeVideo: DeleteVideo
    private var NOTIFICATION_COUNT = 0
    private lateinit var notificationManager: NotificationManager
    private val coroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var uploadJob: Job? = null

    private val videoIdState = MutableStateFlow<Long?>(null)
    private var sourceUri: Uri? = null

    override fun onCreate() {
        notificationManager = getSystemService(NotificationManager::class.java)
        videoUploadingEntryPoint =
            EntryPointAccessors.fromApplication(this, VideoUploadingEntryPoint::class.java)
        confirmUpload = videoUploadingEntryPoint.getConfirmVideoUpload()
        uploadSource = videoUploadingEntryPoint.getUploadVideoSource()
        removeVideo = videoUploadingEntryPoint.getRemoveVideo()
    }

    @kotlin.OptIn(ExperimentalUuidApi::class)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let { notNullIntent ->
            if (notNullIntent.action == ACTION_LAUNCH_UPLOADING) {
                notNullIntent.extras?.let { bundle ->
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        startForeground(
                            ++NOTIFICATION_COUNT,
                            createProgressNotification(),
                            ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
                        )
                    } else {
                        startForeground(++NOTIFICATION_COUNT, createProgressNotification())
                    }
                    val tmpId = Uuid.parse(bundle.getString("tmp_id")!!)
                    sourceUri = bundle.getString("source")!!.toUri()
                    uploadJob = coroutineScope.launch {
                        try {
                            uploadSource(
                                tmpId = tmpId,
                                source = bundle.getString("source")!!
                            ) {
                                updateProgressNotification((it * 100).toInt())
                            }.onFailure { err ->
                                stopUploading()
                                displayFailureNotification(err)
                            }.onSuccess {
                                stopUploading()
                                coroutineScope.launch {
                                    confirmUpload(tmpId).onSuccess {
                                        displaySuccessNotification(it)
                                    }.onFailure {
                                        displayFailureNotification(it)
                                        // TODO (?)
                                    }
                                }
                            }
                        } catch (_: CancellationException) {
                            // TODO
                        }
                    }
                }
            } else if (notNullIntent.action == ACTION_CANCEL_UPLOADING) {
                coroutineScope.launch {
                    videoIdState
                        .mapNotNull { it }
                        .collect { newVideoId ->
                            removeVideo(newVideoId)
                            if (uploadJob?.isCancelled != true) {
                                uploadJob?.cancel()
                            }
                            uploadJob = null
                        }
                }

            }
        }
        return START_REDELIVER_INTENT
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    @OptIn(UnstableApi::class)
    private fun displaySuccessNotification(video: Video) {
        val deepLinkIntent = Intent(this, MainActivity::class.java).apply {
            data = "https://$HOST/videos/${video.videoId}".toUri()
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            deepLinkIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val notification = NotificationCompat.Builder(this, "video_uploading")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(getString(R.string.video_upload_success))
            .setContentText(getString(R.string.video_upload_success_hint))
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
        notificationManager.notify(++NOTIFICATION_COUNT, notification)
    }

    private fun displayFailureNotification(error: Error) {
        val errorMessage =
            if (error is NetworkError) constructNetworkErrorMessage(error) else getString(R.string.unexpected_error)
        val notification = NotificationCompat.Builder(this, "video_uploading")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(getString(R.string.video_upload_failure))
            .setContentText(errorMessage)
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
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(getString(R.string.video_upload_progress_title))
            .setContentText(getString(R.string.video_upload_progress_hint))
            .setSilent(true)
            .setProgress(100, progress, false)
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
            Intent(this, VideoUploadingService::class.java).also {
                it.action = ACTION_CANCEL_UPLOADING
            },
            PendingIntent.FLAG_IMMUTABLE
        )
    )

    private fun stopUploading() {
        contentResolver.apply {
            if (!sourceUri.toString().contains("$packageName.fileprovider")) {
                releasePersistableUriPermission(
                    sourceUri!!,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            }
        }
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    companion object {
        private const val PACKAGE_NAME = "mikhail.shell.video.hosting"
        const val ACTION_LAUNCH_UPLOADING = "$PACKAGE_NAME.ACTION_LAUNCH_UPLOADING"
        const val ACTION_CANCEL_UPLOADING = "$PACKAGE_NAME.ACTION_STOP_UPLOADING"
    }
}