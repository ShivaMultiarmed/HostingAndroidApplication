package mikhail.shell.video.hosting.domain.usecases.user.validation

import mikhail.shell.video.hosting.domain.errors.TextError
import mikhail.shell.video.hosting.domain.models.Result
import javax.inject.Inject

class ValidateName @Inject constructor() {
    operator fun invoke(name: String): Result<Unit, TextError> {

    }
}