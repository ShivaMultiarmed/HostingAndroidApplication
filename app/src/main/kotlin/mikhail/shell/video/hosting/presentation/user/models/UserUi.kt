package mikhail.shell.video.hosting.presentation.user.models

import mikhail.shell.video.hosting.domain.models.User

data class UserUi(
    val userId: Long? = null,
    val nick: String,
    val name: String? = null,
    val avatar: String? = null,
    val bio: String? = null,
    val tel: String? = null,
    val email: String? = null
)

fun User.toUi() = UserUi(
    userId = userId,
    nick = nick,
    name = name,
    avatar = avatar,
    bio = bio,
    tel = tel?.let { "+$it" },
    email = email
)

fun UserUi.toDomain() = User(
    userId = userId,
    nick = nick,
    name = name,
    avatar = avatar,
    bio = bio,
    tel = tel?.replace("+",""),
    email = email
)