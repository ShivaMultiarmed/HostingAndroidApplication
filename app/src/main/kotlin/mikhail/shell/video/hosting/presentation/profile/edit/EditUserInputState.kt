package mikhail.shell.video.hosting.presentation.profile.edit

import mikhail.shell.video.hosting.domain.models.EditAction

data class EditUserInputState(
    val nick: String,
    val name: String,
    val avatar: String?,
    val avatarAction: EditAction,
    val age: String,
    val bio: String,
    val tel: String,
    val email: String
)
