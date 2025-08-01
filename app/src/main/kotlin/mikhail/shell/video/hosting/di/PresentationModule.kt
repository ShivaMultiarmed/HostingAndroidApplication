package mikhail.shell.video.hosting.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object PresentationModule {
    const val HOST = "trendy-app.ru"
}