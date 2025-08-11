package mikhail.shell.video.hosting.presentation.navigation.user

import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.Player
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import mikhail.shell.video.hosting.domain.providers.UserDetailsProvider
import mikhail.shell.video.hosting.presentation.navigation.common.Route
import mikhail.shell.video.hosting.presentation.user.edit.EditUserScreen
import mikhail.shell.video.hosting.presentation.user.edit.EditUserViewModel
import mikhail.shell.video.hosting.presentation.utils.logOut

fun NavGraphBuilder.editUserRoute(
    navController: NavController,
    userDetailsProvider: UserDetailsProvider,
    player: Player
) {
    composable<Route.User.Edit> {
        val userId = userDetailsProvider.getUserId()
        val viewModel = hiltViewModel<EditUserViewModel, EditUserViewModel.Factory> { it.create(userId) }
        val state by viewModel.state.collectAsStateWithLifecycle()
        val coroutineScope = rememberCoroutineScope()
        EditUserScreen(
            userId = userId,
            state = state,
            onInitialize = viewModel::loadUser,
            onEdit = viewModel::editUser,
            onEditSuccess = {
                navController.navigate(Route.User.Profile(it))
            },
            onRemove = {
                player.stop()
                player.clearMediaItems()
                viewModel.removeUser()
            },
            onRemoveSuccess = {
                logOut(userDetailsProvider, navController)
            },
            onPopup = navController::popBackStack,
            onUserNotFound = {
                coroutineScope.launch {
                    delay(800)
                    navController.popBackStack()
                }
            },
            onAuthenticationRequired = {
                coroutineScope.launch {
                    delay(800)
                    navController.navigate(Route.Authentication)
                }
            }
        )
    }
}