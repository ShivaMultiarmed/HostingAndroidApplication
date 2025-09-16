package mikhail.shell.video.hosting.presentation.channel.create

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import mikhail.shell.video.hosting.domain.errors.channel.ChannelCreationError
import mikhail.shell.video.hosting.domain.errors.network.NetworkError
import mikhail.shell.video.hosting.domain.models.Channel
import mikhail.shell.video.hosting.domain.models.Result
import mikhail.shell.video.hosting.domain.providers.UserDetailsProvider
import mikhail.shell.video.hosting.domain.usecases.channels.CreateChannel
import mikhail.shell.video.hosting.domain.usecases.channels.validation.ValidateChannelAlias
import mikhail.shell.video.hosting.domain.usecases.channels.validation.ValidateChannelTitle
import mikhail.shell.video.hosting.domain.utils.ValidateDescription
import mikhail.shell.video.hosting.domain.utils.ValidateImage
import javax.inject.Inject

@HiltViewModel
class ChannelCreationViewModel @Inject constructor(
    private val validateChannelTitle: ValidateChannelTitle,
    private val validateChannelAlias: ValidateChannelAlias,
    private val validateImage: ValidateImage,
    private val validateDescription: ValidateDescription,
    private val createChannel: CreateChannel,
    userDetailsProvider: UserDetailsProvider
) : ViewModel() {

    private var _state = MutableStateFlow(ChannelCreationScreenState(owner = userDetailsProvider.getUserId()))
    val state = _state.asStateFlow()

    fun onEvent(event: ChannelCreationUiEvent) {
        viewModelScope.launch {
            when (event) {
                is ChannelCreationUiEvent.AliasChanged -> onAliasChanged(event.alias)
                ChannelCreationUiEvent.AliasFocused -> onAliasFocused()
                ChannelCreationUiEvent.AliasBlurred -> onAliasBlurred()
                is ChannelCreationUiEvent.HeaderChanged -> onHeaderChanged(event.header)
                is ChannelCreationUiEvent.LogoChanged -> onLogoChanged(event.logo)
                is ChannelCreationUiEvent.TitleChanged -> onTitleChanged(event.title)
                ChannelCreationUiEvent.TitleFocused -> onTitleFocused()
                ChannelCreationUiEvent.TitleBlurred -> onTitleBlurred()
                is ChannelCreationUiEvent.DescriptionChanged -> onDescriptionChanged(event.description)
                ChannelCreationUiEvent.DescriptionFocused -> onDescriptionFocused()
                ChannelCreationUiEvent.DescriptionBlurred -> onDescriptionBlurred()
                is ChannelCreationUiEvent.Submit -> onSubmit()
                else -> Unit
            }
        }
    }

    private fun onAliasChanged(alias: String) {
        _state.update {
            it.copy(
                alias = it.alias.copy(
                    value = alias
                )
            )
        }
    }

    private fun onAliasFocused() {
        _state.update {
            it.copy(
                alias = it.alias.copy(
                    error = null
                )
            )
        }
    }

    private suspend fun onAliasBlurred() {
        _state.update {
            it.copy(
                alias = it.alias.copy(
                    error = it.alias.value.takeIf { it.isNotEmpty() }?.let {
                        val validationResult = validateChannelAlias(alias = it)
                        if (validationResult is Result.Failure) validationResult.error else null
                    }
                )
            )
        }
    }

    private fun onHeaderChanged(header: String?) {
        _state.update {
            it.copy(
                header = it.header.copy(
                    value = header,
                    error = header?.let {
                        val validationResult = validateImage(it)
                        if (validationResult is Result.Failure) validationResult.error else null
                    }
                )
            )
        }
    }

    private fun onLogoChanged(logo: String?) {
        _state.update {
            it.copy(
                logo = it.logo.copy(
                    value = logo,
                    error = logo?.let {
                        val validationResult = validateImage(it)
                        if (validationResult is Result.Failure) validationResult.error else null
                    }
                )
            )
        }
    }

    private fun onTitleChanged(title: String) {
        _state.update {
            it.copy(
                title = it.title.copy(
                    value = title
                )
            )
        }
    }

    private fun onTitleFocused() {
        _state.update {
            it.copy(
                title = it.title.copy(
                    error = null
                )
            )
        }
    }

    private suspend fun onTitleBlurred() {
        _state.update {
            it.copy(
                title = it.title.copy(
                    error = it.title.value.let {
                        val validationResult = validateChannelTitle(title = it)
                        if (validationResult is Result.Failure) validationResult.error else null
                    }
                )
            )
        }
    }

    private fun onDescriptionChanged(description: String) {
        _state.update {
            it.copy(
                description = it.description.copy(
                    value = description
                )
            )
        }
    }

    private fun onDescriptionFocused() {
        _state.update {
            it.copy(
                description = it.description.copy(
                    error = null
                )
            )
        }
    }
    private fun onDescriptionBlurred() {
        _state.update {
            it.copy(
                description = it.description.copy(
                    error = it.description.value.takeIf { it.isNotEmpty() }?.let {
                        val validationResult = validateDescription(it)
                        if (validationResult is Result.Failure) validationResult.error else null
                    }
                )
            )
        }
    }

    private suspend fun onSubmit() {
        _state.update {
            it.copy(
                title = it.title.copy(
                    error = it.title.value.let {
                        val validationResult = validateChannelTitle(title = it)
                        if (validationResult is Result.Failure) validationResult.error else null
                    }
                ),
                alias = it.alias.copy(
                    error = it.alias.value.takeIf { it.isNotEmpty() }?.let {
                        val validationResult = validateChannelAlias(alias = it)
                        if (validationResult is Result.Failure) validationResult.error else null
                    }
                ),
                logo = it.logo.copy(
                    error = it.logo.value?.let {
                        val validationResult = validateImage(it)
                        if (validationResult is Result.Failure) validationResult.error else null
                    }
                ),
                header = it.header.copy(
                    error = it.header.value?.let {
                        val validationResult = validateImage(it)
                        if (validationResult is Result.Failure) validationResult.error else null
                    }
                ),
                description = it.description.copy(
                    error = it.description.value.takeIf { it.isNotEmpty() }?.let {
                        val validationResult = validateDescription(it)
                        if (validationResult is Result.Failure) validationResult.error else null
                    }
                )
            )
        }
        if (_state.value.title.error != null
            && state.value.title.error !is NetworkError
            || _state.value.alias.error != null
            && state.value.alias.error !is NetworkError
            || _state.value.logo.error != null
            || _state.value.header.error != null
            || _state.value.description.error != null
        ) {
            return
        }
        _state.update {
            it.copy(isLoading = true)
        }
        createChannel(
            channel = Channel(
                title = _state.value.title.value,
                alias = _state.value.alias.value,
                ownerId = _state.value.owner,
            ),
            logo = _state.value.logo.value,
            header = _state.value.header.value
        ).onSuccess { channelId ->
            _state.update {
                it.copy(
                    channelId = channelId,
                    isLoading = false,
                    error = null
                )
            }
        }.onFailure { error ->
            _state.update {
                if (error is ChannelCreationError) {
                    it.copy(
                        title = it.title.copy(
                            error = error.titleError
                        ),
                        alias = it.alias.copy(
                            error = error.aliasError
                        )
                    )
                } else {
                    it.copy(
                        error = error,
                        isLoading = false,
                    )
                }
            }
        }
    }
}