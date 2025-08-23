package mikhail.shell.video.hosting.domain.usecases.channels

import mikhail.shell.video.hosting.domain.errors.Error
import mikhail.shell.video.hosting.domain.models.ChannelForUser
import mikhail.shell.video.hosting.domain.models.Result
import mikhail.shell.video.hosting.domain.repositories.ChannelRepository
import javax.inject.Inject

class GetChannelDetails @Inject constructor(
    private val _channelRepository: ChannelRepository
) {
    suspend operator fun invoke(channelId: Long): Result<ChannelForUser, Error> =
        _channelRepository.fetchChannelForUser(channelId)
}