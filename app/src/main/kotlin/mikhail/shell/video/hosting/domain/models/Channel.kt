package mikhail.shell.video.hosting.domain.models

data class Channel(
    val channelId: Long? = null,
    val ownerId: Long,
    val title: String,
    val alias: String? = null,
    val description: String? = null,
    val subscribers: Long = 0,
    val coverUrl: String? = null,
    val avatarUrl: String? = null
)
data class ChannelWithUser(
    val channelId: Long? = null,
    val ownerId: Long,
    val title: String,
    val alias: String,
    val description: String,
    val subscribers: Long = 0,
    val subscription: SubscriptionState,
    val coverUrl: String? = null,
    val avatarUrl: String? = null
)