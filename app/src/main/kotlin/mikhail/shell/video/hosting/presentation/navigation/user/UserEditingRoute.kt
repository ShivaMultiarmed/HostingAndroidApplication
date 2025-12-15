package mikhail.shell.video.hosting.presentation.navigation.user

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.Player
import androidx.navigation3.runtime.EntryProviderScope
import kotlinx.coroutines.launch
import mikhail.shell.video.hosting.domain.errors.network.NetworkError
import mikhail.shell.video.hosting.domain.providers.UserDetailsProvider
import mikhail.shell.video.hosting.domain.validation.getStandardErrorMessage
import mikhail.shell.video.hosting.presentation.navigation.common.Route
import mikhail.shell.video.hosting.presentation.user.edit.UserEditingScreen
import mikhail.shell.video.hosting.presentation.user.edit.UserEditingViewModel
import mikhail.shell.video.hosting.presentation.utils.logOut
import mikhail.shell.video.hosting.presentation.utils.observe
import mikhail.shell.video.hosting.presentation.user.edit.UserEditingScreenEvent as ScreenEvent

fun EntryProviderScope<Route>.userEditingRoute(
    rootBackStack: MutableList<Route>,
    userBackStack: MutableList<Route>,
    userDetailsProvider: UserDetailsProvider,
    player: Player
) {
    entry<Route.User.Edit> { route ->
        val context = LocalContext.current
        val coroutineScope = rememberCoroutineScope()
        val userId = rememberSaveable{ userDetailsProvider.getUserId() }
        val viewModel = hiltViewModel<UserEditingViewModel, UserEditingViewModel.Factory> { factory ->
            factory.create(userId)
        }
        val state by viewModel.state.collectAsStateWithLifecycle()
        val events = viewModel.events
        val snackBarHostState = remember { SnackbarHostState() }
        UserEditingScreen(
            state = state,
            onAction = viewModel::onAction,
            snackBarHostState = snackBarHostState
        )
        events.observe { event ->
            when (event) {
                ScreenEvent.Cancelled -> userBackStack.removeLastOrNull()
                is ScreenEvent.Failure -> {
                    if (event.error in setOf(NetworkError.AUTHENTICATION, NetworkError.NOT_FOUND)) {
                        rootBackStack.add(Route.Authentication)
                    } else {
                        context.getStandardErrorMessage(event.error)?.let {
                            coroutineScope.launch {
                                snackBarHostState.showSnackbar(it)
                            }
                        }
                    }
                }
                ScreenEvent.Removed -> {
                    player.stop()
                    player.clearMediaItems()
                    logOut(userDetailsProvider, rootBackStack)
                }
                ScreenEvent.Success -> {
                    userBackStack.removeLastOrNull()
                    userBackStack.removeLastOrNull()
                    userBackStack.add(Route.User.Profile(userId))
                }
            }
        }
    }
}