package mikhail.shell.video.hosting.presentation.navigation

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
    data object CreateChannel: Route()
    @Serializable
    data object UploadVideo: Route()
    @Serializable
    data class EditVideo(val videoId: Long): Route()
    @Serializable
    data object Profile: Route()
    @Serializable
    data object Subscriptions: Route()
    @Serializable
    data class Channel(val channelId: Long): Route()
    @Serializable
    data class Video(val videoId: Long): Route()
}