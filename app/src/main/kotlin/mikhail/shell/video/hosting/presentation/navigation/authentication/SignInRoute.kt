package mikhail.shell.video.hosting.presentation.navigation.authentication

import android.app.Activity.MODE_PRIVATE
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.edit
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.coroutines.launch
import mikhail.shell.video.hosting.presentation.navigation.Route
import mikhail.shell.video.hosting.presentation.signin.password.SignInScreen
import mikhail.shell.video.hosting.presentation.signin.password.SignInWithPasswordViewModel

fun NavGraphBuilder.signInRoute(
    navController: NavController
) {
    composable<Route.Authentication.SignIn> {
        val viewModel = hiltViewModel<SignInWithPasswordViewModel>()
        val state by viewModel.state.collectAsStateWithLifecycle()
        val sharedPref = LocalContext.current.applicationContext.getSharedPreferences(
            "user_details",
            MODE_PRIVATE
        )
        val coroutineScope = rememberCoroutineScope()
        SignInScreen(
            state = state,
            onSubmit = viewModel::signIn,
            onSuccess = {
                if (state.authModel != null) {
                    coroutineScope.launch {
                        sharedPref.edit {
                            putLong("userId", state.authModel!!.userId)
                            putString("token", state.authModel!!.token)
                            commit()
                        }
                        viewModel.subscribeToNotifications()
                    }.invokeOnCompletion {
                        navController.navigate(Route.Search)
                    }
                }
            },
            onSigningUp = {
                navController.navigate(Route.Authentication.SignUp)
            }
        )
    }
}
