package mikhail.shell.video.hosting.presentation.navigation.authentication.signup.password

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.navigation
import mikhail.shell.video.hosting.domain.providers.UserDetailsProvider
import mikhail.shell.video.hosting.presentation.navigation.common.Route

fun NavGraphBuilder.signUpGraph(
    navController: NavController,
    userDetailsProvider: UserDetailsProvider
) {
    navigation<Route.Authentication.SignUp>(
        startDestination = Route.Authentication.SignUp.Request
    ) {
        requestSignUpRoute(navController)
        verifySignUpRoute(navController)
        confirmSignUpRoute(navController, userDetailsProvider)
    }
}