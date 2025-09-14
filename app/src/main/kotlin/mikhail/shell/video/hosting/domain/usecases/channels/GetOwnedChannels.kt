package mikhail.shell.video.hosting.domain.usecases.channels

import mikhail.shell.video.hosting.domain.errors.Error
import mikhail.shell.video.hosting.domain.models.Channel
import mikhail.shell.video.hosting.domain.models.Result
import mikhail.shell.video.hosting.domain.repositories.ChannelRepository
import javax.inject.Inject

class GetOwnedChannels @Inject constructor(
    private val channelRepository: ChannelRepository
) {
    suspend operator fun invoke(
        userId: Long,
        partIndex: Int,
        partSize: Int
    ): Result<List<Channel>, Error> = channelRepository.fetchChannelsByOwner(
        userId = userId,
        partIndex = partIndex,
        partSize = partSize
    )
}