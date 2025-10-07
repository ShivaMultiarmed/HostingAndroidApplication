package mikhail.shell.video.hosting.data.dto

import mikhail.shell.video.hosting.domain.models.User

data class UserDto(
    val userId: Long? = null,
    val nick: String,
    val name: String? = null,
    val bio: String? = null,
    val tel: String? = null,
    val email: String? = null
)

fun User.toDto() = UserDto(
    userId = userId,
    nick = nick,
    name = name,
    bio = bio,
    tel = tel,
    email = email
)
fun UserDto.toDomain() = User(
    userId = userId,
    nick = nick,
    name = name,
    bio = bio,
    tel = tel,
    email = email
)