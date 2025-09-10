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
import mikhail.shell.video.hosting.domain.usecases.comments.EditComment
import mikhail.shell.video.hosting.domain.usecases.comments.GetComments
import mikhail.shell.video.hosting.domain.usecases.comments.PostComment
import mikhail.shell.video.hosting.domain.usecases.comments.RemoveComment
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
    private val postComment: PostComment,
    private val editComment: EditComment,
    private val removeComment: RemoveComment,
    private val getComments: GetComments
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
                    val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
                    getComments(now)
                }
            }
            VideoScreenUiEvent.ReachedCommentsEnd, VideoScreenUiEvent.RestartComments -> {
                val before = _state.value.commentsState.comments?.lastOrNull()?.dateTime
                    ?: Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
                getComments(before)
            }
            VideoScreenUiEvent.Restart -> load()
            VideoScreenUiEvent.Remove -> remove()
            is VideoScreenUiEvent.PostComment -> postComment(event.text)
            is VideoScreenUiEvent.EditComment -> editComment(
                commentId = event.commentId,
                text = event.text
            )
            is VideoScreenUiEvent.RemoveComment -> removeComment(event.commentId)
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

    private fun postComment(
        text: String
    ) {
        viewModelScope.launch {
            postComment(
                Comment(
                    videoId = videoId,
                    userId = 0,
                    text = text
                )
            ).onSuccess { comment ->
                _state.update {
                    it.copy(
                        commentsState = it.commentsState.copy(
                            comments = listOf(comment.toUi()) + (it.commentsState.comments
                                ?: emptyList()),
                            error = null
                        )
                    )
                }
            }.onFailure { error ->
                _state.update {
                    it.copy(
                        commentsState = it.commentsState.copy(
                            error = error
                        )
                    )
                }
            }
        }
    }

    private fun editComment(
        commentId: Long,
        text: String
    ) {
        viewModelScope.launch {
            editComment(
                Comment(
                    videoId = videoId,
                    userId = 0,
                    text = text
                )
            ).onSuccess { comment ->
                _state.update {
                    val editedCommentPosition =
                        it.commentsState.comments!!.indexOfFirst { it.commentId == commentId }
                    val editedComments = it.commentsState.comments.toMutableList().apply {
                        set(editedCommentPosition, comment.toUi())
                    }
                    it.copy(
                        commentsState = it.commentsState.copy(
                            comments = editedComments,
                            error = null
                        )
                    )
                }
            }.onFailure { error ->
                _state.update {
                    it.copy(
                        commentsState = it.commentsState.copy(
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
                            commentsState = it.commentsState.copy(
                                comments = it.commentsState.comments!!.filter { it.commentId != commentId },
                                error = null
                            )
                        )
                    }
                }.onFailure { error ->
                    _state.update {
                        it.copy(
                            commentsState = it.commentsState.copy(
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
                videoId = videoId,
                partSize = PART_SIZE
            ).onSuccess { comments ->
                _state.update {
                    it.copy(
                        commentsState = it.commentsState.copy(
                            error = null,
                            comments = (
                                    (it.commentsState.comments ?: listOf()) + comments.map { it.toUi() }
                                    ).distinct(),
                            hasMore = comments.size < PART_SIZE
                        ),
                    )
                }
            }.onFailure { error ->
                _state.update {
                    it.copy(
                        commentsState = it.commentsState.copy(
                            error = error
                        )
                    )
                }
            }
        }
    }

    private companion object {
        const val PART_SIZE = 10
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
    data class PostComment(val text: String) : VideoScreenUiEvent()
    data class EditComment(val commentId: Long, val text: String) : VideoScreenUiEvent()
    data class RemoveComment(val commentId: Long) : VideoScreenUiEvent()
    data object ReachedCommentsEnd : VideoScreenUiEvent()
    data object RestartComments : VideoScreenUiEvent()
}