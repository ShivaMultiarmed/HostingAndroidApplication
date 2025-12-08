package mikhail.shell.video.hosting.presentation.video.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import mikhail.shell.video.hosting.domain.errors.Error
import mikhail.shell.video.hosting.domain.models.ImageSize
import mikhail.shell.video.hosting.domain.models.errorOrNull
import mikhail.shell.video.hosting.domain.usecases.videos.GetVideoCoverUrl
import mikhail.shell.video.hosting.domain.usecases.videos.SearchForVideos
import mikhail.shell.video.hosting.domain.usecases.videos.validation.ValidateSearchQuery
import mikhail.shell.video.hosting.domain.utils.GetChannelLogoUrl
import mikhail.shell.video.hosting.presentation.video.models.toUi
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val searchForVideos: SearchForVideos,
    private val getChannelLogoUrl: GetChannelLogoUrl,
    private val getVideoCoverUrl: GetVideoCoverUrl,
    private val validateSearchQuery: ValidateSearchQuery
) : ViewModel() {

    private val _state = MutableStateFlow(SearchScreenState())
    val state = _state.asStateFlow()

    private val _events = MutableSharedFlow<SearchScreenEvent>()
    val events = _events.asSharedFlow()

    fun onAction(action: SearchScreenAction) {
        when (action) {
            is SearchScreenAction.ChangeQuery -> onQueryChanged(action.query)
            SearchScreenAction.LoadNextPart -> load(start = false)
            SearchScreenAction.Restart, SearchScreenAction.Submit -> load()
            is SearchScreenAction.ChooseVideo -> viewModelScope.launch {
                _events.emit(SearchScreenEvent.VideoChosen(action.videoId))
            }
        }
    }

    private fun onQueryChanged(query: String) {
        _state.update {
            it.copy(
                query = it.query.copy(
                    value = query,
                    error = if (query.isEmpty()) null else validateSearchQuery(query).errorOrNull()
                )
            )
        }
    }

    private fun load(start: Boolean = true) {
        if (
            _state.value.isStarting
            || _state.value.isLoading
            || _state.value.query.error != null
        ) {
            return
        }
        _state.update {
            it.copy(
                isStarting = start,
                isLoading = !start
            )
        }
        viewModelScope.launch {
            searchForVideos(
                query = _state.value.query.value,
                cursor = if (start) null else _state.value.videos!!.last().videoId,
                partSize = PART_SIZE
            ).onSuccess { videos ->
                _state.update {
                    it.copy(
                        videos = ((if (!start) it.videos else null) ?: emptyList()) + videos.map {
                            it.toUi(
                                channelLogo = getChannelLogoUrl(
                                    channelId = it.channel.channelId,
                                    size = ImageSize.MEDIUM
                                ),
                                videoCover = getVideoCoverUrl(
                                    videoId = it.video.videoId,
                                    size = ImageSize.MEDIUM
                                )
                            )
                        },
                        error = null,
                        isStarting = false,
                        isLoading = false,
                        hasMore = videos.size == PART_SIZE
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
                viewModelScope.launch {
                    _events.emit(SearchScreenEvent.Failure(error))
                }
            }
        }
    }

    private companion object {
        const val PART_SIZE = 10
    }
}

sealed class SearchScreenAction {
    data class ChangeQuery(val query: String) : SearchScreenAction()
    data object Submit : SearchScreenAction()
    data object Restart : SearchScreenAction()
    data object LoadNextPart : SearchScreenAction()
    data class ChooseVideo(val videoId: Long) : SearchScreenAction()
}

sealed class SearchScreenEvent {
    data class Failure(val error: Error) : SearchScreenEvent()
    data class VideoChosen(val videoId: Long) : SearchScreenEvent()
}