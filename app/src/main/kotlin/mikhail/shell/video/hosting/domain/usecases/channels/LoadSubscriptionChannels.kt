package mikhail.shell.video.hosting.domain.usecases.channels

import mikhail.shell.video.hosting.domain.errors.Error
import mikhail.shell.video.hosting.domain.models.Channel
import mikhail.shell.video.hosting.domain.models.Result
import mikhail.shell.video.hosting.domain.repositories.ChannelRepository
import javax.inject.Inject

class LoadSubscriptionChannels @Inject constructor(
    private val channelsRepository: ChannelRepository
) {
    suspend operator fun invoke(userId: Long): Result<List<Channel>, Error> {
        return channelsRepository.fetchChannelsBySubscriber(userId)
    }
}
