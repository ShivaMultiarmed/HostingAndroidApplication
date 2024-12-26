package mikhail.shell.video.hosting.presentation.navigation

import android.app.Activity.MODE_PRIVATE
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.edit
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import mikhail.shell.video.hosting.presentation.signin.password.SignInScreen
import mikhail.shell.video.hosting.presentation.signin.password.SignInWithPasswordViewModel

fun NavGraphBuilder.signInRoute(
    navController: NavController
) {
    composable<Route.SignIn> {
        val viewModel = hiltViewModel<SignInWithPasswordViewModel>()
        val state by viewModel.state.collectAsStateWithLifecycle()
        val sharedPref =
            LocalContext.current.applicationContext.getSharedPreferences(
                "user_details",
                MODE_PRIVATE
            )
        SignInScreen(
            state = state,
            onSubmit = { email, password ->
                viewModel.signIn(email, password)
            },
            onSuccess = {
                if (state.authModel != null) {
                    sharedPref.edit {
                        putLong("userId", state.authModel!!.userId)
                        putString("token", state.authModel!!.token)
                        commit()
                    }
                }
                navController.navigate(Route.Channel(1))
            },
            onSigningUp = {
                navController.navigate(Route.SignUp)
            }
        )
    }
}
