package mikhail.shell.video.hosting.presentation.navigation.user

import androidx.media3.common.Player
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.navigation
import mikhail.shell.video.hosting.domain.providers.UserDetailsProvider
import mikhail.shell.video.hosting.presentation.navigation.common.Route

fun NavGraphBuilder.userGraph(
    navController: NavController,
    userDetailsProvider: UserDetailsProvider,
    player: Player
) {
    val userId = userDetailsProvider.getUserId()
    navigation<Route.User>(
        startDestination = Route.User.Profile(userId)
    ) {
        profileRoute(navController, userDetailsProvider, player)
        subscriptionsRoute(navController, userDetailsProvider)
        settingsRoute(navController)
        editUserRoute(navController, userDetailsProvider, player)
    }
}