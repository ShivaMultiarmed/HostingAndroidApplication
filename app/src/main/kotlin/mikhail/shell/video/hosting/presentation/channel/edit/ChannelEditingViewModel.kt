package mikhail.shell.video.hosting.presentation.channel.edit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import mikhail.shell.video.hosting.domain.errors.channel.ChannelEditingError
import mikhail.shell.video.hosting.domain.errors.network.NetworkError
import mikhail.shell.video.hosting.domain.models.ChannelEditingModel
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
import mikhail.shell.video.hosting.presentation.channel.edit.ChannelEditingScreenState as ScreenState

@HiltViewModel(assistedFactory = ChannelEditingViewModel.Factory::class)
class ChannelEditingViewModel @AssistedInject constructor(
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
    private val _state =
        MutableStateFlow<ScreenState>(ScreenState.Idle)
    val state = _state.onStart { start() }.stateIn(_state.value)

    private val _events = MutableSharedFlow<ChannelEditingScreenEvent>()
    val events = _events.asSharedFlow()

    fun onAction(action: ChannelEditingScreenAction) {
        when (action) {
            is ChannelEditingScreenAction.ChangeAlias -> onAliasChanged(action.alias)
            ChannelEditingScreenAction.FocusAlias -> onAliasFocused()
            ChannelEditingScreenAction.BlurAlias -> onAliasBlurred()
            is ChannelEditingScreenAction.ChangeDescription -> onDescriptionChanged(action.description)
            ChannelEditingScreenAction.FocusDescription -> onDescriptionFocused()
            ChannelEditingScreenAction.BlurDescription -> onDescriptionBlurred()
            is ChannelEditingScreenAction.ChangeHeader -> onHeaderChanged(action.editingState)
            is ChannelEditingScreenAction.ChangeLogo -> onLogoChanged(action.editingState)
            is ChannelEditingScreenAction.ChangeTitle -> onTitleChanged(action.title)
            ChannelEditingScreenAction.FocusTitle -> onTitleFocused()
            ChannelEditingScreenAction.BlurTitle -> onTitleBlurred()
            ChannelEditingScreenAction.Restart -> start()
            ChannelEditingScreenAction.Submit -> edit()
            ChannelEditingScreenAction.Cancel -> viewModelScope.launch {
                _events.emit(ChannelEditingScreenEvent.Cancelled)
            }
        }
    }

    private fun start() {
        if (
            _state.value is ScreenState.Idle
            || state.value is ScreenState.Failure
        ) {
            return
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
                        ),
                        isLoading = false
                    )
                }
            }.onFailure { error ->
                _state.update {
                    ScreenState.Failure(error)
                }
                viewModelScope.launch {
                    _events.emit(ChannelEditingScreenEvent.Failure(error))
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
                currentState?.copy(
                    channel = currentState.channel.copy(
                        title = currentState.channel.title.copy(
                            error = currentState.channel.title.value.let {
                                val validationResult =
                                    validateChannelTitle(channelId = channelId, title = it)
                                if (validationResult is Result.Failure) validationResult.error else null
                            }
                        ),
                        alias = currentState.channel.alias.copy(
                            error = currentState.channel.alias.value.takeIf { it.isNotEmpty() }
                                ?.let {
                                    val validationResult =
                                        validateChannelAlias(channelId = channelId, alias = it)
                                    if (validationResult is Result.Failure) validationResult.error else null
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
                ) ?: it
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
                    header = currentState.channel.header.value,
                    logo = currentState.channel.logo.value,
                )
            ).onSuccess { editedChannel ->
                _state.update {
                    ScreenState.Success
                }
            }.onFailure { error ->
                if (error !is ChannelEditingError) {
                    _state.update {
                        val currentState = it as? ScreenState.Editing
                        currentState?.copy(isLoading = false) ?: it
                    }
                    viewModelScope.launch {
                        _events.emit(ChannelEditingScreenEvent.Failure(error))
                    }
                } else {
                    _state.update {
                        val currentState = it as? ScreenState.Editing
                        currentState?.copy(
                            channel = currentState.channel.copy(
                                title = currentState.channel.title.copy(
                                    error = error.titleError
                                ),
                                alias = currentState.channel.alias.copy(
                                    error = error.aliasError
                                ),
                                logo = currentState.channel.logo.copy(
                                    error = error.logoError
                                ),
                                header = currentState.channel.header.copy(
                                    error = error.headerError
                                ),
                                description = currentState.channel.description.copy(
                                    error = error.descriptionError
                                )
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