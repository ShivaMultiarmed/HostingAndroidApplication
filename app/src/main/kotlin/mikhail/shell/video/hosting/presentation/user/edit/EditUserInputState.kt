package mikhail.shell.video.hosting.presentation.user.edit

import mikhail.shell.video.hosting.domain.models.EditAction

data class EditUserInputState(
    val nick: String,
    val name: String,
    val avatar: String?,
    val avatarAction: EditAction,
    val bio: String,
    val tel: String,
    val email: String
)
