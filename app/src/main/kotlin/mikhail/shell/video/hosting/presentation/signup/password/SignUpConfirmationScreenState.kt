package mikhail.shell.video.hosting.presentation.signup.password

data class SignUpConfirmationScreenState(
    val user: SignUpInputState = SignUpInputState(),
    val isLoading: Boolean = false
)