package mikhail.shell.video.hosting.presentation.user.models

import androidx.compose.runtime.Immutable
import mikhail.shell.video.hosting.domain.models.ImageSize
import mikhail.shell.video.hosting.domain.models.User

@Immutable
data class UserUi(
    val userId: Long,
    val nick: String,
    val name: String?,
    val avatar: Map<ImageSize, String>,
    val bio: String?,
    val tel: String?,
    val email: String?
)

fun User.toUi(avatar: Map<ImageSize, String>) = UserUi(
    userId = userId,
    nick = nick,
    name = name,
    avatar = avatar,
    bio = bio,
    tel = tel?.let { "+$it" },
    email = email
)

fun UserUi.toDomain() = User(
    userId = userId,
    nick = nick,
    name = name,
    bio = bio,
    tel = tel?.removePrefix("+"),
    email = email
)