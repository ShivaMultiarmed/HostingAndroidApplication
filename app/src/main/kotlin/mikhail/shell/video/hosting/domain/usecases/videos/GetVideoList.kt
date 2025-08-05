package mikhail.shell.video.hosting.domain.usecases.videos

import mikhail.shell.video.hosting.domain.errors.Error
import mikhail.shell.video.hosting.domain.models.Result
import mikhail.shell.video.hosting.domain.models.Video
import mikhail.shell.video.hosting.domain.repositories.VideoRepository
import javax.inject.Inject

class GetVideoList @Inject constructor(
    private val repository: VideoRepository
) {
    suspend operator fun invoke(
        channelId: Long,
        userId: Long,
        partNumber: Long,
        partSize: Int
    ): Result<List<Video>, Error> {
        return repository.fetchChannelVideoList(
            channelId = channelId,
            partNumber = partNumber,
            partSize = partSize
        )
    }
}