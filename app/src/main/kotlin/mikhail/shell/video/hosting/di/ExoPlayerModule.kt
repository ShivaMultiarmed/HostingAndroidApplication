package mikhail.shell.video.hosting.di

import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import mikhail.shell.video.hosting.data.DSWithTokenFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ExoPlayerModule {
    @OptIn(UnstableApi::class)
    @Provides
    @Singleton
    fun provideMediaSourceFactory(
        dsWithTokenFactory: DSWithTokenFactory
    ): DefaultMediaSourceFactory {
        return DefaultMediaSourceFactory(dsWithTokenFactory)
    }
}