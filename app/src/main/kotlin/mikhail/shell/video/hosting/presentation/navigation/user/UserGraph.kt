package mikhail.shell.video.hosting.presentation.navigation.user

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.navigation
import mikhail.shell.video.hosting.domain.providers.UserDetailsProvider
import mikhail.shell.video.hosting.presentation.navigation.Route

fun NavGraphBuilder.userGraph(
    navController: NavController,
    userDetailsProvider: UserDetailsProvider
) {
    navigation<Route.User>(
        startDestination = Route.User.Profile
    ) {
        profileRoute(navController, userDetailsProvider)
        createChannelRoute(navController, userDetailsProvider)
        uploadVideoRoute(navController, userDetailsProvider)
        settingsRoute(navController)
    }
}