package mikhail.shell.video.hosting.presentation.navigation.authentication

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.navigation
import mikhail.shell.video.hosting.presentation.navigation.common.Route

fun NavGraphBuilder.authenticationGraph(
    navController: NavController
) {
    navigation<Route.Authentication>(
        startDestination = Route.Authentication.SignIn
    ) {
        signInRoute(navController)
        signUpRoute(navController)
    }
}