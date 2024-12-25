package mikhail.shell.video.hosting.presentation.video.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import mikhail.shell.video.hosting.domain.usecases.videos.SearchForVideos
import javax.inject.Inject

@HiltViewModel
class SearchVideosViewModel @Inject constructor(
    private val _searchForVideos: SearchForVideos
): ViewModel() {
    private val _state = MutableStateFlow(SearchVideosScreenState())
    val state = _state.asStateFlow()

    fun search(
        query: String
    ) {
        _state.update {
            it.copy(
                query = query
            )
        }
        loadVideoPart(10, 1)
    }
    fun loadVideoPart(
        partSize: Int,
        partNumber: Long
    ) {
        _state.update {
            it.copy(
                isLoading = true
            )
        }
        viewModelScope.launch {
            _searchForVideos(
                _state.value.query!!,
                partNumber,
                partSize
            ).onSuccess { list ->
                _state.update {
                    it.copy(
                        videos = list,
                        error = null,
                        isLoading = false
                    )
                }
            }.onFailure { e ->
                _state.update {
                    it.copy(
                        videos = null,
                        error = e,
                        isLoading = false
                    )
                }
            }
        }
    }
}