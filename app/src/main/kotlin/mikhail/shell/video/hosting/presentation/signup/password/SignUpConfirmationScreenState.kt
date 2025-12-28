package mikhail.shell.video.hosting.presentation.signup.password

data class SignUpConfirmationScreenState(
    val user: SignUpConfirmationInputState = SignUpConfirmationInputState(),
    val isLoading: Boolean = false
)