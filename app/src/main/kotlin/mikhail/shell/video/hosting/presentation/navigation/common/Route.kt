package mikhail.shell.video.hosting.presentation.navigation.common

import android.os.Parcelable
import androidx.navigation3.runtime.NavKey
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Serializable
@Parcelize
sealed interface Route: NavKey, Parcelable {
    @Serializable
    @Parcelize
    data object Authentication: Route {
        @Serializable
        @Parcelize
        data object SignIn: Route
        @Serializable
        @Parcelize
        data object SignUp: Route {
            @Serializable
            @Parcelize
            data object Request: Route
            @Serializable
            @Parcelize
            data class Verification(val userName: String): Route
            @Serializable
            @Parcelize
            data class Confirmation(val token: String): Route
        }
        @Serializable
        @Parcelize
        data object Reset: Route {
            @Serializable
            @Parcelize
            data object Request: Route
            @Serializable
            @Parcelize
            data class Verification(
                val userId: Long,
                val userName: String
            ): Route
            @Serializable
            @Parcelize
            data class Confirmation(val token: String): Route
        }
    }
    @Serializable
    @Parcelize
    data object Subscriptions: Route {
        @Serializable
        @Parcelize
        data object View: Route
    }
    @Serializable
    @Parcelize
    data class User(val userId: Long): Route {
        @Serializable
        @Parcelize
        data class Profile(val userId: Long): Route
        @Serializable
        @Parcelize
        data object Edit: Route
        @Serializable
        @Parcelize
        data object Settings: Route
        @Serializable
        @Parcelize
        data object ChannelCreation: Route
        @Serializable
        @Parcelize
        data object VideoUploading: Route
    }
    @Serializable
    @Parcelize
    data class Video(val videoId: Long): Route {
        @Serializable
        @Parcelize
        data class View(val videoId: Long): Route
        @Serializable
        data class Edit(val videoId: Long): Route
    }
    @Serializable
    @Parcelize
    data object Recommendations: Route {
        @Serializable
        @Parcelize
        data object View: Route
    }
    @Serializable
    @Parcelize
    data object Search: Route {
        @Serializable
        @Parcelize
        data object View: Route
    }
    @Serializable
    data class Channel(val channelId: Long): Route {
        @Serializable
        data class View(val channelId: Long): Route
        @Serializable
        data class Edit(val channelId: Long): Route
    }
}