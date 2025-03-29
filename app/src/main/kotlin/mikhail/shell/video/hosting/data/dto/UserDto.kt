package mikhail.shell.video.hosting.data.dto

import mikhail.shell.video.hosting.domain.models.User

data class UserDto(
    val userId: Long? = null,
    val name: String? = null,
    val nick: String,
    val avatar: String? = null,
    val bio: String? = null,
    val tel: String? = null,
    val email: String? = null
)

fun User.toDto() = UserDto(userId, name, nick, avatar, bio, tel, email)
fun UserDto.toDomain() = User(userId, name, nick, avatar, bio, tel, email)