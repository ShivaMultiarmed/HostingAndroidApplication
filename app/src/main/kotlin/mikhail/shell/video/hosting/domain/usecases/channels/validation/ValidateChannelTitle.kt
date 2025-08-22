package mikhail.shell.video.hosting.domain.usecases.channels.validation

import mikhail.shell.video.hosting.domain.errors.Error
import mikhail.shell.video.hosting.domain.errors.TextError
import mikhail.shell.video.hosting.domain.models.Result
import mikhail.shell.video.hosting.domain.repositories.ChannelRepository
import mikhail.shell.video.hosting.domain.validation.ValidationRules
import javax.inject.Inject

class ValidateChannelTitle @Inject constructor(
    private val channelRepository: ChannelRepository
) {
    suspend operator fun invoke(title: String): Result<Unit, Error> {
        return if (title.isEmpty()) {
            Result.Failure(TextError.EMPTY)
        } else if (title.length > ValidationRules.MAX_TITLE_LENGTH) {
            Result.Failure(TextError.LARGE)
        } else {
            val exists = channelRepository.existsByTitle(title)
            if (exists is Result.Success) {
                if (exists.data) {
                    Result.Failure(TextError.EXISTS)
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