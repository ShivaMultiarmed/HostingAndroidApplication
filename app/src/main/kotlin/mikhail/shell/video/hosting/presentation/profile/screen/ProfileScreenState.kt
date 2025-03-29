package mikhail.shell.video.hosting.presentation.profile.screen

import mikhail.shell.video.hosting.domain.errors.ChannelLoadingError
import mikhail.shell.video.hosting.domain.models.Channel
import mikhail.shell.video.hosting.domain.models.User

data class ProfileScreenState(
    val user: User? = null, // TODO: change to UserModel
    val channels: List<Channel>? = null,
    val userError: Error? = null,
    val channelError: ChannelLoadingError? = null,
    val isLoading: Boolean = false
)

data class UserModel(
    val userId: Long? = null,
    val name: String,
    val nick: String? = null,
    val age: Byte? = null,
    val bio: String? = null,
    val tel: Int? = null,
    val email: String? = null
)
