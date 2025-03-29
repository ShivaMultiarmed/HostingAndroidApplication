package mikhail.shell.video.hosting.data.dto

import mikhail.shell.video.hosting.domain.models.User

data class UserDto(
    val userId: Long? = null,
    val name: String,
    val nick: String? = null,
    val avatar: String? = null,
    val age: Byte? = null,
    val bio: String? = null,
    val tel: Int? = null,
    val email: String? = null
)

fun User.toDto() = UserDto(userId, name, nick, avatar, age, bio, tel, email)
fun UserDto.toDomain() = User(userId, name, nick, avatar, age, bio, tel, email)