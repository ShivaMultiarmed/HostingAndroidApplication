package mikhail.shell.video.hosting.data.dto

import mikhail.shell.video.hosting.domain.models.User

data class SignUpDto(
    val userName: String,
    val password: String,
    val userDto: UserDto
)

data class UserDto(
    val userId: Long?,
    val name: String
)

fun User.toDto() = UserDto(userId, name)
fun UserDto.toDomain() = User(userId, name)