package mikhail.shell.video.hosting.di

import androidx.media3.common.Player
import com.google.firebase.messaging.FirebaseMessaging
import com.google.gson.Gson
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import mikhail.shell.video.hosting.domain.managers.NotificationOrchestrator
import mikhail.shell.video.hosting.domain.usecases.user.ObserveUserDetails
import mikhail.shell.video.hosting.domain.usecases.user.SubscribeToNotifications
import mikhail.shell.video.hosting.domain.usecases.videos.ConfirmVideoUpload
import mikhail.shell.video.hosting.domain.usecases.videos.DeleteVideo
import mikhail.shell.video.hosting.domain.usecases.videos.UploadSource

@EntryPoint
@InstallIn(SingletonComponent::class)
interface VideoDownloadingEntryPoint {
    fun getNotificationOrchestrator(): NotificationOrchestrator
}

@EntryPoint
@InstallIn(SingletonComponent::class)
interface VideoUploadingEntryPoint {
    fun getNotificationOrchestrator(): NotificationOrchestrator
    fun getUploadVideoSource(): UploadSource
    fun getConfirmVideoUpload(): ConfirmVideoUpload
    fun getRemoveVideo(): DeleteVideo
}

@EntryPoint
@InstallIn(SingletonComponent::class)
interface NotificationEntryPoint {
    fun getNotificationOrchestrator(): NotificationOrchestrator
    fun getFirebaseMessaging(): FirebaseMessaging
    fun getResubscribe(): SubscribeToNotifications
    fun observeUserDetails(): ObserveUserDetails
    fun getGson(): Gson
}

@EntryPoint
@InstallIn(SingletonComponent::class)
interface AudioReceiverEntryPoint{
    fun getPlayer(): Player
}