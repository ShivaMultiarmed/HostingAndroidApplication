package mikhail.shell.video.hosting.presentation.channel.edit

data class EditedChannelUi(
    val channelId: Long,
    val title: String,
    val alias: String,
    val description: String,
    val avatar: String,
    val cover: String
)
