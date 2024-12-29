package mikhail.shell.video.hosting.presentation.navigation

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
import mikhail.shell.video.hosting.presentation.profile.ProfileScreen
import mikhail.shell.video.hosting.presentation.profile.ProfileViewModel

fun NavGraphBuilder.profileRoute(
    navController: NavController,
    userDetailsProvider: UserDetailsProvider
) {
    composable<Route.Profile> {
        val userId = userDetailsProvider.getUserId()
        val viewModel = hiltViewModel<ProfileViewModel, ProfileViewModel.Factory> {
            it.create(userId)
        }
        val state by viewModel.state.collectAsStateWithLifecycle()
        val sharedPref = LocalContext.current.getSharedPreferences("auth_details", Context.MODE_PRIVATE)
        ProfileScreen(
            state = state,
            onGoToChannel = {
                navController.navigate(Route.Channel(it))
            },
            onRefresh = {
//                if (state.user == null)
//                    viewModel.loadProfile()
                if (state.channels == null)
                    viewModel.loadChannels()
            },
            onPublishVideo = {
                navController.navigate(Route.UploadVideo)
            },
            onCreateChannel = {
                navController.navigate(Route.CreateChannel)
            },
            onLogOut = {
                sharedPref.edit {
                    remove("userId")
                    remove("token")
                    commit()
                }
                navController.navigate(Route.SignIn)
            }
        )
    }
}