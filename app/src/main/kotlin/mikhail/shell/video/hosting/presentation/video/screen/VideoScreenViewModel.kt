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
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import mikhail.shell.video.hosting.domain.models.CommentCreationModel
import mikhail.shell.video.hosting.domain.models.CommentEditingModel
import mikhail.shell.video.hosting.domain.models.ImageSize
import mikhail.shell.video.hosting.domain.models.Liking
import mikhail.shell.video.hosting.domain.models.Subscription
import mikhail.shell.video.hosting.domain.models.errorOrNull
import mikhail.shell.video.hosting.domain.usecases.channels.Subscribe
import mikhail.shell.video.hosting.domain.usecases.comments.EditComment
import mikhail.shell.video.hosting.domain.usecases.comments.GetComments
import mikhail.shell.video.hosting.domain.usecases.comments.PostComment
import mikhail.shell.video.hosting.domain.usecases.comments.RemoveComment
import mikhail.shell.video.hosting.domain.usecases.comments.ValidateComment
import mikhail.shell.video.hosting.domain.usecases.user.ConstructAvatarUrl
import mikhail.shell.video.hosting.domain.usecases.user.GetUserDetails
import mikhail.shell.video.hosting.domain.usecases.videos.DeleteVideo
import mikhail.shell.video.hosting.domain.usecases.videos.GetVideoDetails
import mikhail.shell.video.hosting.domain.usecases.videos.GetVideoSourceUrl
import mikhail.shell.video.hosting.domain.usecases.videos.IncrementViews
import mikhail.shell.video.hosting.domain.usecases.videos.RateVideo
import mikhail.shell.video.hosting.domain.utils.GetChannelLogoUrl
import mikhail.shell.video.hosting.presentation.comments.models.toUi
import mikhail.shell.video.hosting.presentation.utils.FieldState
import mikhail.shell.video.hosting.presentation.utils.stateIn
import mikhail.shell.video.hosting.presentation.video.models.toUi

@HiltViewModel(assistedFactory = VideoScreenViewModel.Factory::class)
class VideoScreenViewModel @AssistedInject constructor(
    @Assisted("videoId") private val videoId: Long,
    val player: Player,
    private val getUserDetails: GetUserDetails,
    private val getVideoDetails: GetVideoDetails,
    private val constructAvatarUrl: ConstructAvatarUrl,
    private val getVideoSourceUrl: GetVideoSourceUrl,
    private val rateVideo: RateVideo,
    private val subscribe: Subscribe,
    private val incrementViews: IncrementViews,
    private val deleteVideo: DeleteVideo,
    private val validateComment: ValidateComment,
    private val postComment: PostComment,
    private val editComment: EditComment,
    private val removeComment: RemoveComment,
    private val getComments: GetComments,
    private val getChannelLogoUrl: GetChannelLogoUrl
) : ViewModel() {
    private val _state = MutableStateFlow(VideoScreenState(userId = getUserDetails().userId))
    val state = _state.onStart { start() }.stateIn(_state.value)

    private val _events = MutableSharedFlow<VideoScreenEvent>()
    val events = _events.asSharedFlow()

    fun onAction(action: VideoScreenAction) {
        when (action) {
            is VideoScreenAction.Like -> rate(action.liking)
            VideoScreenAction.RestartComments -> {
                if (_state.value.commentsState.comments == null) {
                    loadComments(null)
                }
            }
            VideoScreenAction.LoadNextCommentsPart -> {
                val before = _state.value.commentsState.comments?.lastOrNull()?.dateTime
                loadComments(before)
            }
            VideoScreenAction.Restart -> start()
            VideoScreenAction.Remove -> remove()
            is VideoScreenAction.SubmitComment -> submitComment()
            is VideoScreenAction.ChangeCommentText -> onCommentTextChanged(action.text)
            is VideoScreenAction.EditComment -> onEditComment(action.commentId)
            is VideoScreenAction.CancelEditingComment -> onEditingCommentCancellation()
            is VideoScreenAction.RemoveComment -> removeComment(action.commentId)
            is VideoScreenAction.Subscribe -> subscribe(action.subscription)
            VideoScreenAction.DownLoad -> viewModelScope.launch {
                _events.emit(VideoScreenEvent.DownloadRequested)
            }
            VideoScreenAction.Edit -> viewModelScope.launch {
                _events.emit(VideoScreenEvent.EditRequested)
            }
            VideoScreenAction.OpenChannel -> viewModelScope.launch {
                _events.emit(VideoScreenEvent.ChannelRequested)
            }
            is VideoScreenAction.OpenProfile -> viewModelScope.launch {
                _events.emit(VideoScreenEvent.ProfileRequested(action.userId))
            }
            VideoScreenAction.Share -> viewModelScope.launch {
                _events.emit(VideoScreenEvent.SharingRequested)
            }
            else -> viewModelScope.launch {
                _events.emit(VideoScreenEvent.ExitRequested)
            }
        }
    }

    @OptIn(UnstableApi::class)
    private fun start() {
        if (_state.value.isStarting) {
            return
        }
        _state.update {
            it.copy(isStarting = true)
        }
        viewModelScope.launch {
            getVideoDetails(videoId).onSuccess { videoDetails ->
                _state.update {
                    it.copy(
                        isStarting = false,
                        video = videoDetails.toUi(
                            channelLogo = getChannelLogoUrl(
                                channelId = videoDetails.channel.channelId,
                                size = ImageSize.MEDIUM
                            )
                        ),
                        error = null
                    )
                }
                val url = getVideoSourceUrl(videoId)
                val previousUri = player.currentMediaItem?.localConfiguration?.uri?.toString()
                if (url != previousUri) {
                    val uri = url.toUri()
                    val mediaItem = MediaItem.fromUri(uri)
                    player.setMediaItem(mediaItem)
                    player.prepare()
                    player.play()
                }
                incrementViews()
            }.onFailure { error ->
                _state.update {
                    it.copy(
                        isStarting = false,
                        error = error
                    )
                }
                viewModelScope.launch {
                    _events.emit(VideoScreenEvent.Failure(error))
                }
                val url = getVideoSourceUrl(videoId)
                val previousUri = player.currentMediaItem?.localConfiguration?.uri?.toString()
                if (url != previousUri) {
                    player.clearMediaItems()
                }
            }
        }
    }

    private fun incrementViews() {
        viewModelScope.launch {
            incrementViews(videoId).onSuccess { video ->
                _state.update {
                    it.copy(
                        video = it.video!!.copy(views = video.views)
                    )
                }
            }.onFailure { error ->
                viewModelScope.launch {
                    _events.emit(VideoScreenEvent.Failure(error))
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
                        )
                    )
                }
            }.onFailure { error ->
                viewModelScope.launch {
                    _events.emit(VideoScreenEvent.Failure(error))
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
                            liking = video.liking
                        )
                    )
                }
            }.onFailure { error ->
                viewModelScope.launch {
                    _events.emit(VideoScreenEvent.Failure(error))
                }
            }
        }
    }

    private fun remove() {
        if (_state.value.isRemoving) {
            return
        }
        _state.update {
            it.copy(isRemoving = true)
        }
        viewModelScope.launch {
            deleteVideo(videoId).onSuccess {
                _state.update {
                    it.copy(isRemoving = false)
                }
                viewModelScope.launch {
                    _events.emit(VideoScreenEvent.Removed)
                }
            }.onFailure { error ->
                _state.update {
                    it.copy(isRemoving = false)
                }
                viewModelScope.launch {
                    _events.emit(VideoScreenEvent.Failure(error))
                }
            }
        }
    }

    private fun submitComment() {
        if (_state.value.commentsState.comment.commentId == null) {
            postComment(_state.value.commentsState.comment.text.value)
        } else {
            editComment(
                commentId = _state.value.commentsState.comment.commentId!!,
                text = _state.value.commentsState.comment.text.value
            )
        }
    }

    private fun onCommentTextChanged(text: String) {
        _state.update {
            it.copy(
                commentsState = it.commentsState.copy(
                    comment = it.commentsState.comment.copy(
                        text = it.commentsState.comment.text.copy(value = text)
                    )
                )
            )
        }
    }

    private fun postComment(text: String) {
        _state.update {
            it.copy(
                commentsState = it.commentsState.copy(
                    comment = it.commentsState.comment.copy(
                        text = it.commentsState.comment.text.copy(
                            error = validateComment(text).errorOrNull()
                        )
                    )
                )
            )
        }
        if (_state.value.commentsState.comment.text.error != null) {
            return
        }
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
                                comment.toUi(
                                    avatar = constructAvatarUrl(
                                        comment.user.userId,
                                        ImageSize.SMALL
                                    )
                                )
                            ) + (it.commentsState.comments ?: listOf()),
                            comment = it.commentsState.comment.copy(
                                text = it.commentsState.comment.text.copy(
                                    value = "",
                                    error = null
                                )
                            )
                        )
                    )
                }
            }.onFailure { error ->
                viewModelScope.launch {
                    _events.emit(VideoScreenEvent.Failure(error))
                }
            }
        }
    }

    private fun onEditComment(commentId: Long) {
        _state.update {
            val text = it.commentsState.comments?.firstOrNull { it.commentId == commentId }?.text
                ?: return@update it
            it.copy(
                commentsState = it.commentsState.copy(
                    comment = it.commentsState.comment.copy(
                        commentId = commentId,
                        text = FieldState(text)
                    )
                )
            )
        }
    }

    private fun onEditingCommentCancellation() {
        _state.update {
            it.copy(
                commentsState = it.commentsState.copy(
                    comment = it.commentsState.comment.copy(
                        commentId = null,
                        text = FieldState("")
                    )
                )
            )
        }
    }

    private fun editComment(
        commentId: Long,
        text: String
    ) {
        _state.update {
            it.copy(
                commentsState = it.commentsState.copy(
                    comment = it.commentsState.comment.copy(
                        text = it.commentsState.comment.text.copy(
                            error = validateComment(text).errorOrNull()
                        )
                    )
                )
            )
        }
        if (_state.value.commentsState.comment.text.error != null) {
            return
        }
        viewModelScope.launch {
            editComment(
                CommentEditingModel(
                    commentId = commentId,
                    text = text
                )
            ).onSuccess { comment ->
                _state.update {
                    val editedCommentPosition =
                        it.commentsState.comments!!.indexOfFirst { it.commentId == commentId }
                    val editedComments = it.commentsState.comments.toMutableList().apply {
                        this[editedCommentPosition] = comment.toUi(
                            avatar = constructAvatarUrl(
                                comment.user.userId,
                                ImageSize.SMALL
                            )
                        )
                    }
                    it.copy(
                        commentsState = it.commentsState.copy(
                            comments = editedComments,
                            comment = it.commentsState.comment.copy(
                                commentId = null,
                                text = FieldState("")
                            )
                        )
                    )
                }
            }.onFailure { error ->
                viewModelScope.launch {
                    _events.emit(VideoScreenEvent.Failure(error))
                }
            }
        }
    }

    private fun removeComment(commentId: Long) {
        viewModelScope.launch {
            removeComment.invoke(commentId).onSuccess {
                _state.update {
                    it.copy(
                        commentsState = it.commentsState.copy(
                            comments = it.commentsState.comments!!.filter { it.commentId != commentId }
                        )
                    )
                }
            }.onFailure { error ->
                viewModelScope.launch {
                    _events.emit(VideoScreenEvent.Failure(error))
                }
            }
        }
    }

    private fun loadComments(before: LocalDateTime?) {
        if (_state.value.commentsState.isStarting || _state.value.commentsState.isLoading) {
            return
        }
        _state.update {
            it.copy(
                commentsState = it.commentsState.copy(
                    isStarting = it.commentsState.comments == null,
                    isLoading = it.commentsState.comments != null
                )
            )
        }
        viewModelScope.launch {
            getComments(
                before = before?.toInstant(TimeZone.currentSystemDefault()),
                videoId = videoId,
                partSize = PART_SIZE
            ).onSuccess { comments ->
                _state.update {
                    it.copy(
                        commentsState = it.commentsState.copy(
                            comments = (
                                    (it.commentsState.comments ?: listOf()) + comments.map {
                                        it.toUi(
                                            avatar = constructAvatarUrl(
                                                it.user.userId,
                                                ImageSize.SMALL
                                            )
                                        )
                                    }
                                    ).distinct(),
                            hasMore = comments.size == PART_SIZE,
                            isLoading = false,
                            isStarting = false,
                            error = null
                        )
                    )
                }
            }.onFailure { error ->
                _state.update {
                    it.copy(
                        commentsState = it.commentsState.copy(
                            isLoading = false,
                            isStarting = false,
                            error = error
                        )
                    )
                }
                viewModelScope.launch {
                    _events.emit(VideoScreenEvent.Failure(error))
                }
            }
        }
    }

    private companion object {
        const val PART_SIZE = 10
    }

    @AssistedFactory
    interface Factory {
        fun create(@Assisted("videoId") videoId: Long): VideoScreenViewModel
    }
}