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
    val userId = userDetailsProvider.getUserId()
    navigation<Route.User>(
        startDestination = Route.User.Profile(userId)
    ) {
        profileRoute(navController, userDetailsProvider)
        subscriptionsRoute(navController, userDetailsProvider)
        settingsRoute(navController)
        inviteUserRoute(navController)
        editUserRoute(navController, userDetailsProvider)
    }
}