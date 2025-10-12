package mikhail.shell.video.hosting.presentation.navigation.user

import android.content.Intent
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.Player
import androidx.navigation3.runtime.EntryProviderScope
import kotlinx.coroutines.launch
import mikhail.shell.video.hosting.R
import mikhail.shell.video.hosting.domain.errors.network.NetworkError
import mikhail.shell.video.hosting.domain.providers.UserDetailsProvider
import mikhail.shell.video.hosting.presentation.navigation.common.Route
import mikhail.shell.video.hosting.presentation.user.screen.ProfileScreen
import mikhail.shell.video.hosting.presentation.user.screen.ProfileScreenUiEvent
import mikhail.shell.video.hosting.presentation.user.screen.ProfileViewModel
import mikhail.shell.video.hosting.presentation.utils.logOut

fun EntryProviderScope<Route>.profileRoute(
    rootBackStack: MutableList<Route>,
    currentTabBackStack: MutableList<Route>,
    userDetailsProvider: UserDetailsProvider,
    player: Player
) {
    entry<Route.User.Profile> { bundle ->
        val context = LocalContext.current
        val userId = bundle.userId
        val viewModel = hiltViewModel<ProfileViewModel, ProfileViewModel.Factory> { it.create(userId) }
        val state by viewModel.state.collectAsStateWithLifecycle()
        val coroutineScope = rememberCoroutineScope()
        ProfileScreen(
            owns = userId == userDetailsProvider.getUserId(),
            state = state,
            onEvent = { event ->
                when (event) {
                    is ProfileScreenUiEvent.ClickedChannel -> currentTabBackStack.add(Route.Channel(event.channelId))
                    ProfileScreenUiEvent.CreateChannel -> currentTabBackStack.add(Route.User.CreateChannel)
                    ProfileScreenUiEvent.Invite -> {
                        context.startActivity(
                            Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, context.getString(R.string.invitation_text))
                            }
                        )
                    }
                    ProfileScreenUiEvent.OpenSettings -> currentTabBackStack.add(Route.User.Settings)
                    ProfileScreenUiEvent.PublishVideo -> currentTabBackStack.add(Route.User.UploadVideo)
                    ProfileScreenUiEvent.SignOut -> {
                        player.stop()
                        player.clearMediaItems()
                        viewModel.onEvent(event)
                        coroutineScope.launch {
                            logOut(userDetailsProvider, rootBackStack)
                        }
                    }
                    else -> viewModel.onEvent(event)
                }
            }
        )
        LaunchedEffect(state.error) {
            if (state.error == NetworkError.AUTHENTICATION) {
                rootBackStack.add(Route.Authentication)
            }
        }
    }
}