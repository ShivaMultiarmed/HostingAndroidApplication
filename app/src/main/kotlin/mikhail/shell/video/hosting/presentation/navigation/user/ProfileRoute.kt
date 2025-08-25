package mikhail.shell.video.hosting.presentation.navigation.user

import android.content.Intent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.Player
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import mikhail.shell.video.hosting.R
import mikhail.shell.video.hosting.domain.providers.UserDetailsProvider
import mikhail.shell.video.hosting.presentation.navigation.common.Route
import mikhail.shell.video.hosting.presentation.user.screen.ProfileScreen
import mikhail.shell.video.hosting.presentation.user.screen.ProfileScreenUiEvent
import mikhail.shell.video.hosting.presentation.user.screen.ProfileViewModel
import mikhail.shell.video.hosting.presentation.utils.logOut
import kotlin.time.Duration.Companion.milliseconds

fun NavGraphBuilder.profileRoute(
    navController: NavController,
    userDetailsProvider: UserDetailsProvider,
    player: Player
) {
    composable<Route.User.Profile> {
        val context = LocalContext.current
        val bundle = it.toRoute<Route.User.Profile>()
        val userId = bundle.userId
        val viewModel = hiltViewModel<ProfileViewModel, ProfileViewModel.Factory> { it.create(userId) }
        val state by viewModel.state.collectAsStateWithLifecycle()
        val coroutineScope = rememberCoroutineScope()
        ProfileScreen(
            owns = userId == userDetailsProvider.getUserId(),
            state = state,
            onEvent = {
                when (it) {
                    is ProfileScreenUiEvent.ClickedChannel -> navController.navigate(Route.Channel.View(it.channelId))
                    ProfileScreenUiEvent.CreateChannel -> navController.navigate(Route.Channel.Create)
                    ProfileScreenUiEvent.Invite -> {
                        context.startActivity(
                            Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, context.getString(R.string.invitation_text))
                            }
                        )
                    }
                    ProfileScreenUiEvent.OpenSettings -> navController.navigate(Route.User.Settings)
                    ProfileScreenUiEvent.PublishVideo -> navController.navigate(Route.Video.Upload)
                    ProfileScreenUiEvent.SignOut -> {
                        player.stop()
                        player.clearMediaItems()

                        viewModel.onEvent(it)

                        logOut(userDetailsProvider, navController)
                    }
                    else -> viewModel.onEvent(it)
                }
            },
            onGoToChannel = {

            },
            onPublishVideo = {

            },
            onAuthenticationRequired = {
                coroutineScope.launch {
                    delay(800.milliseconds)
                    navController.navigate(Route.Authentication.SignIn)
                }
            },
            onUserNotFound = {
                coroutineScope.launch {
                    delay(800.milliseconds)
                    navController.popBackStack()
                }
            }
        )
    }
}