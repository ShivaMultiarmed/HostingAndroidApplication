package mikhail.shell.video.hosting.di

import androidx.media3.common.Player
import com.google.firebase.messaging.FirebaseMessaging
import com.google.gson.Gson
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import mikhail.shell.video.hosting.domain.providers.UserDetailsProvider
import mikhail.shell.video.hosting.domain.repositories.CommentRepository
import mikhail.shell.video.hosting.domain.usecases.channels.SubscribeToNotifications
import mikhail.shell.video.hosting.domain.usecases.videos.ConfirmVideoUpload
import mikhail.shell.video.hosting.domain.usecases.videos.DeleteVideo
import mikhail.shell.video.hosting.domain.usecases.videos.UploadSource

@EntryPoint
@InstallIn(SingletonComponent::class)
interface VideoUploadingEntryPoint {
    fun getUploadVideoSource(): UploadSource
    fun getConfirmVideoUpload(): ConfirmVideoUpload
    fun getRemoveVideo(): DeleteVideo
}

@EntryPoint
@InstallIn(SingletonComponent::class)
interface NotificationEntryPoint {
    fun getFirebaseMessaging(): FirebaseMessaging
    fun getResubscribe(): SubscribeToNotifications
    fun getUserDetailsProvider(): UserDetailsProvider
    fun getCommentRepository(): CommentRepository
    fun getGson(): Gson
}

@EntryPoint
@InstallIn(SingletonComponent::class)
interface AudioReceiverEntryPoint{
    fun getPlayer(): Player
}