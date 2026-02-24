package mikhail.shell.video.hosting.presentation.channel.edit

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import mikhail.shell.video.hosting.domain.errors.channel.ChannelEditingError
import mikhail.shell.video.hosting.domain.errors.network.NetworkError
import mikhail.shell.video.hosting.domain.models.ChannelEditingModel
import mikhail.shell.video.hosting.domain.models.EditingAction
import mikhail.shell.video.hosting.domain.models.ImageSize
import mikhail.shell.video.hosting.domain.models.Result
import mikhail.shell.video.hosting.domain.models.errorOrNull
import mikhail.shell.video.hosting.domain.usecases.channels.EditChannel
import mikhail.shell.video.hosting.domain.usecases.channels.GetChannel
import mikhail.shell.video.hosting.domain.usecases.channels.validation.ValidateChannelAlias
import mikhail.shell.video.hosting.domain.usecases.channels.validation.ValidateChannelTitle
import mikhail.shell.video.hosting.domain.utils.GetChannelHeaderUrl
import mikhail.shell.video.hosting.domain.utils.GetChannelLogoUrl
import mikhail.shell.video.hosting.domain.utils.ValidateDescription
import mikhail.shell.video.hosting.domain.utils.ValidateImage
import mikhail.shell.video.hosting.presentation.utils.EditingState
import mikhail.shell.video.hosting.presentation.utils.stateIn
import mikhail.shell.video.hosting.presentation.channel.edit.ChannelEditingScreenAction as ScreenAction
import mikhail.shell.video.hosting.presentation.channel.edit.ChannelEditingScreenEvent as ScreenEvent
import mikhail.shell.video.hosting.presentation.channel.edit.ChannelEditingScreenState as ScreenState

@HiltViewModel(assistedFactory = ChannelEditingViewModel.Factory::class)
class ChannelEditingViewModel @AssistedInject constructor(
    private val savedStateHandle: SavedStateHandle,
    @Assisted("channelId") private val channelId: Long,
    private val getChannel: GetChannel,
    private val getChannelLogoUrl: GetChannelLogoUrl,
    private val getChannelHeaderUrl: GetChannelHeaderUrl,
    private val validateChannelTitle: ValidateChannelTitle,
    private val validateChannelAlias: ValidateChannelAlias,
    private val validateImage: ValidateImage,
    private val validateDescription: ValidateDescription,
    private val editChannel: EditChannel
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
            is ScreenAction.ChangeAlias -> onAliasChanged(action.alias)
            ScreenAction.FocusAlias -> onAliasFocused()
            ScreenAction.BlurAlias -> onAliasBlurred()
            is ScreenAction.ChangeDescription -> onDescriptionChanged(action.description)
            ScreenAction.FocusDescription -> onDescriptionFocused()
            ScreenAction.BlurDescription -> onDescriptionBlurred()
            is ScreenAction.ChangeHeader -> onHeaderChanged(action.editingState)
            is ScreenAction.ChangeLogo -> onLogoChanged(action.editingState)
            is ScreenAction.ChangeTitle -> onTitleChanged(action.title)
            ScreenAction.FocusTitle -> onTitleFocused()
            ScreenAction.BlurTitle -> onTitleBlurred()
            ScreenAction.Restart -> start()
            ScreenAction.Submit -> edit()
            ScreenAction.Cancel -> viewModelScope.launch {
                _events.emit(ScreenEvent.Cancelled)
            }
        }
    }

    private fun start() {
        _state.update {
            ScreenState.Starting
        }
        viewModelScope.launch {
            getChannel(channelId).onSuccess { initialChannel ->
                _state.update {
                    ScreenState.Editing(
                        channel = ChannelEditingInputState.initialize(
                            channelId = channelId,
                            title = initialChannel.title,
                            alias = initialChannel.alias ?: "",
                            description = initialChannel.description ?: "",
                            header = getChannelHeaderUrl(initialChannel.channelId, ImageSize.MEDIUM),
                            logo = getChannelLogoUrl(initialChannel.channelId,ImageSize.MEDIUM)
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

    private fun onAliasChanged(alias: String) {
        _state.update {
            val currentState = it as? ScreenState.Editing
            currentState?.copy(
                channel = currentState.channel.copy(
                    alias = currentState.channel.alias.copy(value = alias)
                )
            ) ?: it
        }
    }

    private fun onAliasFocused() {
        _state.update {
            val currentState = it as? ScreenState.Editing
            currentState?.copy(
                channel = currentState.channel.copy(
                    alias = currentState.channel.alias.copy(error = null)
                )
            ) ?: it
        }
    }

    private fun onAliasBlurred() {
        viewModelScope.launch {
            _state.update {
                val currentState = it as? ScreenState.Editing
                currentState?.copy(
                    channel = currentState.channel.copy(
                        alias = currentState.channel.alias.copy(
                            error = currentState.channel.alias.value.takeIf { it.isNotEmpty() }
                                ?.let {
                                    validateChannelAlias(
                                        channelId = channelId,
                                        alias = it
                                    ).errorOrNull()
                                }
                        )
                    )
                ) ?: it
            }
        }
    }

    private fun onTitleChanged(title: String) {
        _state.update {
            val currentState = it as? ScreenState.Editing
            currentState?.copy(
                channel = currentState.channel.copy(
                    title = currentState.channel.title.copy(value = title)
                )
            ) ?: it
        }
    }

    private fun onTitleFocused() {
        _state.update {
            val currentState = it as? ScreenState.Editing
            currentState?.copy(
                channel = currentState.channel.copy(
                    title = currentState.channel.title.copy(error = null)
                )
            ) ?: it
        }
    }

    private fun onTitleBlurred() {
        viewModelScope.launch {
            _state.update {
                val currentState = it as? ScreenState.Editing
                currentState?.copy(
                    channel = currentState.channel.copy(
                        title = currentState.channel.title.copy(
                            error = validateChannelTitle(
                                channelId = channelId,
                                title = currentState.channel.title.value
                            ).errorOrNull()
                        )
                    )
                ) ?: it
            }
        }
    }

    private fun onDescriptionChanged(description: String) {
        _state.update {
            val currentState = it as? ScreenState.Editing
            currentState?.copy(
                channel = currentState.channel.copy(
                    description = currentState.channel.description.copy(value = description)
                )
            ) ?: it
        }
    }

    private fun onDescriptionFocused() {
        _state.update {
            val currentState = it as? ScreenState.Editing
            currentState?.copy(
                channel = currentState.channel.copy(
                    description = currentState.channel.description.copy(error = null)
                )
            ) ?: it
        }
    }

    private fun onDescriptionBlurred() {
        _state.update {
            val currentState = it as? ScreenState.Editing
            currentState?.copy(
                channel = currentState.channel.copy(
                    description = currentState.channel.description.copy(
                        error = currentState.channel.description.value.takeIf { it.isNotEmpty() }
                            ?.let {
                                validateDescription(it).errorOrNull()
                            }
                    )
                )
            ) ?: it
        }
    }

    private fun onHeaderChanged(editingState: EditingState<String?>) {
        _state.update {
            val currentState = it as? ScreenState.Editing
            currentState?.copy(
                channel = currentState.channel.copy(
                    header = currentState.channel.header.copy(
                        value = editingState,
                        error = when (editingState) {
                            is EditingState.Editing -> validateImage(editingState.value!!).errorOrNull()
                            else -> null
                        }
                    )
                )
            ) ?: it
        }
    }

    private fun onLogoChanged(editingState: EditingState<String?>) {
        _state.update {
            val currentState = it as? ScreenState.Editing
            currentState?.copy(
                channel = currentState.channel.copy(
                    logo = currentState.channel.logo.copy(
                        value = editingState,
                        error = when (editingState) {
                            is EditingState.Editing -> validateImage(editingState.value!!).errorOrNull()
                            else -> null
                        }
                    )
                )
            ) ?: it
        }
    }

    private fun edit() {
        viewModelScope.launch {
            _state.update {
                val currentState = it as? ScreenState.Editing
                if (
                    currentState == null
                    || currentState.isLoading
                    ) {
                    return@launch
                }
                currentState.copy(
                    channel = currentState.channel.copy(
                        title = currentState.channel.title.copy(
                            error = currentState.channel.title.value.let {
                                validateChannelTitle(channelId = channelId, title = it).errorOrNull()
                            }
                        ),
                        alias = currentState.channel.alias.copy(
                            error = currentState.channel.alias.value.takeIf { it.isNotEmpty() }
                                ?.let {
                                    validateChannelAlias(channelId = channelId, alias = it).errorOrNull()
                                }
                        ),
                        logo = currentState.channel.logo.copy(
                            value = currentState.channel.logo.value,
                            error = when (currentState.channel.logo.value) {
                                is EditingState.Editing<*> -> validateImage(currentState.channel.logo.value.value as String).errorOrNull()
                                else -> null
                            }
                        ),
                        header = currentState.channel.header.copy(
                            value = currentState.channel.header.value,
                            error = when (currentState.channel.header.value) {
                                is EditingState.Editing<*> -> validateImage(currentState.channel.header.value.value as String).errorOrNull()
                                else -> null
                            }
                        ),
                        description = currentState.channel.description.copy(
                            error = currentState.channel.description.value.takeIf { it.isNotEmpty() }
                                ?.let {
                                    val validationResult = validateDescription(it)
                                    if (validationResult is Result.Failure) validationResult.error else null
                                }
                        )
                    )
                )
            }
            val currentState = _state.value as? ScreenState.Editing
            if (currentState == null || currentState.channel.title.error != null && currentState.channel.title.error !is NetworkError
                || currentState.channel.alias.error != null && currentState.channel.alias.error !is NetworkError
                || currentState.channel.header.error != null
                || currentState.channel.logo.error != null
                || currentState.channel.description.error != null
            ) {
                return@launch
            }
            _state.update {
                val currentState = it as? ScreenState.Editing
                currentState?.copy(isLoading = true) ?: it
            }
            editChannel(
                channel = ChannelEditingModel(
                    channelId = channelId,
                    title = currentState.channel.title.value,
                    alias = currentState.channel.alias.value.takeIf { it.isNotEmpty() },
                    description = currentState.channel.description.value.takeIf { it.isNotEmpty() },
                    header = when (currentState.channel.header.value) {
                        is EditingState.Editing -> EditingAction.Edit(currentState.channel.header.value.value!!)
                        is EditingState.Keeping -> EditingAction.Keep
                        is EditingState.Removing -> EditingAction.Remove
                    },
                    logo = when (currentState.channel.logo.value) {
                        is EditingState.Editing -> EditingAction.Edit(currentState.channel.logo.value.value!!)
                        is EditingState.Keeping -> EditingAction.Keep
                        is EditingState.Removing -> EditingAction.Remove
                    }
                )
            ).onSuccess {
                viewModelScope.launch {
                    _events.emit(ScreenEvent.Success)
                }
            }.onFailure { error ->
                if (error !is ChannelEditingError) {
                    _state.update {
                        val currentState = it as? ScreenState.Editing
                        currentState?.copy(isLoading = false) ?: it
                    }
                    viewModelScope.launch {
                        _events.emit(ScreenEvent.Failure(error))
                    }
                } else {
                    _state.update {
                        val currentState = it as? ScreenState.Editing
                        currentState?.copy(
                            channel = currentState.channel.copy(
                                title = currentState.channel.title.copy(error = error.titleError),
                                alias = currentState.channel.alias.copy(error = error.aliasError),
                                logo = currentState.channel.logo.copy(error = error.logoError),
                                header = currentState.channel.header.copy(error = error.headerError),
                                description = currentState.channel.description.copy(error = error.descriptionError)
                            ),
                            isLoading = false
                        ) ?: it
                    }
                }
            }
        }
    }
    @AssistedFactory
    interface Factory {
        fun create(@Assisted("channelId") channelId: Long): ChannelEditingViewModel
    }
}