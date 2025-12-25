package mikhail.shell.video.hosting.presentation.navigation.user

import android.content.Intent
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.Player
import androidx.navigation3.runtime.EntryProviderScope
import mikhail.shell.video.hosting.R
import mikhail.shell.video.hosting.domain.errors.network.NetworkError
import mikhail.shell.video.hosting.domain.validation.getStandardErrorMessage
import mikhail.shell.video.hosting.presentation.navigation.common.Route
import mikhail.shell.video.hosting.presentation.user.screen.ProfileScreen
import mikhail.shell.video.hosting.presentation.user.screen.ProfileViewModel
import mikhail.shell.video.hosting.presentation.utils.observe
import mikhail.shell.video.hosting.presentation.user.screen.ProfileScreenEvent as ScreenEvent

fun EntryProviderScope<Route>.profileRoute(
    rootBackStack: MutableList<Route>,
    currentTabBackStack: MutableList<Route>
) {
    entry<Route.User.Profile> { route ->
        val context = LocalContext.current
        val userId = route.userId
        val viewModel = hiltViewModel<ProfileViewModel, ProfileViewModel.Factory> { factory ->
            factory.create(userId)
        }
        val state by viewModel.state.collectAsStateWithLifecycle()
        val events = viewModel.events
        val snackBarHostState = remember { SnackbarHostState() }
        ProfileScreen(
            owns = state.user?.userId == state.signedInUserId,
            state = state,
            onAction = viewModel::onAction,
            snackBarHostState = snackBarHostState
        )
        events.observe { event ->
            when (event) {
                is ScreenEvent.Failure -> {
                    if (
                        event.error !in setOf(
                            NetworkError.AUTHENTICATION,
                            NetworkError.NOT_FOUND
                        )
                    ) {
                        context.getStandardErrorMessage(event.error)?.let {
                            snackBarHostState.showSnackbar(it)
                        }
                    } else {
                        rootBackStack.add(Route.Authentication)
                    }
                }
                is ScreenEvent.ChannelChosen -> currentTabBackStack.add(Route.Channel(event.channelId))
                ScreenEvent.ChannelCreationRequested -> currentTabBackStack.add(Route.User.ChannelCreation)
                ScreenEvent.InvitationRequested -> context.startActivity(
                    Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, context.getString(R.string.invitation_text))
                    }
                )
                ScreenEvent.SettingsRequested -> currentTabBackStack.add(Route.User.Settings)
                ScreenEvent.VideoUploadingRequested -> currentTabBackStack.add(Route.User.VideoUploading)
                ScreenEvent.SignedOut -> {
                    rootBackStack.clear()
                    rootBackStack.add(Route.Authentication)
                }
            }
        }
    }
}