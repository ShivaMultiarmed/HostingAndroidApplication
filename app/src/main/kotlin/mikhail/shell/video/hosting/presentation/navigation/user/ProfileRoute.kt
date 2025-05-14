package mikhail.shell.video.hosting.presentation.navigation.user

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import mikhail.shell.video.hosting.domain.providers.UserDetailsProvider
import mikhail.shell.video.hosting.presentation.navigation.Route
import mikhail.shell.video.hosting.presentation.user.screen.ProfileScreen
import mikhail.shell.video.hosting.presentation.user.screen.ProfileViewModel
import mikhail.shell.video.hosting.presentation.utils.logOut

fun NavGraphBuilder.profileRoute(
    navController: NavController,
    userDetailsProvider: UserDetailsProvider
) {
    composable<Route.User.Profile> {
        val bundle = it.toRoute<Route.User.Profile>()
        val userId = bundle.userId
        val viewModel = hiltViewModel<ProfileViewModel, ProfileViewModel.Factory> { it.create(userId) }
        val state by viewModel.state.collectAsStateWithLifecycle()
        val sharedPref = LocalContext.current.getSharedPreferences("user_details", Context.MODE_PRIVATE)
        ProfileScreen(
            state = state,
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
                if (state.channels == null) {
                    viewModel.loadChannels()
                }
            },
            onLogOut = {
                logOut(sharedPref, navController)
            },
            onInvite = {
                navController.navigate(Route.User.Invite)
            },
            onOpenSettings = {
                navController.navigate(Route.User.Settings)
            },
            isOwner = userId == userDetailsProvider.getUserId()
        )
    }
}