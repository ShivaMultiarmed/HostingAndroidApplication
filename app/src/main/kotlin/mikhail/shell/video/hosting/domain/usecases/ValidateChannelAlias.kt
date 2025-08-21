package mikhail.shell.video.hosting.domain.usecases

import mikhail.shell.video.hosting.domain.errors.Error
import mikhail.shell.video.hosting.domain.errors.TextError
import mikhail.shell.video.hosting.domain.models.Result
import mikhail.shell.video.hosting.domain.repositories.ChannelRepository
import mikhail.shell.video.hosting.domain.validation.ValidationRules
import javax.inject.Inject

class ValidateChannelAlias @Inject constructor(
    private val channelRepository: ChannelRepository
) {
    suspend operator fun invoke(alias: String): Result<Unit, Error> {
        return if (alias.isEmpty()) {
            Result.Failure(TextError.EMPTY)
        } else if (alias.length > ValidationRules.MAX_TITLE_LENGTH) {
            Result.Failure(TextError.LARGE)
        } else {
            val exists = channelRepository.existsByAlias(alias)
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