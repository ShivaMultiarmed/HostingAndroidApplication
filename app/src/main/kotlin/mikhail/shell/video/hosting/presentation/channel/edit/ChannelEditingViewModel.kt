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
import mikhail.shell.video.hosting.domain.models.Channel
import mikhail.shell.video.hosting.domain.models.Result
import mikhail.shell.video.hosting.domain.usecases.channels.EditChannel
import mikhail.shell.video.hosting.domain.usecases.channels.GetChannel
import mikhail.shell.video.hosting.domain.usecases.channels.validation.ValidateChannelAlias
import mikhail.shell.video.hosting.domain.usecases.channels.validation.ValidateChannelTitle
import mikhail.shell.video.hosting.domain.usecases.channels.validation.ValidateImage
import mikhail.shell.video.hosting.domain.utils.ValidateDescription

@HiltViewModel(assistedFactory = ChannelEditingViewModel.Factory::class)
class ChannelEditingViewModel @AssistedInject constructor(
    @Assisted("channelId") private val channelId: Long,
    private val getChannel: GetChannel,
    private val validateChannelTitle: ValidateChannelTitle,
    private val validateChannelAlias: ValidateChannelAlias,
    private val validateImage: ValidateImage,
    private val validateDescription: ValidateDescription,
    private val editChannel: EditChannel
) : ViewModel() {

    private val _state =
        MutableStateFlow<ChannelEditingScreenState>(ChannelEditingScreenState.Loading)
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
                                logo = initialChannel.logo!!,
                                header = initialChannel.header!!
                            ),
                            editedChannel = ChannelEditingInputState(
                                title = initialChannel.title,
                                alias = initialChannel.alias ?: "",
                                description = initialChannel.description ?: "",
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
        viewModelScope.launch {
            when (event) {
                is ChannelEditingUiEvent.AliasChanged -> onAliasChanged(event.alias)
                ChannelEditingUiEvent.AliasTypingStarted -> onAliasTypingStarted()
                ChannelEditingUiEvent.AliasTypingEnded -> onAliasTypingEnded()
                is ChannelEditingUiEvent.DescriptionChanged -> onDescriptionChanged(event.description)
                ChannelEditingUiEvent.DescriptionTypingStarted -> onDescriptionTypingStarted()
                ChannelEditingUiEvent.DescriptionTypingEnded -> onDescriptionTypingEnded()
                is ChannelEditingUiEvent.HeaderChanged -> onHeaderChanged(event.header)
                is ChannelEditingUiEvent.LogoChanged -> onLogoChanged(event.logo)
                ChannelEditingUiEvent.Submit -> edit()
                is ChannelEditingUiEvent.TitleChanged -> onTitleChanged(event.title)
                ChannelEditingUiEvent.TitleTypingStarted -> onTitleTypingStarted()
                ChannelEditingUiEvent.TitleTypingEnded -> onTitleTypingEnded()
                is ChannelEditingUiEvent.HeaderExists -> onHeaderExists(event.exists)
                is ChannelEditingUiEvent.LogoExists -> onLogoExists(event.exists)
                ChannelEditingUiEvent.Retry -> load()
                else -> Unit
            }
        }
    }

    private fun onHeaderExists(exists: Boolean) {
        _state.update {
            it as ChannelEditingScreenState.Editing
            it.copy(
                initialChannel = it.initialChannel.copy(headerExists = exists)
            )
        }
    }

    private fun onLogoExists(exists: Boolean) {
        _state.update {
            it as ChannelEditingScreenState.Editing
            it.copy(
                initialChannel = it.initialChannel.copy(logoExists = exists)
            )
        }
    }

    private fun onAliasChanged(alias: String) {
        _state.update {
            it as ChannelEditingScreenState.Editing
            it.copy(
                editedChannel = it.editedChannel.copy(
                    alias = alias
                )
            )
        }
    }
    private fun onAliasTypingStarted() {
        _state.update {
            it as ChannelEditingScreenState.Editing
            it.copy(
                editedChannel = it.editedChannel.copy(
                    aliasError = null
                )
            )
        }
    }
    private suspend fun onAliasTypingEnded() {
        _state.update {
            it as ChannelEditingScreenState.Editing
            it.copy(
                editedChannel = it.editedChannel.copy(
                    aliasError = it.editedChannel.alias.takeIf { it.isNotEmpty() }?.let {
                        val validationResult = validateChannelAlias(it)
                        if (validationResult is Result.Failure) validationResult.error else null
                    }
                )
            )
        }
    }

    private suspend fun onTitleChanged(title: String) {
        _state.update {
            it as ChannelEditingScreenState.Editing
            it.copy(
                editedChannel = it.editedChannel.copy(
                    title = title
                )
            )
        }
    }

    private suspend fun onTitleTypingStarted() {
        _state.update {
            it as ChannelEditingScreenState.Editing
            it.copy(
                editedChannel = it.editedChannel.copy(
                    titleError = null
                )
            )
        }
    }

    private suspend fun onTitleTypingEnded() {
        _state.update {
            it as ChannelEditingScreenState.Editing
            it.copy(
                editedChannel = it.editedChannel.copy(
                    titleError = it.editedChannel.title.let {
                        val validationResult = validateChannelTitle(it)
                        if (validationResult is Result.Failure) validationResult.error else null
                    }
                )
            )
        }
    }

    private fun onDescriptionChanged(description: String) {
        _state.update {
            it as ChannelEditingScreenState.Editing
            it.copy(
                editedChannel = it.editedChannel.copy(
                    description = description
                )
            )
        }
    }
    private fun onDescriptionTypingStarted() {
        _state.update {
            it as ChannelEditingScreenState.Editing
            it.copy(
                editedChannel = it.editedChannel.copy(
                    descriptionError = null
                )
            )
        }
    }
    private fun onDescriptionTypingEnded() {
        _state.update {
            it as ChannelEditingScreenState.Editing
            it.copy(
                editedChannel = it.editedChannel.copy(
                    descriptionError = it.editedChannel.description.takeIf { it.isNotEmpty() }?.let {
                        val validationResult = validateDescription(it)
                        if (validationResult is Result.Failure) validationResult.error else null
                    }
                )
            )
        }
    }

    private suspend fun onHeaderChanged(header: String?) {
        _state.update {
            it as ChannelEditingScreenState.Editing
            it.copy(
                editedChannel = it.editedChannel.copy(
                    header = header,
                    headerError = header?.let {
                        val validationResult = validateImage(it)
                        if (validationResult is Result.Failure) validationResult.error else null
                    }
                )
            )
        }
    }

    private suspend fun onLogoChanged(logo: String?) {
        _state.update {
            it as ChannelEditingScreenState.Editing
            it.copy(
                editedChannel = it.editedChannel.copy(
                    logo = logo,
                    logoError = logo?.let {
                        val validationResult = validateImage(it)
                        if (validationResult is Result.Failure) validationResult.error else null
                    }
                )
            )
        }
    }

    private fun edit() {
        val currentState = (_state.value as ChannelEditingScreenState.Editing)
        _state.update {
            it as ChannelEditingScreenState.Editing
            it.copy(isLoading = true)
        }
        viewModelScope.launch {
            val channel = Channel(
                channelId = channelId,
                ownerId = currentState.initialChannel.ownerId,
                title = currentState.editedChannel.title,
                alias = currentState.editedChannel.alias.ifEmpty { null },
                description = currentState.editedChannel.description.ifEmpty { null },
            )
            editChannel(
                channel = channel,
                header = currentState.editedChannel.header,
                headerAction = currentState.editedChannel.headerAction,
                logo = currentState.editedChannel.logo,
                logoAction = currentState.editedChannel.logoAction
            ).onSuccess { editedChannel ->
                _state.update {
                    ChannelEditingScreenState.Success
                }
            }.onFailure { error ->
                _state.update {
                    it as ChannelEditingScreenState.Editing
                    it.copy(
                        error = error,
                        isLoading = false
                    )
                }
            }
        }
    }

    @AssistedFactory
    interface Factory {
        fun create(@Assisted("channelId") channelId: Long): ChannelEditingViewModel
    }
}