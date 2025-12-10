package mikhail.shell.video.hosting.presentation.channel.create

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import mikhail.shell.video.hosting.domain.errors.channel.ChannelCreationError
import mikhail.shell.video.hosting.domain.errors.network.NetworkError
import mikhail.shell.video.hosting.domain.models.ChannelCreationModel
import mikhail.shell.video.hosting.domain.models.errorOrNull
import mikhail.shell.video.hosting.domain.usecases.channels.CreateChannel
import mikhail.shell.video.hosting.domain.usecases.channels.validation.ValidateChannelAlias
import mikhail.shell.video.hosting.domain.usecases.channels.validation.ValidateChannelTitle
import mikhail.shell.video.hosting.domain.utils.ValidateDescription
import mikhail.shell.video.hosting.domain.utils.ValidateImage

@HiltViewModel(assistedFactory = ChannelCreationViewModel.Factory::class)
class ChannelCreationViewModel @AssistedInject constructor(
    @Assisted("ownerId") private val ownerId: Long,
    private val validateChannelTitle: ValidateChannelTitle,
    private val validateChannelAlias: ValidateChannelAlias,
    private val validateImage: ValidateImage,
    private val validateDescription: ValidateDescription,
    private val createChannel: CreateChannel
) : ViewModel() {

    private val _state = MutableStateFlow(
        ChannelCreationScreenState(
            channel = ChannelCreationInputState(ownerId)
        )
    )
    val state = _state.asStateFlow()

    private val _events = MutableSharedFlow<ChannelCreationScreenEvent>()
    val events = _events.asSharedFlow()

    fun onAction(action: ChannelCreationScreenAction) {
        when (action) {
            is ChannelCreationScreenAction.ChangeAlias -> onAliasChanged(action.alias)
            ChannelCreationScreenAction.FocusAlias -> onAliasFocused()
            ChannelCreationScreenAction.BlurAlias -> onAliasBlurred()
            is ChannelCreationScreenAction.ChangeHeader -> onHeaderChanged(action.header)
            is ChannelCreationScreenAction.ChangeLogo -> onLogoChanged(action.logo)
            is ChannelCreationScreenAction.ChangeTitle -> onTitleChanged(action.title)
            ChannelCreationScreenAction.FocusTitle -> onTitleFocused()
            ChannelCreationScreenAction.BlurTitle -> onTitleBlurred()
            is ChannelCreationScreenAction.ChangeDescription -> onDescriptionChanged(action.description)
            ChannelCreationScreenAction.FocusDescription -> onDescriptionFocused()
            ChannelCreationScreenAction.BlurDescription -> onDescriptionBlurred()
            is ChannelCreationScreenAction.Submit -> create()
            ChannelCreationScreenAction.Cancel -> viewModelScope.launch {
                _events.emit(ChannelCreationScreenEvent.Cancelled)
            }
        }

    }

    private fun onAliasChanged(alias: String) {
        _state.update {
            it.copy(
                channel = it.channel.copy(
                    alias = it.channel.alias.copy(value = alias)
                )
            )
        }
    }

    private fun onAliasFocused() {
        _state.update {
            it.copy(
                channel = it.channel.copy(
                    alias = it.channel.alias.copy(error = null)
                )
            )
        }
    }

    private fun onAliasBlurred() {
        if (_state.value.isLoading) {
            return
        }
        viewModelScope.launch {
            _state.update {
                it.copy(
                    channel = it.channel.copy(
                        alias = it.channel.alias.copy(
                            error = it.channel.alias.value.takeIf { it.isNotEmpty() }?.let {
                                validateChannelAlias(alias = it).errorOrNull()
                            }
                        )
                    )
                )
            }
        }
    }

    private fun onHeaderChanged(header: String?) {
        if (_state.value.isLoading) {
            return
        }
        _state.update {
            it.copy(
                channel = it.channel.copy(
                    header = it.channel.header.copy(
                        value = header,
                        error = header?.let {
                            validateImage(it).errorOrNull()
                        }
                    )
                )
            )
        }
    }

    private fun onLogoChanged(logo: String?) {
        if (_state.value.isLoading) {
            return
        }
        _state.update {
            it.copy(
                channel = it.channel.copy(
                    logo = it.channel.logo.copy(
                        value = logo,
                        error = logo?.let {
                            validateImage(it).errorOrNull()
                        }
                    )
                )
            )
        }
    }

    private fun onTitleChanged(title: String) {
        _state.update {
            it.copy(
                channel = it.channel.copy(
                    title = it.channel.title.copy(value = title)
                )
            )
        }
    }

    private fun onTitleFocused() {
        _state.update {
            it.copy(
                channel = it.channel.copy(
                    title = it.channel.title.copy(error = null)
                )
            )
        }
    }

    private fun onTitleBlurred() {
        if (_state.value.isLoading) {
            return
        }
        viewModelScope.launch {
            _state.update {
                it.copy(
                    channel = it.channel.copy(
                        title = it.channel.title.copy(
                            error = validateChannelTitle(title = it.channel.title.value).errorOrNull()
                        )
                    )
                )
            }
        }
    }

    private fun onDescriptionChanged(description: String) {
        _state.update {
            it.copy(
                channel = it.channel.copy(
                    description = it.channel.description.copy(value = description)
                )
            )
        }
    }

    private fun onDescriptionFocused() {
        _state.update {
            it.copy(
                channel = it.channel.copy(
                    description = it.channel.description.copy(error = null)
                )
            )
        }
    }

    private fun onDescriptionBlurred() {
        if (_state.value.isLoading) {
            return
        }
        viewModelScope.launch {
            _state.update {
                it.copy(
                    channel = it.channel.copy(
                        description = it.channel.description.copy(
                            error = it.channel.description.value.takeIf { it.isNotEmpty() }?.let {
                                validateDescription(it).errorOrNull()
                            }
                        )
                    )
                )
            }
        }
    }

    private fun create() {
        if (_state.value.isLoading) {
            return
        }
        viewModelScope.launch {
            _state.update {
                it.copy(
                    channel = it.channel.copy(
                        title = it.channel.title.copy(
                            error = validateChannelTitle(title = it.channel.title.value).errorOrNull()
                        ),
                        alias = it.channel.alias.copy(
                            error = it.channel.alias.value.takeIf { it.isNotEmpty() }?.let {
                                validateChannelAlias(alias = it).errorOrNull()
                            }
                        ),
                        logo = it.channel.logo.copy(
                            error = it.channel.logo.value?.let {
                                validateImage(it).errorOrNull()
                            }
                        ),
                        header = it.channel.header.copy(
                            error = it.channel.header.value?.let {
                                validateImage(it).errorOrNull()
                            }
                        ),
                        description = it.channel.description.copy(
                            error = it.channel.description.value.takeIf { it.isNotEmpty() }?.let {
                                validateDescription(it).errorOrNull()
                            }
                        )
                    )
                )
            }
            if (_state.value.channel.title.error != null
                && state.value.channel.title.error !is NetworkError
                || _state.value.channel.alias.error != null
                && state.value.channel.alias.error !is NetworkError
                || _state.value.channel.logo.error != null
                || _state.value.channel.header.error != null
                || _state.value.channel.description.error != null
            ) {
                return@launch
            }
            _state.update {
                it.copy(isLoading = true)
            }
            createChannel(
                channel = ChannelCreationModel(
                    title = _state.value.channel.title.value,
                    alias = _state.value.channel.alias.value,
                    ownerId = _state.value.channel.ownerId,
                    description = _state.value.channel.description.value,
                    logo = _state.value.channel.logo.value,
                    header = _state.value.channel.header.value
                )
            ).onSuccess { channelId ->
                _state.update {
                    it.copy(isLoading = false)
                }
                viewModelScope.launch {
                    _events.emit(ChannelCreationScreenEvent.Created(channelId))
                }
            }.onFailure { error ->
                if (error is ChannelCreationError) {
                    _state.update {
                        it.copy(
                            channel = it.channel.copy(
                                title = it.channel.title.copy(error = error.titleError),
                                alias = it.channel.alias.copy(error = error.aliasError),
                            ),
                            isLoading = false
                        )
                    }
                } else {
                    _state.update {
                        it.copy(isLoading = false)
                    }
                    viewModelScope.launch {
                        _events.emit(ChannelCreationScreenEvent.Failure(error))
                    }
                }
            }
        }
    }

    @AssistedFactory
    interface Factory {
        fun create(@Assisted("ownerId") ownerId: Long): ChannelCreationViewModel
    }
}