package mikhail.shell.video.hosting.presentation.navigation.user

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.Player
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import mikhail.shell.video.hosting.domain.errors.network.NetworkError
import mikhail.shell.video.hosting.domain.providers.UserDetailsProvider
import mikhail.shell.video.hosting.presentation.navigation.common.Route
import mikhail.shell.video.hosting.presentation.user.edit.UserEditingScreen
import mikhail.shell.video.hosting.presentation.user.edit.UserEditingScreenState
import mikhail.shell.video.hosting.presentation.user.edit.UserEditingUiEvent
import mikhail.shell.video.hosting.presentation.user.edit.UserEditingViewModel
import mikhail.shell.video.hosting.presentation.utils.logOut

fun NavGraphBuilder.editUserRoute(
    navController: NavController,
    userDetailsProvider: UserDetailsProvider,
    player: Player
) {
    composable<Route.User.Edit> {
        val userId = userDetailsProvider.getUserId()
        val viewModel = hiltViewModel<UserEditingViewModel, UserEditingViewModel.Factory> { it.create(userId) }
        val state by viewModel.state.collectAsStateWithLifecycle()
        UserEditingScreen(
            state = state,
            onEvent = {
                when (it) {
                    UserEditingUiEvent.Cancel -> navController.popBackStack()
                    else -> viewModel.onEvent(it)
                }
            }
        )
        LaunchedEffect(state) {
            if (state is UserEditingScreenState.Success) {
                navController.navigate(Route.User.Profile(userId))
            } else if (state is UserEditingScreenState.Removed) {
                player.stop()
                player.clearMediaItems()
                logOut(userDetailsProvider, navController)
            } else if (state is UserEditingScreenState.Failure) {
                if ((state as UserEditingScreenState.Failure).error == NetworkError.AUTHENTICATION) {
                    navController.navigate(Route.Authentication)
                } else if ((state as UserEditingScreenState.Failure).error == NetworkError.NOT_FOUND){
                    navController.popBackStack()
                }
            }
        }
    }
}