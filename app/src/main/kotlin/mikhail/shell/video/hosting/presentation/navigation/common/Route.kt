package mikhail.shell.video.hosting.presentation.navigation.common

import kotlinx.serialization.Serializable

@Serializable
sealed class Route {
    @Serializable
    data object Authentication: Route() {
        @Serializable
        data object SignIn: Route()
        @Serializable
        data object SignUp: Route()
    }
    @Serializable
    data object User: Route() {
        @Serializable
        data class Profile(val userId: Long): Route()
        @Serializable
        data object Edit: Route()
        @Serializable
        data object Subscriptions: Route()
        @Serializable
        data object Settings: Route()
    }
    @Serializable
    data object Video: Route() {
        @Serializable
        data class View(val videoId: Long): Route()
        @Serializable
        data class Edit(val videoId: Long): Route()
        @Serializable
        data object Upload: Route()
        @Serializable
        data object Search: Route()
        @Serializable
        data object Recommendations: Route()
    }
    @Serializable
    data object Channel: Route() {
        @Serializable
        data class View(val channelId: Long): Route()
        @Serializable
        data class Edit(val channelId: Long): Route()
        @Serializable
        data object Create: Route()
    }
}