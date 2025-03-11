package mikhail.shell.video.hosting.presentation.navigation.user

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.edit
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import mikhail.shell.video.hosting.domain.providers.UserDetailsProvider
import mikhail.shell.video.hosting.presentation.navigation.Route
import mikhail.shell.video.hosting.presentation.profile.ProfileScreen
import mikhail.shell.video.hosting.presentation.profile.ProfileViewModel

fun NavGraphBuilder.profileRoute(
    navController: NavController,
    userDetailsProvider: UserDetailsProvider
) {
    composable<Route.User.Profile> {
        val userId = userDetailsProvider.getUserId()
        val viewModel = hiltViewModel<ProfileViewModel, ProfileViewModel.Factory> {
            it.create(userId)
        }
        val state by viewModel.state.collectAsStateWithLifecycle()
        val sharedPref = LocalContext.current.getSharedPreferences("user_details", Context.MODE_PRIVATE)
        ProfileScreen(
            state = state,
            onGoToChannel = {
                navController.navigate(Route.Channel(it))
            },
            onPublishVideo = {
                navController.navigate(Route.User.UploadVideo)
            },
            onCreateChannel = {
                navController.navigate(Route.User.CreateChannel)
            },
            onRefresh = {
//                if (state.user == null)
//                    viewModel.loadProfile()
                if (state.channels == null)
                    viewModel.loadChannels()
            },
            onLogOut = {
                sharedPref.edit {
                    clear()
                    commit()
                }
                navController.navigate(Route.Authentication)
            },
            onInvite = {
                navController.navigate(Route.Invite)
            }
        )
    }
}