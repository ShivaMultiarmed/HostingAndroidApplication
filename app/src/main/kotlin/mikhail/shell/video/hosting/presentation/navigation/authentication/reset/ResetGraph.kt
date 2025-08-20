package mikhail.shell.video.hosting.presentation.navigation.authentication.reset

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.navigation
import mikhail.shell.video.hosting.presentation.navigation.common.Route

fun NavGraphBuilder.resetGraph(
    navController: NavController
) {
    navigation<Route.Authentication.Reset>(
        startDestination = Route.Authentication.Reset.Request
    ) {
        requestResetRoute(navController)
        verifyResetRoute(navController)
        confirmResetRoute(navController)
    }
}