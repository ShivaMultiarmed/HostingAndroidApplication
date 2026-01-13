package mikhail.shell.video.hosting.presentation.navigation.video

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import mikhail.shell.video.hosting.presentation.navigation.common.Route
import mikhail.shell.video.hosting.presentation.navigation.common.SubGraphAnimations

fun EntryProviderScope<Route>.videoGraph(
    rootBackStack: MutableList<Route>,
    currentTabBackStack: MutableList<Route>
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
            transitionSpec = { SubGraphAnimations.enteringAnimation },
            popTransitionSpec = { SubGraphAnimations.leavingAnimation },
            predictivePopTransitionSpec = { SubGraphAnimations.leavingAnimation },
            entryProvider = entryProvider {
                videoRoute(
                    rootBackStack = rootBackStack,
                    currentTabBackStack = currentTabBackStack,
                    videoBackStack = videoBackStack
                )
                editVideoRoute(
                    rootBackStack = rootBackStack,
                    videoBackStack = videoBackStack
                )
            }
        )
    }
}