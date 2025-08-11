package mikhail.shell.video.hosting.presentation.utils

import androidx.navigation.NavController
import mikhail.shell.video.hosting.domain.providers.UserDetailsProvider
import mikhail.shell.video.hosting.presentation.navigation.common.Route

fun logOut(
    userDetailsProvider: UserDetailsProvider,
    navController: NavController
) {
    userDetailsProvider.remove()
    navController.navigate(Route.Authentication) {
        popUpTo<Route.Authentication> {
            inclusive = true
        }
    }
}