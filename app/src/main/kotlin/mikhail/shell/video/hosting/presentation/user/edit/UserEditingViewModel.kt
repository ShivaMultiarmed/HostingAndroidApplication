package mikhail.shell.video.hosting.presentation.user.edit

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.Player
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import mikhail.shell.video.hosting.domain.errors.network.NetworkError
import mikhail.shell.video.hosting.domain.errors.user.UserEditingError
import mikhail.shell.video.hosting.domain.models.EditingAction
import mikhail.shell.video.hosting.domain.models.ImageSize.MEDIUM
import mikhail.shell.video.hosting.domain.models.NickCheckPurpose
import mikhail.shell.video.hosting.domain.models.UserEditingModel
import mikhail.shell.video.hosting.domain.models.errorOrNull
import mikhail.shell.video.hosting.domain.usecases.user.ConstructAvatarUrl
import mikhail.shell.video.hosting.domain.usecases.user.EditUser
import mikhail.shell.video.hosting.domain.usecases.user.GetUser
import mikhail.shell.video.hosting.domain.usecases.user.GetUserDetails
import mikhail.shell.video.hosting.domain.usecases.user.RemoveUser
import mikhail.shell.video.hosting.domain.usecases.user.RemoveUserDetails
import mikhail.shell.video.hosting.domain.usecases.user.UnsubscribeFromNotifications
import mikhail.shell.video.hosting.domain.usecases.user.validation.ValidateBio
import mikhail.shell.video.hosting.domain.usecases.user.validation.ValidateEmail
import mikhail.shell.video.hosting.domain.usecases.user.validation.ValidateName
import mikhail.shell.video.hosting.domain.usecases.user.validation.ValidateNick
import mikhail.shell.video.hosting.domain.usecases.user.validation.ValidateTelephone
import mikhail.shell.video.hosting.domain.utils.ValidateImage
import mikhail.shell.video.hosting.presentation.utils.EditingState
import mikhail.shell.video.hosting.presentation.utils.stateIn
import javax.inject.Inject
import mikhail.shell.video.hosting.presentation.user.edit.UserEditingScreenAction as ScreenAction
import mikhail.shell.video.hosting.presentation.user.edit.UserEditingScreenEvent as ScreenEvent
import mikhail.shell.video.hosting.presentation.user.edit.UserEditingScreenState as ScreenState

@HiltViewModel
class UserEditingViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val getUserDetails: GetUserDetails,
    private val removeUserDetails: RemoveUserDetails,
    private val player: Player,
    private val getUser: GetUser,
    private val validateNick: ValidateNick,
    private val validateName: ValidateName,
    private val validateBio: ValidateBio,
    private val validateImage: ValidateImage,
    private val validateEmail: ValidateEmail,
    private val validateTelephone: ValidateTelephone,
    private val constructAvatarUrl: ConstructAvatarUrl,
    private val editUser: EditUser,
    private val unsubscribeFromNotifications: UnsubscribeFromNotifications,
    private val removeUser: RemoveUser
) : ViewModel() {
    private val _state = savedStateHandle.getMutableStateFlow<ScreenState>(
        key = "state",
        initialValue = ScreenState.Idle
    )
    val state = _state.onStart {
        if (_state.value !is ScreenState.Editing) {
            start()
        }
    }.stateIn(_state.value)

    private val _events = MutableSharedFlow<ScreenEvent>()
    val events = _events.asSharedFlow()

    fun onAction(action: ScreenAction) {
        when (action) {
            is ScreenAction.ChangeNick -> onNickChanged(action.nick)
            ScreenAction.FocusNick -> onNickFocused()
            ScreenAction.BlurNick -> onNickBlurred()
            is ScreenAction.ChangeName -> onNameChanged(action.name)
            ScreenAction.FocusName -> onNameFocused()
            ScreenAction.BlurName -> onNameBlurred()
            is ScreenAction.ChangeAvatar -> onAvatarChanged(action.avatar)
            is ScreenAction.ChangeBio -> onBioChanged(action.bio)
            ScreenAction.FocusBio -> onBioFocused()
            ScreenAction.BlurBio -> onBioBlurred()
            is ScreenAction.ChangeEmail -> onEmailChanged(action.email)
            ScreenAction.FocusEmail -> onEmailFocused()
            ScreenAction.BlurEmail -> onEmailBlurred()
            is ScreenAction.ChangeTelephone -> onTelephoneChanged(action.telephone)
            ScreenAction.FocusTelephone -> onTelephoneFocused()
            ScreenAction.BlurTelephone -> onTelephoneBlurred()
            ScreenAction.Restart -> start()
            ScreenAction.Remove -> remove()
            ScreenAction.Submit -> edit()
            ScreenAction.Cancel -> viewModelScope.launch {
                _events.emit(ScreenEvent.Cancelled)
            }
        }
    }

    private fun onNickChanged(nick: String) {
        _state.update {
            val currentState = it as? ScreenState.Editing
            currentState?.copy(
                user = currentState.user.copy(
                    nick = currentState.user.nick.copy(value = nick)
                )
            ) ?: it
        }
    }

    private fun onNickFocused() {
        _state.update {
            val currentState = it as? ScreenState.Editing
            currentState?.copy(
                user = currentState.user.copy(
                    nick = currentState.user.nick.copy(error = null)
                )
            ) ?: it
        }
    }

    private fun onNickBlurred() {
        viewModelScope.launch {
            _state.update {
                val currentState = it as? ScreenState.Editing
                if (
                    currentState == null
                    || currentState.isLoading
                    || currentState.isRemoving
                ) {
                    return@update it
                }
                currentState.copy(
                    user = currentState.user.copy(
                        nick = currentState.user.nick.copy(
                            error = validateNick(NickCheckPurpose.EDIT, currentState.user.nick.value).errorOrNull()
                        )
                    )
                )
            }
        }
    }

    private fun onNameChanged(name: String) {
        _state.update {
            val currentState = it as? ScreenState.Editing
            currentState?.copy(
                user = currentState.user.copy(
                    name = currentState.user.name.copy(value = name)
                )
            ) ?: it
        }
    }

    private fun onNameFocused() {
        _state.update {
            val currentState = it as? ScreenState.Editing
            currentState?.copy(
                user = currentState.user.copy(
                    name = currentState.user.name.copy(error = null)
                )
            ) ?: it
        }
    }

    private fun onNameBlurred() {
        _state.update {
            val currentState = it as? ScreenState.Editing
            currentState?.copy(
                user = currentState.user.copy(
                    name = currentState.user.name.copy(
                        error = currentState.user.name.value.takeIf { it.isNotEmpty() }?.let {
                            validateName(it).errorOrNull()
                        }
                    )
                )
            ) ?: it
        }
    }

    private fun onBioChanged(bio: String) {
        _state.update {
            val currentState = it as? ScreenState.Editing
            currentState?.copy(
                user = currentState.user.copy(
                    bio = currentState.user.name.copy(value = bio)
                )
            ) ?: it
        }
    }

    private fun onBioFocused() {
        _state.update {
            val currentState = it as? ScreenState.Editing
            currentState?.copy(
                user = currentState.user.copy(
                    bio = currentState.user.bio.copy(error = null)
                )
            ) ?: it
        }
    }

    private fun onBioBlurred() {
        _state.update {
            val currentState = it as? ScreenState.Editing
            currentState?.copy(
                user = currentState.user.copy(
                    bio = currentState.user.bio.copy(
                        error = currentState.user.bio.value.takeIf { it.isNotEmpty() }?.let {
                            validateBio(it).errorOrNull()
                        }
                    )
                )
            ) ?: it
        }
    }

    private fun onTelephoneChanged(telephone: String) {
        _state.update {
            val currentState = it as? ScreenState.Editing
            currentState?.copy(
                user = currentState.user.copy(
                    tel = currentState.user.tel.copy(value = telephone)
                )
            ) ?: it
        }
    }

    private fun onTelephoneFocused() {
        _state.update {
            val currentState = it as? ScreenState.Editing
            currentState?.copy(
                user = currentState.user.copy(
                    tel = currentState.user.tel.copy(error = null)
                )
            ) ?: it
        }
    }

    private fun onTelephoneBlurred() {
        _state.update {
            val currentState = it as? ScreenState.Editing
            currentState?.copy(
                user = currentState.user.copy(
                    tel = currentState.user.tel.copy(
                        error = currentState.user.tel.value.takeIf { it.isNotEmpty() }?.let {
                            validateTelephone(it).errorOrNull()
                        }
                    )
                )
            ) ?: it
        }
    }

    private fun onEmailChanged(email: String) {
        _state.update {
            val currentState = it as? ScreenState.Editing
            currentState?.copy(
                user = currentState.user.copy(
                    email = currentState.user.email.copy(value = email)
                )
            ) ?: it
        }
    }

    private fun onEmailFocused() {
        _state.update {
            val currentState = it as? ScreenState.Editing
            currentState?.copy(
                user = currentState.user.copy(
                    email = currentState.user.email.copy(error = null)
                )
            ) ?: it
        }
    }

    private fun onEmailBlurred() {
        _state.update {
            val currentState = it as? ScreenState.Editing
            currentState?.copy(
                user = currentState.user.copy(
                    email = currentState.user.email.copy(
                        error = currentState.user.email.value.takeIf { it.isNotEmpty() }?.let {
                            validateEmail(it).errorOrNull()
                        }
                    )
                )
            ) ?: it
        }
    }

    private fun onAvatarChanged(avatar: EditingState<String?>) {
        _state.update {
            val currentState = it as? ScreenState.Editing
            currentState?.copy(
                user = currentState.user.copy(
                    avatar = currentState.user.avatar.copy(
                        value = avatar,
                        error = when (avatar) {
                            is EditingState.Editing -> validateImage(avatar.value!!).errorOrNull()
                            else -> null
                        }
                    )
                )
            ) ?: it
        }
    }

    private fun start() {
        _state.update {
            ScreenState.Starting
        }
        viewModelScope.launch {
            val userId = getUserDetails().userId
            getUser(userId).onSuccess { user ->
                _state.update {
                    ScreenState.Editing(
                        user = UserEditingInputState.initialize(
                            userId = userId,
                            nick = user.nick,
                            name = user.name ?: "",
                            bio = user.bio ?: "",
                            telephone = when(user.tel != null) {
                                true -> "+${user.tel}"
                                false -> ""
                            },
                            email = user.email ?: "",
                            avatar = constructAvatarUrl(userId, MEDIUM)
                        )
                    )
                }
            }.onFailure { error ->
                _state.update {
                    ScreenState.Failure(error)
                }
                viewModelScope.launch {
                    _events.emit(ScreenEvent.Failure(error))
                }
            }
        }
    }

    private fun edit() {
        viewModelScope.launch {
            _state.update {
                val currentState = it as? ScreenState.Editing
                if (
                    currentState == null
                    || currentState.isLoading
                    || currentState.isRemoving
                    ) {
                    return@launch
                }
                currentState.copy(
                    user = currentState.user.copy(
                        nick = currentState.user.nick.copy(
                            error = validateNick(NickCheckPurpose.EDIT, currentState.user.nick.value).errorOrNull()
                        ),
                        name = currentState.user.name.copy(
                            error = currentState.user.name.value.takeIf { it.isNotEmpty() }?.let {
                                validateName(it).errorOrNull()
                            }
                        ),
                        bio = currentState.user.bio.copy(
                            error = currentState.user.bio.value.takeIf { it.isNotEmpty() }?.let {
                                validateBio(it).errorOrNull()
                            }
                        ),
                        email = currentState.user.email.copy(
                            error = currentState.user.email.value.takeIf { it.isNotEmpty() }?.let {
                                validateEmail(it).errorOrNull()
                            }
                        ),
                        tel = currentState.user.tel.copy(
                            error = currentState.user.tel.value.takeIf { it.isNotEmpty() }?.let {
                                validateTelephone(it.removePrefix("+")).errorOrNull()
                            }
                        ),
                        avatar = currentState.user.avatar.copy(
                            error = when (currentState.user.avatar.value) {
                                is EditingState.Editing -> validateImage(currentState.user.avatar.value.value!!).errorOrNull()
                                else -> null
                            }
                        )
                    )
                )
            }
            val currentState = (_state.value as? ScreenState.Editing) ?: return@launch
            val user = currentState.user
            if (
                user.nick.error != null && user.nick.error !is NetworkError
                || user.name.error != null
                || user.bio.error != null
                || user.avatar.error != null
                || user.email.error != null
                || user.tel.error != null
            ) {
                return@launch
            }
            editUser(
                user = UserEditingModel(
                    userId = user.userId,
                    nick = user.nick.value,
                    name = user.name.value.takeIf { it.isNotEmpty() },
                    bio = user.bio.value.takeIf { it.isNotEmpty() },
                    tel = user.tel.value.takeIf { it.isNotEmpty() }?.removePrefix("+"),
                    email = user.email.value.takeIf { it.isNotEmpty() },
                    avatar = when (user.avatar.value) {
                        is EditingState.Editing -> EditingAction.Edit(user.avatar.value.value!!)
                        is EditingState.Keeping -> EditingAction.Keep
                        EditingState.Removing -> EditingAction.Remove
                    }
                )
            ).onSuccess {
                viewModelScope.launch {
                    _events.emit(ScreenEvent.Success(user.userId))
                }
            }.onFailure { error ->
                _state.update {
                    val currentState = (it as? ScreenState.Editing)?: return@update it
                    if (error !is UserEditingError) {
                        currentState.copy(isLoading = false)
                    } else {
                        currentState.copy(
                            user = currentState.user.copy(
                                nick = currentState.user.nick.copy(error = error.nickError),
                                name = currentState.user.name.copy(error = error.nameError),
                                bio = currentState.user.bio.copy(error = error.bioError),
                                email = currentState.user.email.copy(error = error.emailError),
                                tel = currentState.user.tel.copy(error = error.telError),
                                avatar = currentState.user.avatar.copy(error = error.avatarError)
                            ),
                            isLoading = false
                        )
                    }
                }
                if (error !is UserEditingError) {
                    viewModelScope.launch {
                        _events.emit(ScreenEvent.Failure(error))
                    }
                }
            }
        }
    }

    private fun remove() {
        _state.update {
            val currentState = it as? ScreenState.Editing
            if (
                currentState == null
                || currentState.isRemoving
                ) {
                return
            }
            currentState.copy(isRemoving = true)
        }
        viewModelScope.launch {
            unsubscribeFromNotifications()
            player.stop()
            player.clearMediaItems()
            removeUser().onSuccess {
                viewModelScope.launch {
                    removeUserDetails()
                    _events.emit(ScreenEvent.Removed)
                }
                _state.update {
                    val currentState = (_state.value as? ScreenState.Editing)?: return@onSuccess
                    currentState.copy(isRemoving = false)
                }
            }.onFailure { error ->
                viewModelScope.launch {
                    _events.emit(ScreenEvent.Failure(error))
                }
                _state.update {
                    val currentState = (_state.value as? ScreenState.Editing)?: return@onFailure
                    currentState.copy(isRemoving = false)
                }
            }
        }
    }
}