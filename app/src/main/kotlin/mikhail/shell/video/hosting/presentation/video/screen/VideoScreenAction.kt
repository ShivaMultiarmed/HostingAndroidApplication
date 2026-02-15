package mikhail.shell.video.hosting.presentation.video.screen

import mikhail.shell.video.hosting.domain.models.Liking
import mikhail.shell.video.hosting.domain.models.Subscription

sealed class VideoScreenAction {
    data object Restart : VideoScreenAction()
    data class Like(val liking: Liking) : VideoScreenAction()
    data class Subscribe(val subscription: Subscription) : VideoScreenAction()
    data object DownLoad : VideoScreenAction()
    data object Share : VideoScreenAction()
    data object Remove : VideoScreenAction()
    data object Edit : VideoScreenAction()
    data object OpenChannel : VideoScreenAction()
    data class OpenProfile(val userId: Long) : VideoScreenAction()
    data object SubmitComment : VideoScreenAction()
    data class ChangeCommentText(val text: String): VideoScreenAction()
    data class EditComment(val commentId: Long) : VideoScreenAction()
    data object CancelEditingComment: VideoScreenAction()
    data class RemoveComment(val commentId: Long) : VideoScreenAction()
    data object RestartComments : VideoScreenAction()
    data object LoadNextCommentsPart : VideoScreenAction()
    data object Exit: VideoScreenAction()
}