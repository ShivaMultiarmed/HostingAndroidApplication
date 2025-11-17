package mikhail.shell.video.hosting.presentation.navigation.video

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.media3.common.Player
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import mikhail.shell.video.hosting.domain.providers.UserDetailsProvider
import mikhail.shell.video.hosting.presentation.navigation.common.Route

fun EntryProviderScope<Route>.videoGraph(
    player: Player,
    rootBackStack: MutableList<Route>,
    currentBackStack: MutableList<Route>,
    userDetailsProvider: UserDetailsProvider
) {
    entry<Route.Video> { graph ->
        val videoBackStack = rememberSaveable {
            mutableStateListOf<Route>(Route.Video.View(graph.videoId))
        }
        NavDisplay(
            backStack = videoBackStack,
            entryDecorators = listOf(
                rememberSaveableStateHolderNavEntryDecorator(),
                rememberViewModelStoreNavEntryDecorator()
            ),
            entryProvider = entryProvider {
                videoRoute(
                    rootBackStack = rootBackStack,
                    currentTabBackStack = currentBackStack,
                    videoBackStack = videoBackStack,
                    userDetailsProvider = userDetailsProvider,
                    player = player
                )
                editVideoRoute(
                    rootBackStack = rootBackStack,
                    videoBackStack = videoBackStack
                )
            }
        )
    }
}