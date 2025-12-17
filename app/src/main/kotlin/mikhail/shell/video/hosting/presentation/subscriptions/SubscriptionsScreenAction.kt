package mikhail.shell.video.hosting.presentation.subscriptions

import mikhail.shell.video.hosting.presentation.subscriptions.SubscriptionsScreenAction as ScreenAction

sealed class SubscriptionsScreenAction {
    data object Restart : ScreenAction()
    data object LoadNextPart: ScreenAction()
    data class ChooseChannel(val channelId: Long) : ScreenAction()
}