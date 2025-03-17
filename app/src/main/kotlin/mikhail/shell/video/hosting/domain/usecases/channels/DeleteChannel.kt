package mikhail.shell.video.hosting.domain.usecases.channels

import mikhail.shell.video.hosting.domain.models.Result
import mikhail.shell.video.hosting.domain.errors.DeleteChannelError
import mikhail.shell.video.hosting.domain.repositories.ChannelRepository
import javax.inject.Inject

class DeleteChannel @Inject constructor(
    private val channelRepository: ChannelRepository
) {
    suspend operator fun invoke(channelId: Long): Result<Unit, DeleteChannelError> {
        return channelRepository.removeChannel(channelId)
    }
}
