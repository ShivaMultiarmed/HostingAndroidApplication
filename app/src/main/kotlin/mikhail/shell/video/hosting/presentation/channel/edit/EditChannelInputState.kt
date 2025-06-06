package mikhail.shell.video.hosting.presentation.channel.edit

import mikhail.shell.video.hosting.domain.models.EditAction

data class EditChannelInputState(
    val title: String = "",
    val alias: String = "",
    val description: String = "",
    val cover: String? = null,
    val editCoverAction: EditAction = EditAction.KEEP,
    val avatar: String? = null,
    val editAvatarAction: EditAction = EditAction.KEEP
)
