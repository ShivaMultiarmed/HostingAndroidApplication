package mikhail.shell.video.hosting.di

import androidx.media3.common.Player
import com.google.gson.Gson
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import mikhail.shell.video.hosting.domain.providers.UserDetailsProvider
import mikhail.shell.video.hosting.domain.repositories.CommentRepository
import mikhail.shell.video.hosting.domain.usecases.channels.Resubscribe
import mikhail.shell.video.hosting.domain.usecases.videos.UploadVideo

@EntryPoint
@InstallIn(SingletonComponent::class)
interface VideoUploadingEntryPoint {
    fun getUploadVideo(): UploadVideo
}

@EntryPoint
@InstallIn(SingletonComponent::class)
interface NotificationEntryPoint {
    fun getResubscribe(): Resubscribe
    fun getUserDetailsProvider(): UserDetailsProvider
    fun getCommentRepository(): CommentRepository
    fun getGson(): Gson
}

@EntryPoint
@InstallIn(SingletonComponent::class)
interface AudioReceiverEntryPoint{
    fun getPlayer(): Player
}