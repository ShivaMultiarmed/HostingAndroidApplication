package mikhail.shell.video.hosting.presentation.subscriptions

sealed class SubscriptionsScreenAction {
    data object Restart : SubscriptionsScreenAction()
    data object LoadNextPart: SubscriptionsScreenAction()
    data class ChooseChannel(val channelId: Long) : SubscriptionsScreenAction()
}