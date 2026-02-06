package mikhail.shell.video.hosting.presentation.navigation.common

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
sealed interface Route: NavKey, java.io.Serializable {
    @Serializable
    data object Authentication: Route {
        private fun readResolve(): Any = Authentication
        @Serializable
        data object SignIn: Route {
            private fun readResolve(): Any = SignIn
        }
        @Serializable
        data object SignUp: Route {
            private fun readResolve(): Any = SignUp
            @Serializable
            data object Request: Route {
                private fun readResolve(): Any = Request
            }
            @Serializable
            data class Verification(val userName: String): Route
            @Serializable
            data class Confirmation(val token: String): Route
        }
        @Serializable
        data object Reset: Route {
            private fun readResolve(): Any = Reset
            @Serializable
            data object Request: Route {
                private fun readResolve(): Any = Request
            }
            @Serializable
            data class Verification(
                val userId: Long,
                val userName: String
            ): Route
            @Serializable
            data class Confirmation(val token: String): Route
        }
    }
    @Serializable
    data object Subscriptions: Route {
        private fun readResolve(): Any = Subscriptions
        @Serializable
        data object View: Route {
            private fun readResolve(): Any = View
        }
    }
    @Serializable
    data class User(val userId: Long): Route {
        @Serializable
        data class Profile(val userId: Long): Route
        @Serializable
        data object Edit: Route {
            private fun readResolve(): Any = Edit
        }
        @Serializable
        data object Settings: Route {
            private fun readResolve(): Any = Settings
        }
        @Serializable
        data object ChannelCreation: Route {
            private fun readResolve(): Any = ChannelCreation
        }
        @Serializable
        data object VideoUploading: Route {
            private fun readResolve(): Any = VideoUploading
        }
    }
    @Serializable
    data class Video(val videoId: Long): Route {
        @Serializable
        data class View(val videoId: Long): Route
        @Serializable
        data class Edit(val videoId: Long): Route
    }
    @Serializable
    data object Recommendations: Route {
        private fun readResolve(): Any = Recommendations
        @Serializable
        data object View: Route {
            private fun readResolve(): Any = View
        }
    }
    @Serializable
    data object Search: Route {
        private fun readResolve(): Any = Search
        @Serializable
        data object View: Route {
            private fun readResolve(): Any = View
        }
    }
    @Serializable
    data class Channel(val channelId: Long): Route {
        @Serializable
        data class View(val channelId: Long): Route
        @Serializable
        data class Edit(val channelId: Long): Route
    }
}
