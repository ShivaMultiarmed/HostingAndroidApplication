package mikhail.shell.video.hosting.domain.usecases

import mikhail.shell.video.hosting.domain.errors.CompoundError
import mikhail.shell.video.hosting.domain.errors.Error
import mikhail.shell.video.hosting.domain.models.Channel
import mikhail.shell.video.hosting.domain.models.EditAction
import mikhail.shell.video.hosting.domain.models.Result
import mikhail.shell.video.hosting.domain.repositories.ChannelRepository
import javax.inject.Inject

class EditChannel @Inject constructor(
    private val channelRepository: ChannelRepository
) {
    suspend operator fun invoke(
        channel: Channel,
        coverAction: EditAction,
        avatarAction: EditAction
    ): Result<Channel, CompoundError<Error>> {
        return channelRepository.editChannel(channel,,)
    }
}
