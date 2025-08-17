package mikhail.shell.video.hosting.presentation.user.edit

import mikhail.shell.video.hosting.domain.models.User

data class EditUserUi(
    val userId: Long? = null,
    val nick: String,
    val name: String,
    val avatar: String? = null,
    val bio: String,
    val tel: String,
    val email: String
)

fun User.toEditUi() = EditUserUi(
    userId = userId,
    nick = nick,
    name = name?: "",
    avatar = avatar,
    bio = bio?: "",
    tel = tel?.let { "+$it" }?: "",
    email = email?: ""
)
