package mikhail.shell.video.hosting.domain.models

import mikhail.shell.video.hosting.presentation.utils.EditingState

data class Channel(
    val channelId: Long,
    val ownerId: Long,
    val title: String,
    val alias: String?,
    val description: String?,
    val subscribers: Long
)
data class ChannelForUser(
    val channelId: Long,
    val ownerId: Long,
    val title: String,
    val alias: String?,
    val description: String?,
    val subscribers: Long,
    val subscription: Subscription
)
data class ChannelCreationModel(
    val title: String,
    val alias: String?,
    val ownerId: Long,
    val description: String?,
    val logo: String?,
    val header: String?
)
data class ChannelEditingModel(
    val channelId: Long,
    val title: String,
    val alias: String?,
    val description: String?,
    val logo: EditingState<String?>,
    val header: EditingState<String?>,
)