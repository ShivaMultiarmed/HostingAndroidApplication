package mikhail.shell.video.hosting.domain.usecases.channels

import mikhail.shell.video.hosting.domain.errors.ChannelCreationError
import mikhail.shell.video.hosting.domain.errors.CompoundError
import mikhail.shell.video.hosting.domain.models.Channel
import mikhail.shell.video.hosting.domain.models.Result
import mikhail.shell.video.hosting.domain.repositories.ChannelRepository
import mikhail.shell.video.hosting.domain.validation.ValidationRules
import java.io.File
import javax.inject.Inject

class CreateChannel @Inject constructor(
    private val channelRepository: ChannelRepository
) {
    suspend operator fun invoke(
        channel: Channel,
        avatar: File?,
        cover: File?
    ): Result<Channel, CompoundError<ChannelCreationError>> {
        val compoundError = CompoundError<ChannelCreationError>()
        if (channel.title.length > ValidationRules.MAX_TITLE_LENGTH) {
            compoundError.add(ChannelCreationError.TITLE_TOO_LARGE)
        }
        if ((channel.alias?.length ?: 0) > ValidationRules.MAX_TITLE_LENGTH) {
            compoundError.add(ChannelCreationError.ALIAS_TOO_LARGE)
        }
        if ((channel.description?.length ?: 0) > ValidationRules.MAX_TEXT_LENGTH) {
            compoundError.add(ChannelCreationError.DESCRIPTION_TOO_LARGE)
        }
        return if (compoundError.isNotNull()) {
            Result.Failure(compoundError)
        } else {
            channelRepository.createChannel(channel,avatar,cover)
        }
    }
}
