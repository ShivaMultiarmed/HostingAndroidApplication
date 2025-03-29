package mikhail.shell.video.hosting.presentation.profile.edit

data class EditUserInputState(
    val name: String,
    val avatar: String?,
    val age: String,
    val bio: String,
    val tel: String,
    val email: String
)
