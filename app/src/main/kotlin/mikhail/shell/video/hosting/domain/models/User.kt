package mikhail.shell.video.hosting.domain.models

data class User(
    val userId: Long? = null,
    val name: String,
    val nick: String? = null,
    val avatar: String? = null,
    val age: Byte? = null,
    val bio: String? = null,
    val tel: Int? = null,
    val email: String? = null
)