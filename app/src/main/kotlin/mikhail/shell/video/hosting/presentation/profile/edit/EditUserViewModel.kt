package mikhail.shell.video.hosting.presentation.profile.edit

import androidx.lifecycle.ViewModel
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import mikhail.shell.video.hosting.domain.usecases.user.EditUser
import mikhail.shell.video.hosting.domain.usecases.user.GetUser
import mikhail.shell.video.hosting.domain.usecases.user.RemoveUser

@HiltViewModel(assistedFactory = EditUserViewModel.Factory::class)
class EditUserViewModel @AssistedInject constructor(
    val userId: Long,
    val _getUser: GetUser,
    val _editUser: EditUser,
    val _removeUser: RemoveUser
): ViewModel() {

    @AssistedFactory
    interface Factory {
        fun create(userId: Long): EditUserViewModel
    }
}