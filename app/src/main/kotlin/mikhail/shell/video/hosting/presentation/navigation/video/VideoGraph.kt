package mikhail.shell.video.hosting.presentation.navigation.video

import androidx.media3.common.Player
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.navigation
import mikhail.shell.video.hosting.domain.providers.UserDetailsProvider
import mikhail.shell.video.hosting.presentation.navigation.common.Route

fun NavGraphBuilder.videoGraph(
    navController: NavController,
    player: Player,
    userDetailsProvider: UserDetailsProvider
) {
    navigation<Route.Video>(
        startDestination = Route.Video.Recommendations
    ) {
        videoRoute(
            navController = navController,
            player = player,
            userDetailsProvider = userDetailsProvider
        )
        videoEditRoute(navController)
        uploadVideoRoute(
            navController = navController,
            userDetailsProvider = userDetailsProvider
        )
        searchRoute(navController)
        videoRecommendationsRoute(navController)
    }
}