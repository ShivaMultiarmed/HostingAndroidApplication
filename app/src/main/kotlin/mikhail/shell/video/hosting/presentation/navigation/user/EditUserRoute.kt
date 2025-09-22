package mikhail.shell.video.hosting.presentation.navigation.user

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.Player
import androidx.navigation3.runtime.EntryProviderBuilder
import androidx.navigation3.runtime.entry
import mikhail.shell.video.hosting.domain.errors.network.NetworkError
import mikhail.shell.video.hosting.domain.providers.UserDetailsProvider
import mikhail.shell.video.hosting.presentation.navigation.common.Route
import mikhail.shell.video.hosting.presentation.user.edit.UserEditingScreen
import mikhail.shell.video.hosting.presentation.user.edit.UserEditingScreenState
import mikhail.shell.video.hosting.presentation.user.edit.UserEditingUiEvent
import mikhail.shell.video.hosting.presentation.user.edit.UserEditingViewModel
import mikhail.shell.video.hosting.presentation.utils.logOut

fun EntryProviderBuilder<Route>.editUserRoute(
    rootBackStack: MutableList<Route>,
    userBackStack: MutableList<Route>,
    userDetailsProvider: UserDetailsProvider,
    player: Player
) {
    entry<Route.User.Edit> {
        val userId = userDetailsProvider.getUserId()
        val viewModel = hiltViewModel<UserEditingViewModel, UserEditingViewModel.Factory> { it.create(userId) }
        val state by viewModel.state.collectAsStateWithLifecycle()
        UserEditingScreen(
            state = state,
            onEvent = { event ->
                when (event) {
                    UserEditingUiEvent.Cancel -> userBackStack.removeLastOrNull()
                    else -> viewModel.onEvent(event)
                }
            }
        )
        LaunchedEffect(state) {
            if (state is UserEditingScreenState.Success) {
                userBackStack.removeFirstOrNull()
                userBackStack.add(Route.User.Profile(userId))
            } else if (state is UserEditingScreenState.Removed) {
                player.stop()
                player.clearMediaItems()
                logOut(userDetailsProvider, rootBackStack)
            } else if (state is UserEditingScreenState.Failure) {
                if ((state as UserEditingScreenState.Failure).error == NetworkError.AUTHENTICATION) {
                    rootBackStack.add(Route.Authentication)
                } else if ((state as UserEditingScreenState.Failure).error == NetworkError.NOT_FOUND){
                    userBackStack.clear()
                }
            }
        }
    }
}