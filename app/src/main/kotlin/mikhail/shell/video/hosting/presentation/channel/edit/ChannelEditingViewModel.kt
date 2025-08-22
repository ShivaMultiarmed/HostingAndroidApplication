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
import mikhail.shell.video.hosting.domain.usecases.channels.validation.ValidateChannelDescription
import mikhail.shell.video.hosting.domain.usecases.channels.validation.ValidateChannelTitle
import mikhail.shell.video.hosting.domain.usecases.channels.validation.ValidateImage

@HiltViewModel(assistedFactory = ChannelEditingViewModel.Factory::class)
class ChannelEditingViewModel @AssistedInject constructor(
    @Assisted("channelId") private val channelId: Long,
    private val getChannel: GetChannel,
    private val validateChannelTitle: ValidateChannelTitle,
    private val validateChannelAlias: ValidateChannelAlias,
    private val validateImage: ValidateImage,
    private val validateChannelDescription: ValidateChannelDescription,
    private val editChannel: EditChannel
) : ViewModel() {

    private val _state =
        MutableStateFlow<ChannelEditingScreenState>(ChannelEditingScreenState.Initializing())
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
                        it as ChannelEditingScreenState.Initializing
                        it.copy(error = error)
                    }
                }
        }
    }

    fun onEvent(event: ChannelEditingUiEvent) {
        viewModelScope.launch {
            when (event) {
                is ChannelEditingUiEvent.AliasChanged -> onAliasChanged(event.alias)
                ChannelEditingUiEvent.AuthenticationRequired -> Unit
                ChannelEditingUiEvent.Cancel -> Unit
                is ChannelEditingUiEvent.DescriptionChanged -> onDescriptionChanged(event.description)
                is ChannelEditingUiEvent.HeaderChanged -> onHeaderChanged(event.header)
                is ChannelEditingUiEvent.LogoChanged -> onLogoChanged(event.logo)
                ChannelEditingUiEvent.Submit -> edit()
                ChannelEditingUiEvent.Success -> Unit
                is ChannelEditingUiEvent.TitleChanged -> onTitleChanged(event.title)
                is ChannelEditingUiEvent.HeaderExists -> onHeaderExists(event.exists)
                is ChannelEditingUiEvent.LogoExists -> onLogoExists(event.exists)
                ChannelEditingUiEvent.Retry -> load()
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

    private suspend fun onAliasChanged(alias: String) {
        _state.update {
            it as ChannelEditingScreenState.Editing
            it.copy(
                editedChannel = it.editedChannel.copy(
                    alias = alias,
                    aliasError = alias.takeIf { it.isNotEmpty() }?.let {
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
                    title = title,
                    titleError = title.let {
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
                    description = description,
                    descriptionError = description.takeIf { it.isNotEmpty() }?.let {
                        val validationResult = validateChannelDescription(it)
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