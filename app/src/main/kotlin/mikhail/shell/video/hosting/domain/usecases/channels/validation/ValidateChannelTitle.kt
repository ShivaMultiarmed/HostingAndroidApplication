package mikhail.shell.video.hosting.domain.usecases.channels.validation

import mikhail.shell.video.hosting.domain.errors.Error
import mikhail.shell.video.hosting.domain.errors.TextError.EXISTS
import mikhail.shell.video.hosting.domain.models.Result
import mikhail.shell.video.hosting.domain.repositories.ChannelRepository
import mikhail.shell.video.hosting.domain.utils.ValidateTitle
import javax.inject.Inject

class ValidateChannelTitle @Inject constructor(
    private val validateTitle: ValidateTitle,
    private val channelRepository: ChannelRepository
) {
    suspend operator fun invoke(title: String): Result<Unit, Error> {
        val localResult = validateTitle(title)
        return if (localResult is Result.Failure) {
            Result.Failure(localResult.error)
        } else {
            val exists = channelRepository.existsByTitle(title)
            if (exists is Result.Success) {
                if (exists.data) {
                    Result.Failure(EXISTS)
                } else {
                    Result.Success(Unit)
                }
            } else {
                exists as Result.Failure
                Result.Failure(exists.error)
            }
        }
    }
}