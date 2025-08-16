package mikhail.shell.video.hosting.presentation.video.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import mikhail.shell.video.hosting.domain.usecases.videos.SearchForVideos
import mikhail.shell.video.hosting.presentation.video.models.toUi
import javax.inject.Inject

@HiltViewModel
class SearchVideosViewModel @Inject constructor(
    private val _searchForVideos: SearchForVideos
): ViewModel() {
    private val _state = MutableStateFlow(SearchVideosScreenState())
    val state = _state.asStateFlow()
    fun search(query: String) {
        _state.update {
            it.copy(
                query = query,
                nextPartNumber = 0,
                areAllVideosLoaded = false,
                videos = null
            )
        }
        loadVideoPart()
    }
    fun loadVideoPart() {
        _state.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            _searchForVideos(
                query = _state.value.query!!,
                partNumber = _state.value.nextPartNumber,
                partSize = PART_SIZE
            ).onSuccess { list ->
                _state.update {
                    it.copy(
                        videos = (it.videos ?: emptyList()) + list.map { it.toUi() },
                        error = null,
                        isLoading = false,
                        nextPartNumber = it.nextPartNumber + 1,
                        areAllVideosLoaded = list.size < PART_SIZE
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