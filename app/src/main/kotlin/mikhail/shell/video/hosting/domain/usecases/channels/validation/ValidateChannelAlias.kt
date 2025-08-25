package mikhail.shell.video.hosting.domain.usecases.channels.validation

import mikhail.shell.video.hosting.domain.errors.Error
import mikhail.shell.video.hosting.domain.errors.TextError.EMPTY
import mikhail.shell.video.hosting.domain.errors.TextError.EXISTS
import mikhail.shell.video.hosting.domain.errors.TextError.LONG
import mikhail.shell.video.hosting.domain.models.Result
import mikhail.shell.video.hosting.domain.repositories.ChannelRepository
import mikhail.shell.video.hosting.domain.validation.ValidationRules.MAX_TITLE_LENGTH
import javax.inject.Inject

class ValidateChannelAlias @Inject constructor(
    private val channelRepository: ChannelRepository
) {
    suspend operator fun invoke(alias: String): Result<Unit, Error> {
        return if (alias.isEmpty()) {
            Result.Failure(EMPTY)
        } else if (alias.length > MAX_TITLE_LENGTH) {
            Result.Failure(LONG)
        } else {
            val exists = channelRepository.existsByAlias(alias)
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