package mikhail.shell.video.hosting.domain.utils

import mikhail.shell.video.hosting.domain.models.ImageSize
import mikhail.shell.video.hosting.domain.repositories.ChannelRepository
import javax.inject.Inject

class GetChannelHeaderUrl @Inject constructor(
    private val channelRepository: ChannelRepository
) {
    operator fun invoke(channelId: Long, size: ImageSize): String {
        return channelRepository.constructHeaderUrl(channelId, size)
    }
}