package mikhail.shell.video.hosting.domain.models

data class User(
    val userId: Long? = null,
    val name: String? = null,
    val nick: String,
    val avatar: String? = null,
    val bio: String? = null,
    val tel: String? = null,
    val email: String? = null
)