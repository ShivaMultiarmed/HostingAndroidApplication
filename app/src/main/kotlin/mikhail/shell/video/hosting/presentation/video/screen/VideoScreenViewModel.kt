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
import mikhail.shell.video.hosting.domain.ImageSize
import mikhail.shell.video.hosting.domain.models.CommentCreationModel
import mikhail.shell.video.hosting.domain.models.CommentEditingModel
import mikhail.shell.video.hosting.domain.models.Liking
import mikhail.shell.video.hosting.domain.models.Subscription
import mikhail.shell.video.hosting.domain.usecases.channels.Subscribe
import mikhail.shell.video.hosting.domain.usecases.comments.EditComment
import mikhail.shell.video.hosting.domain.usecases.comments.GetComments
import mikhail.shell.video.hosting.domain.usecases.comments.PostComment
import mikhail.shell.video.hosting.domain.usecases.comments.RemoveComment
import mikhail.shell.video.hosting.domain.usecases.user.ConstructAvatarUrl
import mikhail.shell.video.hosting.domain.usecases.videos.DeleteVideo
import mikhail.shell.video.hosting.domain.usecases.videos.GetVideoDetails
import mikhail.shell.video.hosting.domain.usecases.videos.IncrementViews
import mikhail.shell.video.hosting.domain.usecases.videos.RateVideo
import mikhail.shell.video.hosting.domain.utils.GetChannelLogoUrl
import mikhail.shell.video.hosting.presentation.models.toUi
import mikhail.shell.video.hosting.presentation.video.models.toUi
import kotlin.time.Clock

@HiltViewModel(assistedFactory = VideoScreenViewModel.Factory::class)
class VideoScreenViewModel @AssistedInject constructor(
    @Assisted("videoId") private val videoId: Long,
    @Assisted("userId") private val userId: Long,
    @Assisted("player") val player: Player,
    private val getVideoDetails: GetVideoDetails,
    private val constructAvatarUrl: ConstructAvatarUrl,
    private val rateVideo: RateVideo,
    private val subscribe: Subscribe,
    private val incrementViews: IncrementViews,
    private val deleteVideo: DeleteVideo,
    private val postComment: PostComment,
    private val editComment: EditComment,
    private val removeComment: RemoveComment,
    private val getComments: GetComments,
    private val getChannelLogoUrl: GetChannelLogoUrl
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
            VideoScreenUiEvent.OpenComments, VideoScreenUiEvent.RestartComments -> {
                if (_state.value.commentsState.comments == null) {
                    val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
                    getComments(now)
                }
            }
            VideoScreenUiEvent.ReachedCommentsEnd -> {
                val before = _state.value.commentsState.comments?.lastOrNull()?.dateTime
                    ?: Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
                getComments(before)
            }
            VideoScreenUiEvent.Restart -> load()
            VideoScreenUiEvent.Remove -> remove()
            is VideoScreenUiEvent.SubmitComment -> submitComment()
            is VideoScreenUiEvent.CommentTextChanged -> onCommentTextChanged(event.text)
            is VideoScreenUiEvent.EditComment -> onEditComment(event.commentId)
            is VideoScreenUiEvent.CancelEditingComment -> onCancelEditingComment()
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
                        video = videoDetails.toUi(
                            channelLogo = getChannelLogoUrl(
                                channelId = videoDetails.channel.channelId!!,
                                size = ImageSize.MEDIUM
                            )
                        ),
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

    private fun submitComment() {
        val currentCommentState = _state.value.commentsState
        if (currentCommentState.initialComment == null) {
            postComment(_state.value.commentsState.currentText)
        } else {
            editComment(
                commentId = currentCommentState.initialComment.commentId,
                text = currentCommentState.currentText
            )
        }
    }

    private fun onCommentTextChanged(text: String) {
        _state.update {
            it.copy(
                commentsState = it.commentsState.copy(
                    currentText = text
                )
            )
        }
    }

    private fun postComment(text: String) {
        viewModelScope.launch {
            postComment(
                CommentCreationModel(
                    videoId = videoId,
                    text = text
                )
            ).onSuccess { comment ->
                _state.update {
                    it.copy(
                        commentsState = it.commentsState.copy(
                            comments = listOf(
                                comment.toUi(avatar = constructAvatarUrl(comment.user.userId!!, ImageSize.SMALL))
                            ) + (it.commentsState.comments ?: emptyList()),
                            actionError = null,
                            currentText = ""
                        )
                    )
                }
            }.onFailure { error ->
                _state.update {
                    it.copy(
                        commentsState = it.commentsState.copy(
                            actionError = error
                        )
                    )
                }
            }
        }
    }

    private fun onEditComment(commentId: Long) {
        _state.update {
            val initialComment = it.commentsState.comments!!.first { it.commentId == commentId }
            it.copy(
                commentsState = it.commentsState.copy(
                    initialComment = initialComment,
                    currentText = initialComment.text
                )
            )
        }
    }

    private fun onCancelEditingComment() {
        _state.update {
            it.copy(
                commentsState = it.commentsState.copy(
                    initialComment = null,
                    currentText = ""
                )
            )
        }
    }

    private fun editComment(
        commentId: Long,
        text: String
    ) {
        viewModelScope.launch {
            editComment(
                CommentEditingModel(
                    commentId = commentId,
                    text = text
                )
            ).onSuccess { comment ->
                _state.update {
                    val editedCommentPosition = it.commentsState.comments!!.indexOfFirst { it.commentId == commentId }
                    val editedComments = it.commentsState.comments.toMutableList().apply {
                        this[editedCommentPosition] = comment.toUi(avatar = constructAvatarUrl(comment.user.userId!!, ImageSize.SMALL))
                    }
                    it.copy(
                        commentsState = it.commentsState.copy(
                            comments = editedComments,
                            actionError = null,
                            initialComment = null,
                            currentText = ""
                        )
                    )
                }
            }.onFailure { error ->
                _state.update {
                    it.copy(
                        commentsState = it.commentsState.copy(
                            actionError = error
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
                                actionError = null
                            )
                        )
                    }
                }.onFailure { error ->
                    _state.update {
                        it.copy(
                            commentsState = it.commentsState.copy(
                                actionError = error
                            )
                        )
                    }
                }
        }
    }

    private fun getComments(before: LocalDateTime) {
        _state.update {
            it.copy(
                commentsState = it.commentsState.copy(
                    isStarting = it.commentsState.comments == null,
                    isLoading = it.commentsState.comments != null
                ),
            )
        }
        viewModelScope.launch {
            getComments(
                before = before.toInstant(TimeZone.currentSystemDefault()),
                videoId = videoId,
                partSize = PART_SIZE
            ).onSuccess { comments ->
                _state.update {
                    it.copy(
                        commentsState = it.commentsState.copy(
                            loadingError = null,
                            comments = (
                                    (it.commentsState.comments ?: listOf()) + comments.map { it.toUi(avatar = constructAvatarUrl(it.user.userId!!, ImageSize.SMALL)) }
                                    ).distinct(),
                            hasMore = comments.size == PART_SIZE,
                            isLoading = false,
                            isStarting = false
                        )
                    )
                }
            }.onFailure { error ->
                _state.update {
                    it.copy(
                        commentsState = it.commentsState.copy(
                            loadingError = error,
                            isLoading = false,
                            isStarting = false
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
            @Assisted("userId") userId: Long,
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
    data object SubmitComment : VideoScreenUiEvent()
    data class CommentTextChanged(val text: String): VideoScreenUiEvent()
    data object CancelEditingComment: VideoScreenUiEvent()
    data class EditComment(val commentId: Long) : VideoScreenUiEvent()
    data class RemoveComment(val commentId: Long) : VideoScreenUiEvent()
    data object ReachedCommentsEnd : VideoScreenUiEvent()
    data object RestartComments : VideoScreenUiEvent()
}