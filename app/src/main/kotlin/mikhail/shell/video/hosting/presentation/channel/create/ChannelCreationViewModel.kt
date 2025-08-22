package mikhail.shell.video.hosting.presentation.channel.create

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import mikhail.shell.video.hosting.domain.models.Channel
import mikhail.shell.video.hosting.domain.models.Result
import mikhail.shell.video.hosting.domain.providers.UserDetailsProvider
import mikhail.shell.video.hosting.domain.usecases.channels.CreateChannel
import mikhail.shell.video.hosting.domain.usecases.channels.validation.ValidateChannelAlias
import mikhail.shell.video.hosting.domain.usecases.channels.validation.ValidateChannelDescription
import mikhail.shell.video.hosting.domain.usecases.channels.validation.ValidateChannelTitle
import mikhail.shell.video.hosting.domain.usecases.channels.validation.ValidateImage

@HiltViewModel
class ChannelCreationViewModel(
    private val validateChannelTitle: ValidateChannelTitle,
    private val validateChannelAlias: ValidateChannelAlias,
    private val validateImage: ValidateImage,
    private val validateChannelDescription: ValidateChannelDescription,
    private val createChannel: CreateChannel,
    userDetailsProvider: UserDetailsProvider
) : ViewModel() {

    private var _state = MutableStateFlow(ChannelCreationScreenState(owner = userDetailsProvider.getUserId()))
    val state = _state.asStateFlow()

    fun onEvent(event: ChannelCreationUiEvent) {
        viewModelScope.launch {
            when (event) {
                is ChannelCreationUiEvent.AliasChanged -> onAliasChanged(event.alias)
                is ChannelCreationUiEvent.HeaderChanged -> onHeaderChanged(event.header)
                is ChannelCreationUiEvent.LogoChanged -> onLogoChanged(event.logo)
                is ChannelCreationUiEvent.TitleChanged -> onTitleChanged(event.title)
                is ChannelCreationUiEvent.Cancel -> Unit
                is ChannelCreationUiEvent.DescriptionChanged -> onDescriptionChanged(event.description)
                is ChannelCreationUiEvent.Success -> Unit
                is ChannelCreationUiEvent.Submit -> onSubmit()
                is ChannelCreationUiEvent.AuthenticationRequired -> Unit
            }
        }
    }
    private suspend fun onAliasChanged(alias: String) {
        _state.update {
            it.copy(
                alias = alias,
                aliasError = alias.takeIf { it.isNotEmpty() }?.let {
                    val validationResult = validateChannelAlias(alias)
                    if (validationResult is Result.Failure) validationResult.error else null
                }
            )
        }
    }

    private suspend fun onHeaderChanged(header: String?) {
        _state.update {
            it.copy(
                header = header,
                headerError = header?.let {
                    val validationResult = validateImage(header)
                    if (validationResult is Result.Failure) validationResult.error else null
                }
            )
        }
    }
    private suspend fun onLogoChanged(logo: String?) {
        _state.update {
            it.copy(
                logo = logo,
                logoError = logo?.let {
                    val validationResult = validateImage(logo)
                    if (validationResult is Result.Failure) validationResult.error else null
                }
            )
        }
    }
    private suspend fun onTitleChanged(title: String) {
        _state.update {
            it.copy(
                title = title,
                titleError = title.let {
                    val validationResult = validateChannelTitle(title)
                    if (validationResult is Result.Failure) validationResult.error else null
                }
            )
        }

    }
    private suspend fun onDescriptionChanged(description: String) {
        _state.update {
            it.copy(
                description = description,
                descriptionError = description.takeIf { it.isNotEmpty() }?.let {
                    val validationResult = validateChannelDescription(description)
                    if (validationResult is Result.Failure) validationResult.error else null
                }
            )
        }
    }
    private suspend fun onSubmit() {
        if (_state.value.titleError != null
            || _state.value.aliasError != null
            || _state.value.logoError != null
            || _state.value.headerError != null
            ) {
            return
        }
        _state.update {
            it.copy(isCreating = true)
        }
        createChannel(
            channel = Channel(
                title = _state.value.title,
                alias = _state.value.alias,
                ownerId = _state.value.owner,
            ),
            logo = _state.value.logo,
            header = _state.value.header
        )
            .onSuccess { channelId ->
                _state.update {
                    it.copy(
                        channelId = channelId,
                        isCreating = false,
                        creationError = null
                    )
                }
            }.onFailure { error ->
                _state.update {
                    it.copy(
                        creationError = error,
                        isCreating = false,
                    )
                }
            }
    }
}