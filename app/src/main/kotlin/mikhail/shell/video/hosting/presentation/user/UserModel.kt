package mikhail.shell.video.hosting.presentation.user

import mikhail.shell.video.hosting.domain.models.User

data class UserModel(
    val nick: String,
    val name: String? = null,
    val avatar: String? = null,
    val bio: String? = null,
    val tel: String? = null,
    val email: String? = null
)

fun User.toModel() = UserModel(nick, name, avatar, bio, tel?.let { "+$it" }, email)
fun UserModel.toDomain(userId: Long) = User(userId, nick, name, avatar, bio, tel, email)
