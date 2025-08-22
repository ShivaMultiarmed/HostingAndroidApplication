package mikhail.shell.video.hosting.presentation.channel.edit

data class EditableChannelUi(
    val channelId: Long,
    val ownerId: Long,
    val title: String,
    val alias: String,
    val description: String,
    val logo: String,
    val logoExists: Boolean? = null,
    val header: String,
    val headerExists: Boolean? = null
)
