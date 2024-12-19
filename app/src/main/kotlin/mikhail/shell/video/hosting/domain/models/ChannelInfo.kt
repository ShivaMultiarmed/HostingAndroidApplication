package mikhail.shell.video.hosting.domain.models


data class ChannelInfo(
    val channelId: Long,
    val ownerId: Long,
    val title: String,
    val alias: String,
    val description: String,
    val subscribers: Long
)

data class ExtendedChannelInfo(
    val info: ChannelInfo,
    val subscription: Boolean
)