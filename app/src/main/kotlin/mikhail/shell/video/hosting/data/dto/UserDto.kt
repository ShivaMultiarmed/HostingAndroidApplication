package mikhail.shell.video.hosting.data.dto

import mikhail.shell.video.hosting.domain.models.User

data class UserDto(
    val userId: Long,
    val nick: String,
    val name: String?,
    val bio: String?,
    val tel: String?,
    val email: String?
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