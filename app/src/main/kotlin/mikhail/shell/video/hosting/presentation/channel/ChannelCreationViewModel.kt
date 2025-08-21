package mikhail.shell.video.hosting.presentation.channel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import mikhail.shell.video.hosting.domain.errors.Error
import mikhail.shell.video.hosting.domain.errors.FileError
import mikhail.shell.video.hosting.domain.errors.OptionError
import mikhail.shell.video.hosting.domain.errors.TextError
import mikhail.shell.video.hosting.domain.models.Channel
import mikhail.shell.video.hosting.domain.models.Result
import mikhail.shell.video.hosting.domain.providers.UserDetailsProvider
import mikhail.shell.video.hosting.domain.usecases.CreateChannelUseCase
import mikhail.shell.video.hosting.domain.usecases.ValidateChannelAlias
import mikhail.shell.video.hosting.domain.usecases.ValidateChannelDescription
import mikhail.shell.video.hosting.domain.usecases.ValidateChannelTitle
import mikhail.shell.video.hosting.domain.usecases.ValidateImage

@HiltViewModel
class ChannelCreationViewModel(
    private val validateChannelTitle: ValidateChannelTitle,
    private val validateChannelAlias: ValidateChannelAlias,
    private val validateImage: ValidateImage,
    private val validateChannelDescription: ValidateChannelDescription,
    private val createChannelUseCase: CreateChannelUseCase,
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
            it.copy(alias = alias)
        }
        val validationResult = validateChannelAlias(alias)
        if (validationResult is Result.Failure) {
            _state.update {
                it.copy(aliasError = validationResult.error)
            }
        }
    }
    private suspend fun onHeaderChanged(header: String?) {
        _state.update {
            it.copy(header = header)
        }
        if (header != null) {
            val validationResult = validateImage(header)
            if (validationResult is Result.Failure) {
                _state.update {
                    it.copy(headerError = validationResult.error)
                }
            }
        }
    }
    private suspend fun onLogoChanged(logo: String?) {
        _state.update {
            it.copy(logo = logo)
        }
        if (logo != null) {
            val validationResult = validateImage(logo)
            if (validationResult is Result.Failure) {
                _state.update {
                    it.copy(logoError = validationResult.error)
                }
            }
        }
    }
    private suspend fun onTitleChanged(title: String) {
        _state.update {
            it.copy(title = title)
        }
        val validationResult = validateChannelTitle(title)
        if (validationResult is Result.Failure) {
            _state.update {
                it.copy(titleError = validationResult.error)
            }
        }
    }
    private suspend fun onDescriptionChanged(description: String) {
        _state.update {
            it.copy(description = description)
        }
        val validationResult = validateChannelDescription(description)
        if (validationResult is Result.Failure) {
            _state.update {
                it.copy(descriptionError = validationResult.error)
            }
        }
    }
    private suspend fun onSubmit() {
        if (_state.value.titleError != null
            || _state.value.aliasError != null
            || _state.value.ownerError != null
            || _state.value.logoError != null
            || _state.value.headerError != null
            ) {
            return
        }
        _state.update {
            it.copy(isCreating = true)
        }
        createChannelUseCase(
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

sealed class ChannelCreationUiEvent {
    data class TitleChanged(val title: String) : ChannelCreationUiEvent()
    data class AliasChanged(val alias: String) : ChannelCreationUiEvent()
    data class HeaderChanged(val header: String?) : ChannelCreationUiEvent()
    data class LogoChanged(val logo: String?) : ChannelCreationUiEvent()
    data class DescriptionChanged(val description: String) : ChannelCreationUiEvent()
    data object Submit : ChannelCreationUiEvent()
    data object Cancel: ChannelCreationUiEvent()
    data class Success(val channelId: Long): ChannelCreationUiEvent()
    data object AuthenticationRequired: ChannelCreationUiEvent()
}

data class ChannelCreationScreenState(
    val title: String = "",
    val titleError: Error? = null,
    val owner: Long,
    val ownerError: OptionError? = null,
    val alias: String = "",
    val aliasError: Error? = null,
    val logo: String? = null,
    val logoError: FileError? = null,
    val header: String? = null,
    val headerError: FileError? = null,
    val description: String = "",
    val descriptionError: TextError? = null,
    val channelId: Long? = null,
    val isCreating: Boolean = false,
    val creationError: Error? = null,
)