package mikhail.shell.video.hosting.presentation.video.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import mikhail.shell.video.hosting.domain.models.Result
import mikhail.shell.video.hosting.domain.usecases.videos.SearchForVideos
import mikhail.shell.video.hosting.domain.usecases.videos.validation.ValidateSearchQuery
import mikhail.shell.video.hosting.presentation.video.models.toUi
import javax.inject.Inject

@HiltViewModel
class SearchVideosViewModel @Inject constructor(
    private val searchForVideos: SearchForVideos,
    private val validateSearchQuery: ValidateSearchQuery
): ViewModel() {

    private val _state = MutableStateFlow(SearchVideosScreenState())
    val state = _state.asStateFlow()

    fun onEvent(event: SearchScreenUiEvent) {
        when(event) {
            is SearchScreenUiEvent.QueryChanged -> onQueryChanged(event.query)
            SearchScreenUiEvent.BottomReached -> load(restart = false)
            SearchScreenUiEvent.Submit -> load(restart = true)
            SearchScreenUiEvent.Reload -> load(restart = _state.value.videos == null)
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

    private fun load(restart: Boolean = true) {
        _state.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            searchForVideos(
                query = _state.value.query,
                partNumber = if (!restart) (_state.value.videos?.size?: 0).toLong() / PART_SIZE else 0,
                partSize = PART_SIZE
            ).onSuccess { list ->
                _state.update {
                    it.copy(
                        videos = ((if (!restart) it.videos else null) ?: emptyList()) + list.map { it.toUi() },
                        error = null,
                        isLoading = false,
                        hasMore = list.size < PART_SIZE
                    )
                }
            }.onFailure { error ->
                _state.update {
                    it.copy(
                        error = error,
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
    data class QueryChanged(val query: String): SearchScreenUiEvent()
    data object Submit: SearchScreenUiEvent()
    data object BottomReached: SearchScreenUiEvent()
    data class ClickedVideo(val videoId: Long): SearchScreenUiEvent()
    data object Reload: SearchScreenUiEvent()
}