package mikhail.shell.video.hosting.data.dto

import mikhail.shell.video.hosting.domain.models.User

data class UserDto(
    val userId: Long? = null,
    val nick: String,
    val name: String? = null,
    val avatar: String? = null,
    val bio: String? = null,
    val tel: String? = null,
    val email: String? = null
)

fun User.toDto() = UserDto(userId, nick, name, avatar, bio, tel, email)
fun UserDto.toDomain() = User(userId, nick, name, avatar, bio, tel, email)