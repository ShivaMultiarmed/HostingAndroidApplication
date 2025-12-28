package mikhail.shell.video.hosting.domain.usecases.channels

import mikhail.shell.video.hosting.domain.errors.Error
import mikhail.shell.video.hosting.domain.models.ChannelCreationModel
import mikhail.shell.video.hosting.domain.models.Result
import mikhail.shell.video.hosting.domain.repositories.ChannelRepository
import javax.inject.Inject

class CreateChannel @Inject constructor(
    private val channelRepository: ChannelRepository
) {
    suspend operator fun invoke(channel: ChannelCreationModel): Result<Long, Error> {
        val creationResult = channelRepository.create(channel)
        return if (creationResult is Result.Success) {
            Result.Success(creationResult.data.channelId)
        } else {
            creationResult as Result.Failure
            Result.Failure(creationResult.error)
        }
    }
}