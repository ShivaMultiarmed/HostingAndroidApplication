package mikhail.shell.video.hosting.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import mikhail.shell.video.hosting.data.repositories.VideoRepositoryWithApi
import mikhail.shell.video.hosting.domain.repositories.VideoRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoriesModule {
    @Binds
    @Singleton
    abstract fun bindVideoRepository(repositoryImpl: VideoRepositoryWithApi): VideoRepository
}