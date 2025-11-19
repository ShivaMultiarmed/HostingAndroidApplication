package mikhail.shell.video.hosting.domain.models

data class User(
    val userId: Long,
    val nick: String,
    val name: String?,
    val bio: String?,
    val tel: String?,
    val email: String?
)

data class UserCreationModel(
    val nick: String
)

data class UserEditingModel(
    val userId: Long,
    val nick: String,
    val name: String?,
    val bio: String?,
    val tel: String?,
    val email: String?,
    val avatar: String?,
    val avatarAction: EditAction
)