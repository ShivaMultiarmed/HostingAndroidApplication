package mikhail.shell.video.hosting.presentation

import kotlinx.serialization.Serializable

@Serializable
sealed class Route {
    @Serializable
    data object SignIn: Route()
    @Serializable
    data object SignUp: Route()
    @Serializable
    data object Search: Route()
    @Serializable
    data class Channel(val channelId: Long): Route()
    @Serializable
    data class Video(val videoId: Long): Route()
}