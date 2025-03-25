package mikhail.shell.video.hosting.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import mikhail.shell.video.hosting.data.CommentRepositoryWithApi
import mikhail.shell.video.hosting.data.repositories.AuthRepositoryWithApi
import mikhail.shell.video.hosting.data.repositories.ChannelRepositoryWithApi
import mikhail.shell.video.hosting.data.repositories.VideoRepositoryWithApi
import mikhail.shell.video.hosting.domain.repositories.AuthRepository
import mikhail.shell.video.hosting.domain.repositories.ChannelRepository
import mikhail.shell.video.hosting.domain.repositories.CommentRepository
import mikhail.shell.video.hosting.domain.repositories.VideoRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoriesModule {
    @Binds
    @Singleton
    abstract fun bindVideoRepository(repositoryImpl: VideoRepositoryWithApi): VideoRepository

    @Binds
    @Singleton
    abstract fun bindChannelRepository(repositoryWithApi: ChannelRepositoryWithApi): ChannelRepository

    @Binds
    @Singleton
    abstract fun bindAuthRepository(repositoryWithApi: AuthRepositoryWithApi): AuthRepository

    @Binds
    @Singleton
    abstract fun bindCommentRepository(repositoryWithApi: CommentRepositoryWithApi): CommentRepository
}