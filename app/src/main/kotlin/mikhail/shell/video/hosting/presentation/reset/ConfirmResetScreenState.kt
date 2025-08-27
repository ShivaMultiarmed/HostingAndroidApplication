package mikhail.shell.video.hosting.presentation.reset

import mikhail.shell.video.hosting.domain.errors.Error
import mikhail.shell.video.hosting.domain.errors.TextError
import mikhail.shell.video.hosting.domain.models.AuthModel

sealed class ConfirmResetScreenState {
    data class Entering (
        val input: ConfirmResetInputState = ConfirmResetInputState(),
        val isLoading: Boolean = false,
        val error: Error? = null
    ): ConfirmResetScreenState()
    data class Success(val authModel: AuthModel): ConfirmResetScreenState()
    data object Expiration: ConfirmResetScreenState()
}

data class ConfirmResetInputState(
    val password: String = "",
    val passwordError: TextError? = null,
    val passwordDuplicate: String = "",
    val passwordDuplicateError: TextError? = null,
)
