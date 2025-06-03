package mikhail.shell.video.hosting.domain.usecases.channels

import mikhail.shell.video.hosting.domain.errors.ChannelLoadingError
import mikhail.shell.video.hosting.domain.models.ChannelWithUser
import mikhail.shell.video.hosting.domain.models.Result
import mikhail.shell.video.hosting.domain.repositories.ChannelRepository
import javax.inject.Inject

class GetChannelInfo @Inject constructor(
    private val _channelRepository: ChannelRepository
) {
    suspend operator fun invoke(channelId: Long, userId: Long): Result<ChannelWithUser, ChannelLoadingError> {
        return _channelRepository.fetchChannelForUser(channelId, userId)
    }
}