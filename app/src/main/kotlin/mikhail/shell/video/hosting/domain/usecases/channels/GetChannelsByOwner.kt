package mikhail.shell.video.hosting.domain.usecases.channels

import mikhail.shell.video.hosting.domain.errors.ChannelError
import mikhail.shell.video.hosting.domain.models.Channel
import mikhail.shell.video.hosting.domain.models.Result
import mikhail.shell.video.hosting.domain.repositories.ChannelRepository
import javax.inject.Inject

class GetChannelsByOwner @Inject constructor(
    private val channelRepository: ChannelRepository
) {
    suspend operator fun invoke(userId: Long): Result<List<Channel>, ChannelError> {
        return channelRepository.fetchChannelsByOwner(userId)
    }
}