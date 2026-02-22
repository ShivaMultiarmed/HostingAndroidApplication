package mikhail.shell.video.hosting.domain.usecases.videos.validation

import mikhail.shell.video.hosting.domain.errors.NumericError
import mikhail.shell.video.hosting.domain.models.Result
import javax.inject.Inject

class ValidateChannelId @Inject constructor() {
    operator fun invoke(channelId: Long?): Result<Unit, NumericError> {
        return if (channelId == null) {
            Result.Failure(NumericError.EMPTY)
        } else {
            Result.Success(Unit)
        }
    }
}