package mikhail.shell.video.hosting.domain.models

data class AuthModel(
    val token: String,
    val userId: Long
)

data class SignUpModel(
    val password: String,
    val user: UserCreationModel
)