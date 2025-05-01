package mikhail.shell.video.hosting.presentation.navigation.video

import androidx.media3.common.Player
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.navigation
import mikhail.shell.video.hosting.domain.providers.UserDetailsProvider
import mikhail.shell.video.hosting.presentation.navigation.Route

fun NavGraphBuilder.videoGraph(
    navController: NavController,
    player: Player,
    userDetailsProvider: UserDetailsProvider,
    onFullScreen: (Boolean) -> Unit = {}
) {
    navigation<Route.Video>(
        startDestination = Route.Video.Upload
    ) {
        videoRoute(
            navController = navController,
            player = player,
            userDetailsProvider = userDetailsProvider,
            onFullScreen = onFullScreen
        )
        videoEditRoute(navController)
        uploadVideoRoute(
            navController = navController,
            userDetailsProvider = userDetailsProvider,
            onFullScreen = onFullScreen
        )
    }
}