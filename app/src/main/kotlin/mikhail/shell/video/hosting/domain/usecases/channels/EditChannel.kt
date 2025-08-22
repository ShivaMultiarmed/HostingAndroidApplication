package mikhail.shell.video.hosting.domain.usecases.channels

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
        headerAction: EditAction,
        header: String?,
        logoAction: EditAction,
        logo: String?
    ): Result<Channel, Error> {
        return channelRepository.editChannel(
            channel = channel,
            headerAction = headerAction,
            header = header,
            logoAction = logoAction,
            logo = logo
        )
    }
}
