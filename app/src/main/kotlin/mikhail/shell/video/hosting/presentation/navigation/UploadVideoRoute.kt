package mikhail.shell.video.hosting.presentation.navigation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import mikhail.shell.video.hosting.domain.providers.UserDetailsProvider
import mikhail.shell.video.hosting.presentation.video.upload.UploadVideoScreen
import mikhail.shell.video.hosting.presentation.video.upload.UploadVideoViewModel

fun NavGraphBuilder.uploadVideoRoute(
    navController: NavController,
    userDetailsProvider: UserDetailsProvider
) {
    composable<Route.UploadVideo> {
        val userId = userDetailsProvider.getUserId()
        val viewModel =
            hiltViewModel<UploadVideoViewModel, UploadVideoViewModel.Factory>() {
                it.create(userId)
            }
        val state by viewModel.state.collectAsStateWithLifecycle()
        val coroutineScope = rememberCoroutineScope()
        UploadVideoScreen(
            state = state,
            onSubmit = {
                viewModel.uploadVideo(it)
            },
            onSuccess = {
                coroutineScope.launch {
                    delay(1000)
                    navController.navigate(Route.Video(it.videoId!!))
                }
            },
            onRefresh = {
                viewModel.loadChannels()
            }
        )
    }
}