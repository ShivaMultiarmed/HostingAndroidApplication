package mikhail.shell.video.hosting.presentation.user.edit

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue
import mikhail.shell.video.hosting.domain.errors.Error
import mikhail.shell.video.hosting.domain.errors.FileError
import mikhail.shell.video.hosting.domain.errors.TextError
import mikhail.shell.video.hosting.presentation.utils.EditingState
import mikhail.shell.video.hosting.presentation.utils.FieldState
import mikhail.shell.video.hosting.presentation.user.edit.UserEditingScreenState as ScreenState

@Parcelize
sealed class UserEditingScreenState : Parcelable {
    @Parcelize
    data object Idle: ScreenState()
    @Parcelize
    data object Starting: ScreenState()
    @Parcelize
    data class Failure(
        val error: @RawValue Error
    ): ScreenState()
    @Parcelize
    data class Editing(
        val user: UserEditingInputState,
        val isLoading: Boolean = false,
        val isRemoving: Boolean = false
    ): ScreenState()
}
@Parcelize
data class UserEditingInputState(
    val userId: Long,
    val nick: FieldState<String, Error>,
    val name: FieldState<String, TextError>,
    val avatar: FieldState<EditingState<String?>, FileError>,
    val bio: FieldState<String, TextError>,
    val tel: FieldState<String, TextError>,
    val email: FieldState<String, TextError>
): Parcelable {
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