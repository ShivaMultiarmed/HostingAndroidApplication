package mikhail.shell.video.hosting.presentation.channel.create

import java.io.File

class ChannelInputState(
    val title: String? = null,
    val alias: String? = "",
    val description: String? = "",
    val cover: File? = null,
    val avatar: File? = null
)