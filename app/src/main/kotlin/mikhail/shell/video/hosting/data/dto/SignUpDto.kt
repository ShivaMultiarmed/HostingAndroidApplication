package mikhail.shell.video.hosting.data.dto

data class SignUpDto(
    val token: String,
    val password: String,
    val userDto: UserDto
)