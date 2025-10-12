package mikhail.shell.video.hosting.presentation.user.models

import mikhail.shell.video.hosting.domain.models.User

data class UserUi(
    val userId: Long,
    val nick: String,
    val name: String?,
    val avatar: String?,
    val bio: String?,
    val tel: String?,
    val email: String?
)

fun User.toUi(avatar: String) = UserUi(
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
    bio = bio,
    tel = tel?.removePrefix("+"),
    email = email
)