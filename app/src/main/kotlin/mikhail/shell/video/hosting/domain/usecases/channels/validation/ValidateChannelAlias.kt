package mikhail.shell.video.hosting.domain.usecases.channels.validation

import mikhail.shell.video.hosting.domain.errors.Error
import mikhail.shell.video.hosting.domain.models.Result
import mikhail.shell.video.hosting.domain.repositories.ChannelRepository
import mikhail.shell.video.hosting.domain.utils.ValidateTitle
import javax.inject.Inject

class ValidateChannelAlias @Inject constructor(
    private val validateTitle: ValidateTitle,
    private val channelRepository: ChannelRepository
) {
    suspend operator fun invoke(channelId: Long? = null, alias: String): Result<Unit, Error> {
        val localResult = validateTitle(alias)
        return if (localResult is Result.Failure) {
            Result.Failure(localResult.error)
        } else {
            channelRepository.existsByAlias(channelId = channelId, alias = alias)
        }
    }
}