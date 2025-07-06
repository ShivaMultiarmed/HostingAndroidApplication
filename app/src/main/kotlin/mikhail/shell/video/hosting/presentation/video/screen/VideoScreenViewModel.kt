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
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import mikhail.shell.video.hosting.domain.errors.GetCommentsError
import mikhail.shell.video.hosting.domain.errors.VideoError
import mikhail.shell.video.hosting.domain.models.Action
import mikhail.shell.video.hosting.domain.models.ActionModel
import mikhail.shell.video.hosting.domain.models.Comment
import mikhail.shell.video.hosting.domain.models.CommentWithUser
import mikhail.shell.video.hosting.domain.models.LikingState
import mikhail.shell.video.hosting.domain.models.SubscriptionState
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
import mikhail.shell.video.hosting.presentation.models.toModel

@HiltViewModel(assistedFactory = VideoScreenViewModel.Factory::class)
class VideoScreenViewModel @AssistedInject constructor(
    @Assisted("userId") private val userId: Long,
    @Assisted("videoId") private val videoId: Long,
    @Assisted("player") val player: Player,
    private val _getVideoDetails: GetVideoDetails,
    private val _rateVideo: RateVideo,
    private val _subscribe: Subscribe,
    private val _incrementViews: IncrementViews,
    private val _deleteVideo: DeleteVideo,
    private val _saveComment: SaveComment,
    private val _removeComment: RemoveComment,
    private val _getComments: GetComments,
    private val _observeComments: ObserveComments,
    private val _unobserveComments: UnobserveComments
) : ViewModel() {
    private val _state = MutableStateFlow(VideoScreenState())
    val state = _state.asStateFlow()
    private var _collectCommentsJob: Job? = null
    init {
        _state.update {
            it.copy(
                isLoading = true
            )
        }
        loadVideo()
    }
    @OptIn(UnstableApi::class)
    fun loadVideo() {
        _state.value = VideoScreenState()
        viewModelScope.launch {
            _getVideoDetails(
                videoId,
                userId
            ).onSuccess {
                _state.value = VideoScreenState(
                    videoDetails = it,
                    isLoading = false,
                    error = null,
                    isViewed = true
                )
                val url = _state.value.videoDetails?.video?.sourceUrl
                val previousUri = player.currentMediaItem?.localConfiguration?.uri.toString()
                if (url != previousUri) {
                    val uri = url!!.toUri()
                    val mediaItem = MediaItem.fromUri(uri)
                    player.setMediaItem(mediaItem)
                    player.prepare()
                    player.play()
                    incrementViews()
                }
            }.onFailure {
                _state.value = VideoScreenState(
                    videoDetails = _state.value.videoDetails,
                    isLoading = false,
                    error = it
                )
            }
        }
    }
    fun incrementViews() {
        viewModelScope.launch {
            _incrementViews(videoId).onSuccess { newViews ->
                _state.update {
                    it.copy(
                        videoDetails = it.videoDetails?.copy(
                            video = it.videoDetails.video.copy(
                                views = newViews
                            )
                        )
                    )
                }
            }
        }
    }
    fun subscribe(subscriptionState: SubscriptionState) {
        _state.update {
            it.copy(
                isLoading = true
            )
        }
        viewModelScope.launch {
            val channelId = _state.value.videoDetails?.channel?.channelId!!
            _subscribe(
                channelId = channelId,
                userId = userId,
                subscriptionState = subscriptionState
            ).onSuccess { channelWithUser ->
                _state.update {
                    it.copy(
                        videoDetails = it.videoDetails?.copy(
                            channel = channelWithUser
                        ),
                        isLoading = false,
                        error = null
                    )
                }
            }.onFailure {
                _state.update {
                    it.copy(
                        videoDetails = null,
                        isLoading = false,
                        error = VideoError.FAILED_LOADING
                    )
                }
            }
        }
    }

    fun rate(likingState: LikingState) {
        viewModelScope.launch {
            _rateVideo(
                videoId,
                userId,
                likingState
            ).onSuccess { updVideo ->
                _state.update { screenState ->
                    screenState.copy(
                        videoDetails = screenState.videoDetails?.copy(
                            video = screenState.videoDetails.video.copy(
                                likes = updVideo.likes,
                                dislikes = updVideo.dislikes,
                                liking = likingState
                            )
                        )
                    )
                }
            }
        }
    }

    fun deleteVideo() {
        _state.update {
            it.copy(
                isLoading = true
            )
        }
        viewModelScope.launch {
            _deleteVideo(videoId).onSuccess {
                _state.update {
                    it.copy(
                        isLoading = false,
                        videoDetails = null
                    )
                }
            }.onFailure {
                _state.update {
                    it.copy(
                        isLoading = false
                    )
                }
            }
        }
    }

    fun saveComment(commentId: Long?, text: String) {
        if (text.isNotEmpty()) {
            val comment = Comment(
                commentId = commentId,
                videoId = videoId,
                userId = userId,
                dateTime = null,
                text = text
            )
            _state.update {
                it.copy(
                    isLoading = true
                )
            }
            val now = Clock.System.now()
            viewModelScope.launch {
                _saveComment(
                    comment.copy(dateTime = now)
                ).onSuccess {
                    _state.update {
                        it.copy(
                            commentError = null
                        )
                    }
                }.onFailure { error ->
                    _state.update {
                        it.copy(
                            commentError = error
                        )
                    }
                }
            }
        }
    }

    fun removeComment(commentId: Long) {
        viewModelScope.launch {
            _removeComment(commentId)
        }
    }

    fun getComments(before: Instant) {
        _state.update {
            it.copy(
                isLoading = true
            )
        }
        viewModelScope.launch {
            _getComments(
                before,
                videoId
            ).onSuccess { commentsWithUsers ->
                val commentModels = commentsWithUsers.map { it.toModel() }
                _state.update {
                    it.copy(
                        commentError = if (it.commentError is GetCommentsError) null else it.commentError,
                        comments = ((it.comments?: listOf()) + commentModels).distinct()
                    )
                }
            }.onFailure { err ->
                _state.update {
                    it.copy(
                        commentError = err
                    )
                }
            }
        }
    }
    fun observeComments() {
        _collectCommentsJob = viewModelScope.launch {
            _observeComments(videoId).collect(::handleCommentAction)
        }
    }
    fun unobserveComments() {
        _state.update {
            it.copy(
                comments = null
            )
        }
        _collectCommentsJob?.cancel()
        _unobserveComments(videoId)
    }

    private fun handleCommentAction(actionModel: ActionModel<CommentWithUser>) {
        val action = actionModel.action
        val commentModel = actionModel.model.toModel()
        if (commentModel.userId == userId) {
            _state.update {
                it.copy(
                    actionComment = ActionModel(action, commentModel)
                )
            }
        }
        when(actionModel.action) {
            Action.ADD -> {
                _state.update {
                    it.copy(
                        comments = listOf(commentModel) + (it.comments?: listOf())
                    )
                }
            }
            Action.REMOVE -> {
                _state.update {
                    it.copy(
                        comments = it.comments?.filter { it.commentId != commentModel.commentId }
                    )
                }
            }
            Action.UPDATE -> {
                _state.update {
                    val currentPosition = it.comments?.indexOfFirst { it.commentId == commentModel.commentId }
                    it.copy(
                        comments = it.comments?.toMutableList().also { it?.set(currentPosition!!, commentModel) }?.toList()
                    )
                }
            }
        }
    }

    @AssistedFactory
    interface Factory {
        fun create(
            @Assisted("userId") userId: Long,
            @Assisted("videoId") videoId: Long,
            @Assisted("player") player: Player
        ): VideoScreenViewModel
    }
}