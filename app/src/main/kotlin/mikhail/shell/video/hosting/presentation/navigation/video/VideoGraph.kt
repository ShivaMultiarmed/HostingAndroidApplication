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
    userDetailsProvider: UserDetailsProvider
) {
    navigation<Route.Video>(
        startDestination = Route.Video.Upload
    ) {
        videoRoute(navController, player, userDetailsProvider)
        videoEditRoute(navController)
        uploadVideoRoute(navController, userDetailsProvider)
    }
}