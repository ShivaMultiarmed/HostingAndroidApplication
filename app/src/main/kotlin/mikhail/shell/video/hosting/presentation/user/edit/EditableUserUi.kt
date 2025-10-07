package mikhail.shell.video.hosting.presentation.user.edit

import mikhail.shell.video.hosting.domain.models.User

data class EditableUserUi(
    val userId: Long,
    val nick: String,
    val name: String,
    val avatar: String?,
    val bio: String,
    val tel: String,
    val email: String
)

fun User.toEditUi(
    avatar: String
) = EditableUserUi(
    userId = userId!!,
    nick = nick,
    name = name?: "",
    bio = bio?: "",
    tel = tel?.let { "+$it" }?: "",
    email = email?: "",
    avatar = avatar
)
