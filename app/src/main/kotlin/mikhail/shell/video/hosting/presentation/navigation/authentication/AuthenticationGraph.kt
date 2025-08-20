package mikhail.shell.video.hosting.presentation.navigation.authentication

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.navigation
import mikhail.shell.video.hosting.domain.providers.UserDetailsProvider
import mikhail.shell.video.hosting.presentation.navigation.authentication.reset.resetGraph
import mikhail.shell.video.hosting.presentation.navigation.authentication.signup.password.signUpGraph
import mikhail.shell.video.hosting.presentation.navigation.common.Route

fun NavGraphBuilder.authenticationGraph(
    navController: NavController,
    userDetailsProvider: UserDetailsProvider
) {
    navigation<Route.Authentication>(
        startDestination = Route.Authentication.SignIn
    ) {
        signInRoute(
            navController = navController,
            userDetailsProvider = userDetailsProvider
        )
        signUpGraph(
            navController = navController,
            userDetailsProvider = userDetailsProvider
        )
        resetGraph(navController)
    }
}