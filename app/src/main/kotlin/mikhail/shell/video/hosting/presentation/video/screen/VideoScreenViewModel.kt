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
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import mikhail.shell.video.hosting.domain.models.Action
import mikhail.shell.video.hosting.domain.models.ActionModel
import mikhail.shell.video.hosting.domain.models.Comment
import mikhail.shell.video.hosting.domain.models.CommentWithUser
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
    private val _state = MutableStateFlow<VideoScreenState>(VideoScreenState.Loading)
    val state = _state
        .onStart {
            load()
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(3000),
            initialValue = _state.value
        )

    private var _collectCommentsJob: Job? = null

    fun onEvent(event: VideoScreenUiEvent) {
        when (event) {
            VideoScreenUiEvent.CloseComments -> unobserve()
            is VideoScreenUiEvent.Like -> rate(event.liking)
            VideoScreenUiEvent.OpenComments -> {
                getComments(Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()))
                observe()
            }
            VideoScreenUiEvent.Reload -> load()
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
        viewModelScope.launch {
            getVideoDetails(videoId)
                .onSuccess { videoDetails ->
                    _state.update {
                        VideoScreenState.Success(
                            video = videoDetails.toUi(),
                            isViewed = false
                        )
                    }
                    val url = videoDetails.video.sourceUrl
                    val previousUri = player.currentMediaItem?.localConfiguration?.uri.toString()
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
                        VideoScreenState.Failure(error)
                    }
                }
        }
    }

    private fun incrementViews() {
        viewModelScope.launch {
            incrementViews(videoId)
                .onSuccess { video ->
                    _state.update {
                        it as VideoScreenState.Success
                        it.copy(
                            video = it.video.copy(
                                views = video.views
                            )
                        )
                    }
                }
        }
    }

    private fun subscribe(subscription: Subscription) {
        viewModelScope.launch {
            subscribe(
                channelId = (_state.value as VideoScreenState.Success).video.videoId,
                subscription = subscription
            ).onSuccess { channel ->
                _state.update {
                    it as VideoScreenState.Success
                    it.copy(
                        video = it.video.copy(
                            subscription = channel.subscription,
                            subscribers = channel.subscribers
                        ),
                        subscriptionError = null
                    )
                }
            }.onFailure { error ->
                _state.update {
                    (it as VideoScreenState.Success).copy(subscriptionError = error)
                }
            }
        }
    }

    private fun rate(liking: Liking) {
        viewModelScope.launch {
            rateVideo(
                videoId = (_state.value as VideoScreenState.Success).video.videoId,
                liking = liking
            ).onSuccess { video ->
                _state.update {
                    it as VideoScreenState.Success
                    it.copy(
                        video = it.video.copy(
                            likes = video.likes,
                            dislikes = video.dislikes,
                            liking = liking
                        ),
                        likingError = null
                    )
                }
            }.onFailure { error ->
                _state.update {
                    it as VideoScreenState.Success
                    it.copy(likingError = error)
                }
            }
        }
    }

    private fun remove() {
        viewModelScope.launch {
            deleteVideo(videoId)
                .onSuccess {
                    _state.update {
                        VideoScreenState.Removed
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
            )
                .onSuccess {
                    _state.update {
                        it as VideoScreenState.Success
                        it.copy(commentsState = it.commentsState!!.copy(error = null))
                    }
                }
                .onFailure { error ->
                    _state.update {
                        it as VideoScreenState.Success
                        it.copy(commentsState = it.commentsState!!.copy(error = error))
                    }
                }
        }

    }

    private fun removeComment(commentId: Long) {
        viewModelScope.launch {
            removeComment.invoke(commentId)
                .onSuccess {
                    _state.update {
                        it as VideoScreenState.Success
                        it.copy(commentsState = it.commentsState!!.copy(error = null))
                    }
                }.onFailure { error ->
                    _state.update {
                        it as VideoScreenState.Success
                        it.copy(commentsState = it.commentsState!!.copy(error = error))
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
                    it as VideoScreenState.Success
                    it.copy(
                        commentsState = it.commentsState!!.copy(
                            error = null,
                            comments = ((it.commentsState.comments ?: listOf()) + comments.map { it.toUi() }).distinct()
                        ),
                    )
                }
            }.onFailure { error ->
                _state.update {
                    it as VideoScreenState.Success
                    it.copy(
                        commentsState = it.commentsState!!.copy(
                            error = error
                        )
                    )
                }
            }
        }
    }

    private fun observe() {
        _collectCommentsJob = viewModelScope.launch {
            observeComments(videoId).collect(::handleComment)
        }
    }

    private fun unobserve() {
        _state.update {
            (it as VideoScreenState.Success).copy(commentsState = null)
        }
        _collectCommentsJob?.cancel()
        unobserveComments(videoId)
    }

    private fun handleComment(actionModel: ActionModel<CommentWithUser>) {
        val commentModel = actionModel.model.toUi()
        _state.update {
            it as VideoScreenState.Success
            it.copy(
                commentsState = it.commentsState!!.copy(
                    comments = when (actionModel.action) {
                        Action.ADD -> listOf(commentModel) + (it.commentsState.comments ?: listOf())
                        Action.REMOVE -> it.commentsState.comments?.filter { it.commentId != commentModel.commentId }
                        Action.UPDATE -> {
                            val currentPosition = it.commentsState.comments?.indexOfFirst { it.commentId == commentModel.commentId }
                            it.commentsState.comments?.toMutableList().also { it?.set(currentPosition!!, commentModel) }?.toList()
                        }
                    }
                )
            )
        }
    }

    override fun onCleared() {
        unobserveComments(videoId)
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
    data object Reload: VideoScreenUiEvent()
    data class Like(val liking: Liking): VideoScreenUiEvent()
    data class Subscribe(val subscription: Subscription): VideoScreenUiEvent()
    data object DownLoad: VideoScreenUiEvent()
    data object Share: VideoScreenUiEvent()
    data object Remove: VideoScreenUiEvent()
    data object Edit: VideoScreenUiEvent()
    data object OpenChannel: VideoScreenUiEvent()
    data object OpenComments: VideoScreenUiEvent()
    data object CloseComments: VideoScreenUiEvent()
    data class OpenProfile(val userId: Long): VideoScreenUiEvent()
    data class SaveComment(val commentId: Long?, val text: String): VideoScreenUiEvent()
    data class RemoveComment(val commentId: Long): VideoScreenUiEvent()
    data object ReachedBottom: VideoScreenUiEvent()
}