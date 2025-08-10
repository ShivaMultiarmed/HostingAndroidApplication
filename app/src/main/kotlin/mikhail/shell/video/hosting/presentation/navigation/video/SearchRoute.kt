package mikhail.shell.video.hosting.presentation.navigation.video

import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import mikhail.shell.video.hosting.presentation.navigation.common.Route
import mikhail.shell.video.hosting.presentation.video.search.SearchVideosScreen
import mikhail.shell.video.hosting.presentation.video.search.SearchVideosViewModel
import kotlin.time.Duration.Companion.milliseconds

fun NavGraphBuilder.searchRoute(navController: NavController) {
    composable<Route.Video.Search> {
        val viewModel = hiltViewModel<SearchVideosViewModel>()
        val state by viewModel.state.collectAsStateWithLifecycle()
        val coroutineScope = rememberCoroutineScope()
        SearchVideosScreen(
            state = state,
            onSubmit = viewModel::search,
            onScrollToBottom = viewModel::loadVideoPart,
            onVideoClick = {
                navController.navigate(Route.Video.View(it))
            },
            onAuthenticationRequired = {
                coroutineScope.launch {
                    delay(800.milliseconds)
                    navController.navigate(Route.Authentication)
                }
            }
        )
    }
}