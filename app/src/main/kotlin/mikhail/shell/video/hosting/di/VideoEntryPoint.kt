package mikhail.shell.video.hosting.di

import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import mikhail.shell.video.hosting.domain.providers.UserDetailsProvider
import mikhail.shell.video.hosting.domain.usecases.channels.Resubscribe
import mikhail.shell.video.hosting.domain.usecases.videos.UploadVideo

@EntryPoint
@InstallIn(SingletonComponent::class)
interface VideoUploadingEntryPoint {
    fun getUploadVideo(): UploadVideo
}

@EntryPoint
@InstallIn(SingletonComponent::class)
interface ChannelNotificationEntryPoint {
    fun getResubscribe(): Resubscribe
    fun getUserDetailsProvider(): UserDetailsProvider
}
//
//@EntryPoint
//@InstallIn(SingletonComponent::class)
//interface VideoPlayerEntryPoint{
//    fun getVideoPlayer(): Player
//}