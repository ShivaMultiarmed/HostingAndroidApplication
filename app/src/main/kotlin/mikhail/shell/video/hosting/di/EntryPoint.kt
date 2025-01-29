package mikhail.shell.video.hosting.di

import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import mikhail.shell.video.hosting.domain.usecases.videos.UploadVideo

@EntryPoint
@InstallIn(SingletonComponent::class)
interface EntryPoint {
    fun getUploadVideoUsecase(): UploadVideo
}