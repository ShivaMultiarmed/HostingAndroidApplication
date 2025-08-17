package mikhail.shell.video.hosting.domain.models

data class Channel(
    val channelId: Long? = null,
    val ownerId: Long,
    val title: String,
    val alias: String? = null,
    val description: String? = null,
    val subscribers: Long = 0,
    val cover: String? = null,
    val avatar: String? = null
)
data class ChannelForUser(
    val channelId: Long? = null,
    val ownerId: Long,
    val title: String,
    val alias: String? = null,
    val description: String? = null,
    val subscribers: Long = 0,
    val subscription: Subscription = Subscription.NOT_SUBSCRIBED,
    val coverUrl: String? = null,
    val avatarUrl: String? = null
)