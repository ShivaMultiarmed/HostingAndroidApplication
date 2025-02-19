package mikhail.shell.video.hosting.di

import android.content.Context
import android.media.session.PlaybackState
import android.util.Log
import androidx.annotation.OptIn
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.components.ServiceComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ServiceScoped
import dagger.hilt.components.SingletonComponent
import mikhail.shell.video.hosting.data.DSWithTokenFactory
import javax.inject.Singleton

@Module
@InstallIn(ServiceComponent::class)
object ExoPlayerModule {
    @OptIn(UnstableApi::class)
    @Provides
    @ServiceScoped
    fun provideMediaSourceFactory(
        dsWithTokenFactory: DSWithTokenFactory
    ): DefaultMediaSourceFactory {
        return DefaultMediaSourceFactory(dsWithTokenFactory)
    }
    @Provides
    @ServiceScoped
    fun providePlayer(
        @ApplicationContext context: Context,
        mediaSourceFactory: DefaultMediaSourceFactory
    ): Player {
        return ExoPlayer.Builder(context)
            .setMediaSourceFactory(mediaSourceFactory)
            .build()
    }
}