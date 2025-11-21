package mikhail.shell.video.hosting.data.dto

data class SignUpRequest(
    val password: String,
    val user: UserCreationRequest
)

data class UserCreationRequest(
    val nick: String
)