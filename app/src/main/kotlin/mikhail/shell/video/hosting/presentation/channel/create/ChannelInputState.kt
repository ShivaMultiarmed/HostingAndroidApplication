package mikhail.shell.video.hosting.presentation.channel.create

import mikhail.shell.video.hosting.domain.models.File

class ChannelInputState(
    val title: String? = null,
    val alias: String? = "",
    val description: String? = "",
    val cover: File? = null,
    val avatar: File? = null
)