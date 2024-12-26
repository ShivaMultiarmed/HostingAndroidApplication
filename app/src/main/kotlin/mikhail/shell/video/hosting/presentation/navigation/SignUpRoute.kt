package mikhail.shell.video.hosting.presentation.navigation

import android.app.Activity.MODE_PRIVATE
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.coroutines.launch
import mikhail.shell.video.hosting.presentation.signup.password.SignUpScreen
import mikhail.shell.video.hosting.presentation.signup.password.SignUpWithPasswordViewModel

fun NavGraphBuilder.signUpRoute(
    navController: NavController
) {
    composable<Route.SignUp> {
        val viewModel = hiltViewModel<SignUpWithPasswordViewModel>()
        val state by viewModel.state.collectAsStateWithLifecycle()
        val sharedPref =
            LocalContext.current.applicationContext.getSharedPreferences(
                "user_details",
                MODE_PRIVATE
            )
        val coroutineScope = rememberCoroutineScope()
        SignUpScreen(
            state = state,
            onSubmit = {
                viewModel.signUp(it)
            },
            onSuccess = {
                coroutineScope.launch {
                    navController.navigate(Route.Channel(1))
                }
            }
        )
    }
}