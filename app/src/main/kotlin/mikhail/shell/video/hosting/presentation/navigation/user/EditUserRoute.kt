package mikhail.shell.video.hosting.presentation.navigation.user

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.Player
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import mikhail.shell.video.hosting.domain.providers.UserDetailsProvider
import mikhail.shell.video.hosting.presentation.navigation.Route
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
        val sharedPref = LocalContext.current.getSharedPreferences("user_details", Context.MODE_PRIVATE)
        EditUserScreen(
            userId = userId,
            state = state,
            onInitialize = viewModel::loadUser,
            onEdit = viewModel::editUser,
            onEditSuccess = {
                navController.navigate(Route.User.Profile(userId))
            },
            onRemove = {
                player.stop()
                player.clearMediaItems()

                viewModel.removeUser()
            },
            onRemoveSuccess = {
                logOut(sharedPref, navController)
            },
            onPopup = navController::popBackStack
        )
    }
}