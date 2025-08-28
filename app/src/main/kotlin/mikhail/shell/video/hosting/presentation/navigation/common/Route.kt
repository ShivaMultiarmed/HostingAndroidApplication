package mikhail.shell.video.hosting.presentation.navigation.common

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

sealed class Route: NavKey {
    @Serializable
    data object Authentication: Route() {
        @Serializable
        data object SignIn: Route()
        @Serializable
        data object SignUp: Route() {
            @Serializable
            data object Request: Route()
            @Serializable
            data class Verification(val userName: String): Route()
            @Serializable
            data class Confirmation(val token: String): Route()
        }
        @Serializable
        data object Reset: Route() {
            @Serializable
            data object Request: Route()
            @Serializable
            data class Verification(val userName: String): Route()
            @Serializable
            data class Confirmation(val token: String): Route()
        }
    }
    @Serializable
    data object Subscriptions: Route()
    @Serializable
    data object User: Route() {
        @Serializable
        data class Profile(val userId: Long): Route()
        @Serializable
        data object Edit: Route()
        @Serializable
        data object Settings: Route()
        @Serializable
        data object CreateChannel: Route()
        @Serializable
        data object UploadVideo: Route()
    }
    @Serializable
    data object Video: Route() {
        @Serializable
        data class View(val videoId: Long): Route()
        @Serializable
        data class Edit(val videoId: Long): Route()
    }
    @Serializable
    data object Recommendations: Route()
    @Serializable
    data object Search: Route()
    @Serializable
    data object Channel: Route() {
        @Serializable
        data class View(val channelId: Long): Route()
        @Serializable
        data class Edit(val channelId: Long): Route()
    }
}
