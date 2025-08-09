package mikhail.shell.video.hosting.presentation.user

import mikhail.shell.video.hosting.domain.models.User

data class UserModel(
    val userId: Long? = null,
    val nick: String,
    val name: String? = null,
    val avatar: String? = null,
    val bio: String? = null,
    val tel: String? = null,
    val email: String? = null
)

fun User.toModel() = UserModel(userId, nick, name, avatar, bio, tel?.let { "+$it" }, email)
fun UserModel.toDomain() = User(userId, nick, name, avatar, bio, tel, email)