package mikhail.shell.video.hosting.domain.usecases

import mikhail.shell.video.hosting.domain.errors.Error
import mikhail.shell.video.hosting.domain.models.Channel
import mikhail.shell.video.hosting.domain.models.Result
import mikhail.shell.video.hosting.domain.repositories.ChannelRepository
import javax.inject.Inject

class CreateChannelUseCase @Inject constructor(
    private val channelRepository: ChannelRepository
) {
    suspend operator fun invoke(channel: Channel, logo: String?, header: String?): Result<Long, Error> {
        val creationResult = channelRepository.create(
            channel = channel,
            logo = logo,
            header = header
        )
        return if (creationResult is Result.Success) {
            Result.Success(creationResult.data.channelId!!)
        } else {
            creationResult as Result.Failure
            Result.Failure(creationResult.error)
        }
    }
}
