package mikhail.shell.video.hosting.presentation.video.screen

import androidx.annotation.OptIn
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
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
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import mikhail.shell.video.hosting.domain.models.Comment
import mikhail.shell.video.hosting.domain.models.Liking
import mikhail.shell.video.hosting.domain.models.Subscription
import mikhail.shell.video.hosting.domain.usecases.channels.Subscribe
import mikhail.shell.video.hosting.domain.usecases.comments.GetComments
import mikhail.shell.video.hosting.domain.usecases.comments.ObserveComments
import mikhail.shell.video.hosting.domain.usecases.comments.RemoveComment
import mikhail.shell.video.hosting.domain.usecases.comments.SaveComment
import mikhail.shell.video.hosting.domain.usecases.comments.UnobserveComments
import mikhail.shell.video.hosting.domain.usecases.videos.DeleteVideo
import mikhail.shell.video.hosting.domain.usecases.videos.GetVideoDetails
import mikhail.shell.video.hosting.domain.usecases.videos.IncrementViews
import mikhail.shell.video.hosting.domain.usecases.videos.RateVideo
import mikhail.shell.video.hosting.presentation.models.toUi
import mikhail.shell.video.hosting.presentation.video.models.toUi
import kotlin.time.Clock

@HiltViewModel(assistedFactory = VideoScreenViewModel.Factory::class)
class VideoScreenViewModel @AssistedInject constructor(
    @Assisted("videoId") private val videoId: Long,
    @Assisted("player") val player: Player,
    private val getVideoDetails: GetVideoDetails,
    private val rateVideo: RateVideo,
    private val subscribe: Subscribe,
    private val incrementViews: IncrementViews,
    private val deleteVideo: DeleteVideo,
    private val saveComment: SaveComment,
    private val removeComment: RemoveComment,
    private val getComments: GetComments,
    private val observeComments: ObserveComments,
    private val unobserveComments: UnobserveComments
) : ViewModel() {
    private val _state = MutableStateFlow(VideoScreenState())
    val state = _state.onStart {
        load()
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(3000),
        initialValue = _state.value
    )


    fun onEvent(event: VideoScreenUiEvent) {
        when (event) {
            is VideoScreenUiEvent.Like -> rate(event.liking)
            VideoScreenUiEvent.OpenComments -> {
                if (_state.value.commentsState.comments == null) {
                    getComments(Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()))
                }
            }
            VideoScreenUiEvent.Restart -> load()
            VideoScreenUiEvent.Remove -> remove()
            is VideoScreenUiEvent.SaveComment -> saveComment(
                commentId = event.commentId,
                text = event.text
            )

            is VideoScreenUiEvent.Subscribe -> subscribe(event.subscription)
            else -> Unit
        }
    }

    @OptIn(UnstableApi::class)
    private fun load() {
        _state.update {
            it.copy(
                isStarting = true
            )
        }
        viewModelScope.launch {
            getVideoDetails(videoId).onSuccess { videoDetails ->
                _state.update {
                    it.copy(
                        isStarting = false,
                        video = videoDetails.toUi(),
                        startingError = null
                    )
                }
                val url = videoDetails.video.sourceUrl
                val previousUri = player.currentMediaItem?.localConfiguration?.uri?.toString()
                if (url != previousUri) {
                    val uri = url!!.toUri()
                    val mediaItem = MediaItem.fromUri(uri)
                    player.setMediaItem(mediaItem)
                    player.prepare()
                    player.play()
                    incrementViews()
                }
            }.onFailure { error ->
                _state.update {
                    it.copy(
                        isStarting = false,
                        startingError = error
                    )
                }
            }
        }
    }

    private fun incrementViews() {
        viewModelScope.launch {
            incrementViews(videoId).onSuccess { video ->
                _state.update {
                    it.copy(
                        video = it.video!!.copy(
                            views = video.views,
                        ),
                        viewError = null
                    )
                }
            }.onFailure { error ->
                _state.update {
                    it.copy(
                        viewError = error
                    )
                }
            }
        }
    }

    private fun subscribe(subscription: Subscription) {
        viewModelScope.launch {
            subscribe(
                channelId = _state.value.video!!.channelId,
                subscription = subscription
            ).onSuccess { channel ->
                _state.update {
                    it.copy(
                        video = it.video!!.copy(
                            subscription = channel.subscription,
                            subscribers = channel.subscribers
                        ),
                        subscriptionError = null
                    )
                }
            }.onFailure { error ->
                _state.update {
                    it.copy(subscriptionError = error)
                }
            }
        }
    }

    private fun rate(liking: Liking) {
        viewModelScope.launch {
            rateVideo(
                videoId = _state.value.video!!.videoId,
                liking = liking
            ).onSuccess { video ->
                _state.update {
                    it.copy(
                        video = it.video!!.copy(
                            likes = video.likes,
                            dislikes = video.dislikes,
                            liking = liking
                        ),
                        likingError = null
                    )
                }
            }.onFailure { error ->
                _state.update {
                    it.copy(likingError = error)
                }
            }
        }
    }

    private fun remove() {
        viewModelScope.launch {
            deleteVideo(videoId).onSuccess {
                _state.update {
                    it.copy(
                        isRemoved = true,
                        removingError = null
                    )
                }
            }.onFailure { error ->
                _state.update {
                    it.copy(
                        removingError = error
                    )
                }
            }
        }
    }

    private fun saveComment(
        commentId: Long? = null,
        text: String
    ) {
        viewModelScope.launch {
            saveComment(
                Comment(
                    commentId = commentId,
                    videoId = videoId,
                    userId = 0,
                    text = text
                )
            ).onSuccess {
                _state.update {
                    it.copy(
                        commentsState = it.commentsState!!.copy(
                            comments = it.commentsState.comments, // TODO: Add/edit a comment
                            error = null
                        )
                    )
                }
            }.onFailure { error ->
                _state.update {
                    it.copy(
                        commentsState = it.commentsState!!.copy(
                            error = error
                        )
                    )
                }
            }
        }

    }

    private fun removeComment(commentId: Long) {
        viewModelScope.launch {
            removeComment.invoke(commentId)
                .onSuccess {
                    _state.update {
                        it.copy(
                            commentsState = it.commentsState!!.copy(
                                comments = it.commentsState.comments!!.filter { it.commentId != commentId },
                                error = null
                            )
                        )
                    }
                }.onFailure { error ->
                    _state.update {
                        it.copy(
                            commentsState = it.commentsState!!.copy(
                                error = error
                            )
                        )
                    }
                }
        }
    }

    private fun getComments(before: LocalDateTime) {
        viewModelScope.launch {
            getComments(
                before = before.toInstant(TimeZone.currentSystemDefault()),
                videoId = videoId
            ).onSuccess { comments ->
                _state.update {
                    it.copy(
                        commentsState = it.commentsState!!.copy(
                            error = null,
                            comments = ((it.commentsState.comments
                                ?: listOf()) + comments.map { it.toUi() }).distinct()
                        ),
                    )
                }
            }.onFailure { error ->
                _state.update {
                    it.copy(
                        commentsState = it.commentsState!!.copy(
                            error = error
                        )
                    )
                }
            }
        }
    }

    @AssistedFactory
    interface Factory {
        fun create(
            @Assisted("videoId") videoId: Long,
            @Assisted("player") player: Player
        ): VideoScreenViewModel
    }
}

sealed class VideoScreenUiEvent {
    data object Restart : VideoScreenUiEvent()
    data class Like(val liking: Liking) : VideoScreenUiEvent()
    data class Subscribe(val subscription: Subscription) : VideoScreenUiEvent()
    data object DownLoad : VideoScreenUiEvent()
    data object Share : VideoScreenUiEvent()
    data object Remove : VideoScreenUiEvent()
    data object Edit : VideoScreenUiEvent()
    data object OpenChannel : VideoScreenUiEvent()
    data object OpenComments : VideoScreenUiEvent()
    data object CloseComments : VideoScreenUiEvent()
    data class OpenProfile(val userId: Long) : VideoScreenUiEvent()
    data class SaveComment(val commentId: Long?, val text: String) : VideoScreenUiEvent()
    data class RemoveComment(val commentId: Long) : VideoScreenUiEvent()
    data object ReachedBottom : VideoScreenUiEvent()
}