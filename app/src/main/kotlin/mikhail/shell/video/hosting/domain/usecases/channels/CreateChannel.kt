package mikhail.shell.video.hosting.domain.usecases.channels

import mikhail.shell.video.hosting.domain.errors.ChannelCreationError
import mikhail.shell.video.hosting.domain.errors.CompoundError
import mikhail.shell.video.hosting.domain.models.Channel
import mikhail.shell.video.hosting.domain.models.Result
import mikhail.shell.video.hosting.domain.repositories.ChannelRepository
import mikhail.shell.video.hosting.domain.utils.isBlank
import mikhail.shell.video.hosting.presentation.channel.create.ChannelInputState
import javax.inject.Inject

class CreateChannel @Inject constructor(
    private val channelRepository: ChannelRepository
) {
    suspend operator fun invoke(
        input: ChannelInputState,
        ownerId: Long
    ): Result<Channel, CompoundError<ChannelCreationError>> {
        val error = CompoundError<ChannelCreationError>()
        if (input.title.isBlank())
            error.add(ChannelCreationError.TITLE_EMPTY)
        if (error.isNotNull())
            return Result.Failure(error)
        val channel = Channel(
            ownerId = ownerId,
            description = input.description,
            title = input.title!!,
            alias = input.alias
        )
        return channelRepository.createChannel(channel)
    }
}
