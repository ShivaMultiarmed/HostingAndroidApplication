package mikhail.shell.video.hosting.presentation.channel.edit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import mikhail.shell.video.hosting.domain.ImageSize
import mikhail.shell.video.hosting.domain.errors.channel.ChannelEditingError
import mikhail.shell.video.hosting.domain.errors.network.NetworkError
import mikhail.shell.video.hosting.domain.models.ChannelEditingModel
import mikhail.shell.video.hosting.domain.models.EditAction
import mikhail.shell.video.hosting.domain.models.Result
import mikhail.shell.video.hosting.domain.usecases.channels.EditChannel
import mikhail.shell.video.hosting.domain.usecases.channels.GetChannel
import mikhail.shell.video.hosting.domain.usecases.channels.validation.ValidateChannelAlias
import mikhail.shell.video.hosting.domain.usecases.channels.validation.ValidateChannelTitle
import mikhail.shell.video.hosting.domain.utils.GetChannelHeaderUrl
import mikhail.shell.video.hosting.domain.utils.GetChannelLogoUrl
import mikhail.shell.video.hosting.domain.utils.ValidateDescription
import mikhail.shell.video.hosting.domain.utils.ValidateImage
import mikhail.shell.video.hosting.presentation.utils.FieldState

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
        MutableStateFlow<ChannelEditingScreenState>(ChannelEditingScreenState.Starting)
    val state = _state
        .onStart {
            load()
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(3000),
            initialValue = _state.value
        )

    private fun load() {
        viewModelScope.launch {
            getChannel(channelId)
                .onSuccess { initialChannel ->
                    _state.update {
                        ChannelEditingScreenState.Editing(
                            initialChannel = EditableChannelUi(
                                channelId = channelId,
                                ownerId = initialChannel.ownerId,
                                title = initialChannel.title,
                                alias = initialChannel.alias ?: "",
                                description = initialChannel.description ?: "",
                                logo = getChannelLogoUrl(
                                    initialChannel.channelId!!,
                                    ImageSize.MEDIUM
                                ),
                                header = getChannelHeaderUrl(
                                    initialChannel.channelId,
                                    ImageSize.MEDIUM
                                )
                            ),
                            editedChannel = ChannelEditingInputState(
                                title = FieldState(initialChannel.title),
                                alias = FieldState(initialChannel.alias ?: ""),
                                description = FieldState(initialChannel.description ?: ""),
                            ),
                            error = null,
                            isLoading = false
                        )
                    }
                }.onFailure { error ->
                    _state.update {
                        ChannelEditingScreenState.Failure(error = error)
                    }
                }
        }
    }

    fun onEvent(event: ChannelEditingUiEvent) {
        when (event) {
            is ChannelEditingUiEvent.AliasChanged -> onAliasChanged(event.alias)
            ChannelEditingUiEvent.AliasFocused -> onAliasFocused()
            ChannelEditingUiEvent.AliasBlurred -> onAliasBlurred()
            is ChannelEditingUiEvent.DescriptionChanged -> onDescriptionChanged(event.description)
            ChannelEditingUiEvent.DescriptionFocused -> onDescriptionFocused()
            ChannelEditingUiEvent.DescriptionBlurred -> onDescriptionBlurred()
            is ChannelEditingUiEvent.HeaderChanged -> onHeaderChanged(event.header, event.action)
            is ChannelEditingUiEvent.LogoChanged -> onLogoChanged(event.logo, event.action)
            is ChannelEditingUiEvent.TitleChanged -> onTitleChanged(event.title)
            ChannelEditingUiEvent.TitleFocused -> onTitleFocused()
            ChannelEditingUiEvent.TitleBlurred -> onTitleBlurred()
            is ChannelEditingUiEvent.HeaderExists -> onHeaderExists(event.exists)
            is ChannelEditingUiEvent.LogoExists -> onLogoExists(event.exists)
            ChannelEditingUiEvent.Restart -> load()
            ChannelEditingUiEvent.Submit -> edit()
            else -> Unit
        }
    }

    private fun onHeaderExists(exists: Boolean) {
        _state.update {
            val currentState = it as? ChannelEditingScreenState.Editing
            currentState?.copy(
                initialChannel = currentState.initialChannel.copy(headerExists = exists)
            ) ?: it
        }
    }

    private fun onLogoExists(exists: Boolean) {
        _state.update {
            val currentState = it as? ChannelEditingScreenState.Editing
            currentState?.copy(
                initialChannel = currentState.initialChannel.copy(logoExists = exists)
            ) ?: it
        }
    }

    private fun onAliasChanged(alias: String) {
        _state.update {
            val currentState = it as? ChannelEditingScreenState.Editing
            currentState?.copy(
                editedChannel = currentState.editedChannel.copy(
                    alias = currentState.editedChannel.alias.copy(
                        value = alias
                    )
                )
            ) ?: it
        }
    }

    private fun onAliasFocused() {
        _state.update {
            val currentState = it as? ChannelEditingScreenState.Editing
            currentState?.copy(
                editedChannel = currentState.editedChannel.copy(
                    alias = currentState.editedChannel.alias.copy(
                        error = null
                    )
                )
            ) ?: it
        }
    }

    private fun onAliasBlurred() {
        viewModelScope.launch {
            _state.update {
                val currentState = it as? ChannelEditingScreenState.Editing
                currentState?.copy(
                    editedChannel = currentState.editedChannel.copy(
                        alias = currentState.editedChannel.alias.copy(
                            error = currentState.editedChannel.alias.value.takeIf { it.isNotEmpty() }
                                ?.let {
                                    val validationResult =
                                        validateChannelAlias(channelId = channelId, alias = it)
                                    if (validationResult is Result.Failure) validationResult.error else null
                                }
                        )
                    )
                ) ?: it
            }
        }
    }

    private fun onTitleChanged(title: String) {
        _state.update {
            val currentState = it as? ChannelEditingScreenState.Editing
            currentState?.copy(
                editedChannel = currentState.editedChannel.copy(
                    title = currentState.editedChannel.title.copy(
                        value = title
                    )
                )
            ) ?: it
        }
    }

    private fun onTitleFocused() {
        _state.update {
            val currentState = it as? ChannelEditingScreenState.Editing
            currentState?.copy(
                editedChannel = currentState.editedChannel.copy(
                    title = currentState.editedChannel.title.copy(
                        error = null
                    )
                )
            ) ?: it
        }
    }

    private fun onTitleBlurred() {
        viewModelScope.launch {
            _state.update {
                val currentState = it as? ChannelEditingScreenState.Editing
                currentState?.copy(
                    editedChannel = currentState.editedChannel.copy(
                        title = currentState.editedChannel.title.copy(
                            error = currentState.editedChannel.title.value.let {
                                val validationResult =
                                    validateChannelTitle(channelId = channelId, title = it)
                                if (validationResult is Result.Failure) validationResult.error else null
                            }
                        )
                    )
                ) ?: it
            }
        }
    }

    private fun onDescriptionChanged(description: String) {
        _state.update {
            val currentState = it as? ChannelEditingScreenState.Editing
            currentState?.copy(
                editedChannel = currentState.editedChannel.copy(
                    description = currentState.editedChannel.description.copy(
                        value = description
                    )
                )
            ) ?: it
        }
    }

    private fun onDescriptionFocused() {
        _state.update {
            val currentState = it as? ChannelEditingScreenState.Editing
            currentState?.copy(
                editedChannel = currentState.editedChannel.copy(
                    description = currentState.editedChannel.description.copy(
                        error = null
                    )
                )
            ) ?: it
        }
    }

    private fun onDescriptionBlurred() {
        _state.update {
            val currentState = it as? ChannelEditingScreenState.Editing
            currentState?.copy(
                editedChannel = currentState.editedChannel.copy(
                    description = currentState.editedChannel.description.copy(
                        error = currentState.editedChannel.description.value.takeIf { it.isNotEmpty() }
                            ?.let {
                                val validationResult = validateDescription(it)
                                if (validationResult is Result.Failure) validationResult.error else null
                            }
                    )
                )
            ) ?: it
        }
    }

    private fun onHeaderChanged(header: String?, action: EditAction) {
        _state.update {
            val currentState = it as? ChannelEditingScreenState.Editing
            currentState?.copy(
                editedChannel = currentState.editedChannel.copy(
                    header = currentState.editedChannel.header.copy(
                        value = header,
                        error = header?.let {
                            val validationResult = validateImage(it)
                            if (validationResult is Result.Failure) validationResult.error else null
                        }
                    ),
                    headerAction = action
                )
            ) ?: it
        }
    }

    private fun onLogoChanged(logo: String?, action: EditAction) {
        _state.update {
            val currentState = it as? ChannelEditingScreenState.Editing
            currentState?.copy(
                editedChannel = currentState.editedChannel.copy(
                    logo = currentState.editedChannel.logo.copy(
                        value = logo,
                        error = logo?.let {
                            val validationResult = validateImage(it)
                            if (validationResult is Result.Failure) validationResult.error else null
                        }
                    ),
                    logoAction = action
                )
            ) ?: it
        }
    }

    private fun edit() {
        viewModelScope.launch {
            _state.update {
                val currentState = it as? ChannelEditingScreenState.Editing
                currentState?.copy(
                    editedChannel = currentState.editedChannel.copy(
                        title = currentState.editedChannel.title.copy(
                            error = currentState.editedChannel.title.value.let {
                                val validationResult =
                                    validateChannelTitle(channelId = channelId, title = it)
                                if (validationResult is Result.Failure) validationResult.error else null
                            }
                        ),
                        alias = currentState.editedChannel.alias.copy(
                            error = currentState.editedChannel.alias.value.takeIf { it.isNotEmpty() }
                                ?.let {
                                    val validationResult =
                                        validateChannelAlias(channelId = channelId, alias = it)
                                    if (validationResult is Result.Failure) validationResult.error else null
                                }
                        ),
                        logo = currentState.editedChannel.logo.copy(
                            value = currentState.editedChannel.logo.value,
                            error = currentState.editedChannel.logo.value?.let {
                                val validationResult = validateImage(it)
                                if (validationResult is Result.Failure) validationResult.error else null
                            }
                        ),
                        header = currentState.editedChannel.header.copy(
                            value = currentState.editedChannel.header.value,
                            error = currentState.editedChannel.header.value?.let {
                                val validationResult = validateImage(it)
                                if (validationResult is Result.Failure) validationResult.error else null
                            }
                        ),
                        description = currentState.editedChannel.description.copy(
                            error = currentState.editedChannel.description.value.takeIf { it.isNotEmpty() }
                                ?.let {
                                    val validationResult = validateDescription(it)
                                    if (validationResult is Result.Failure) validationResult.error else null
                                }
                        )
                    )
                ) ?: it
            }
            val currentState = _state.value as? ChannelEditingScreenState.Editing
            if (currentState == null || currentState.editedChannel.title.error != null && currentState.editedChannel.title.error !is NetworkError
                || currentState.editedChannel.alias.error != null && currentState.editedChannel.alias.error !is NetworkError
                || currentState.editedChannel.header.error != null
                || currentState.editedChannel.logo.error != null
                || currentState.editedChannel.description.error != null) {
                return@launch
            }
            _state.update {
                val currentState = it as? ChannelEditingScreenState.Editing
                currentState?.copy(isLoading = true) ?: it
            }
            editChannel(
                channel = ChannelEditingModel(
                    channelId = channelId,
                    title = currentState.editedChannel.title.value,
                    alias = currentState.editedChannel.alias.value.takeIf { it.isNotEmpty() },
                    description = currentState.editedChannel.description.value.takeIf { it.isNotEmpty() },
                    header = currentState.editedChannel.header.value,
                    headerAction = currentState.editedChannel.headerAction,
                    logo = currentState.editedChannel.logo.value,
                    logoAction = currentState.editedChannel.logoAction
                )
            ).onSuccess { editedChannel ->
                _state.update {
                    ChannelEditingScreenState.Success
                }
            }.onFailure { error ->
                _state.update {
                    val currentState = it as? ChannelEditingScreenState.Editing
                    if (error !is ChannelEditingError) {
                        currentState?.copy(
                            error = error,
                            isLoading = false
                        ) ?: it
                    } else {
                        currentState?.copy(
                            editedChannel = currentState.editedChannel.copy(
                                title = currentState.editedChannel.title.copy(
                                    error = error.titleError
                                ),
                                alias = currentState.editedChannel.alias.copy(
                                    error = error.aliasError
                                ),
                                logo = currentState.editedChannel.logo.copy(
                                    error = error.logoError
                                ),
                                header = currentState.editedChannel.header.copy(
                                    error = error.headerError
                                ),
                                description = currentState.editedChannel.description.copy(
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