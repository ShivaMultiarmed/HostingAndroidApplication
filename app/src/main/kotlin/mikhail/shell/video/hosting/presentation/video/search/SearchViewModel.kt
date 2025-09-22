package mikhail.shell.video.hosting.presentation.video.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import mikhail.shell.video.hosting.domain.ImageSize
import mikhail.shell.video.hosting.domain.models.Result
import mikhail.shell.video.hosting.domain.usecases.videos.SearchForVideos
import mikhail.shell.video.hosting.domain.usecases.videos.validation.ValidateSearchQuery
import mikhail.shell.video.hosting.domain.utils.GetChannelLogoUrl
import mikhail.shell.video.hosting.presentation.video.models.toUi
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val searchForVideos: SearchForVideos,
    private val getChannelLogoUrl: GetChannelLogoUrl,
    private val validateSearchQuery: ValidateSearchQuery
) : ViewModel() {

    private val _state = MutableStateFlow(SearchScreenState())
    val state = _state.asStateFlow()

    fun onEvent(event: SearchScreenUiEvent) {
        when (event) {
            is SearchScreenUiEvent.QueryChanged -> onQueryChanged(event.query)
            SearchScreenUiEvent.Reload, SearchScreenUiEvent.BottomReached -> load(start = false)
            SearchScreenUiEvent.Restart, SearchScreenUiEvent.Submit -> load(start = true)
            else -> Unit
        }
    }

    private fun onQueryChanged(query: String) {
        _state.update {
            it.copy(
                query = query,
                queryError = query.let {
                    val validationResult = validateSearchQuery(query)
                    if (validationResult is Result.Failure) validationResult.error else null
                }
            )
        }
    }

    private fun load(start: Boolean = true) {
        _state.update {
            it.copy(
                isStarting = start,
                isLoading = !start
            )
        }
        viewModelScope.launch {
            searchForVideos(
                query = _state.value.query,
                cursor = if (start) null else _state.value.videos!!.last().videoId,
                partSize = PART_SIZE
            ).onSuccess { list ->
                _state.update {
                    it.copy(
                        videos = ((if (!start) it.videos else null) ?: emptyList()) + list.map {
                            it.toUi(
                                channelLogo = getChannelLogoUrl(
                                    channelId = it.channel.channelId!!,
                                    size = ImageSize.MEDIUM
                                )
                            )
                        },
                        error = null,
                        isStarting = false,
                        isLoading = false,
                        hasMore = list.size == PART_SIZE
                    )
                }
            }.onFailure { error ->
                _state.update {
                    it.copy(
                        error = error,
                        isStarting = false,
                        isLoading = false
                    )
                }
            }
        }
    }

    private companion object {
        const val PART_SIZE = 10
    }
}

sealed class SearchScreenUiEvent {
    data class QueryChanged(val query: String) : SearchScreenUiEvent()
    data object Restart : SearchScreenUiEvent()
    data object Submit : SearchScreenUiEvent()
    data object BottomReached : SearchScreenUiEvent()
    data object Reload : SearchScreenUiEvent()
    data class ClickedVideo(val videoId: Long) : SearchScreenUiEvent()
}