package mikhail.shell.video.hosting.presentation.navigation.user

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.core.net.toUri
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
        val viewModel =
            hiltViewModel<ProfileViewModel, ProfileViewModel.Factory> { it.create(userId) }
        val state by viewModel.state.collectAsStateWithLifecycle()
        val sharedPref = context.getSharedPreferences("user_details", Context.MODE_PRIVATE)
        val coroutineScope = rememberCoroutineScope()
        ProfileScreen(
            state = state,
            isOwner = userId == userDetailsProvider.getUserId(),
            onGoToChannel = {
                navController.navigate(Route.Channel.View(it))
            },
            onPublishVideo = {
                navController.navigate(Route.Video.Upload)
            },
            onCreateChannel = {
                navController.navigate(Route.Channel.Create)
            },
            onRefresh = {
                if (state.user == null) {
                    viewModel.loadProfile()
                }
                if (state.channels == null) {
                    viewModel.loadChannels()
                }
            },
            onLogOut = {
                player.stop()
                player.clearMediaItems()

                viewModel.signOut()
            },
            onLogOutSuccess = {
                logOut(sharedPref, navController)
            },
            onInvite = {
                context.startActivity(
                    Intent(Intent.ACTION_VIEW).apply {
                        data = "sms:".toUri()
                        putExtra("sms_body", context.getString(R.string.invitation_text))
                    }
                )
            },
            onOpenSettings = {
                navController.navigate(Route.User.Settings)
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