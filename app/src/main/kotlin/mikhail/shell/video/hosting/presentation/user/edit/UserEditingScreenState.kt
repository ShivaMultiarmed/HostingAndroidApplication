package mikhail.shell.video.hosting.presentation.user.edit

import mikhail.shell.video.hosting.domain.errors.Error
import mikhail.shell.video.hosting.domain.errors.FileError
import mikhail.shell.video.hosting.domain.errors.TextError
import mikhail.shell.video.hosting.presentation.utils.EditingState
import mikhail.shell.video.hosting.presentation.utils.FieldState
import mikhail.shell.video.hosting.presentation.user.edit.UserEditingScreenState as ScreenState

sealed class UserEditingScreenState {
    data object Idle: ScreenState()
    data object Starting: ScreenState()
    data class Failure(val error: Error): ScreenState()
    data class Editing(
        val user: UserEditingInputState,
        val isLoading: Boolean = false,
        val isRemoving: Boolean = false
    ): ScreenState()
}

data class UserEditingInputState(
    val userId: Long,
    val nick: FieldState<String, Error>,
    val name: FieldState<String, TextError>,
    val avatar: FieldState<EditingState<String?>, FileError>,
    val bio: FieldState<String, TextError>,
    val tel: FieldState<String, TextError>,
    val email: FieldState<String, TextError>
) {
    companion object {
        fun initialize(
            userId: Long,
            nick: String,
            name: String,
            avatar: String,
            bio: String,
            telephone: String,
            email: String
        ) = UserEditingInputState(
            userId = userId,
            nick = FieldState(nick),
            name = FieldState(name),
            avatar = FieldState(EditingState.Keeping(avatar)),
            bio = FieldState(bio),
            tel = FieldState(telephone),
            email = FieldState(email)
        )
    }
}