package mikhail.shell.video.hosting.presentation.navigation.video

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.scene.Scene
import androidx.navigation3.scene.SceneStrategy
import androidx.navigation3.scene.SceneStrategyScope
import androidx.navigation3.ui.NavDisplay
import mikhail.shell.video.hosting.presentation.navigation.common.Route
import mikhail.shell.video.hosting.presentation.navigation.common.SubGraphAnimations
import mikhail.shell.video.hosting.presentation.navigation.common.defaultNavDecorators

fun EntryProviderScope<Route>.videoGraph(
    rootBackStack: MutableList<Route>,
    currentTabBackStack: State<MutableList<Route>>
) {
    entry<Route.Video> { graph ->
        val videoBackStack = rememberSaveable {
            mutableStateListOf<Route>(Route.Video.View(graph.videoId))
        }
        NavDisplay(
            backStack = videoBackStack,
            entryDecorators = defaultNavDecorators,
            transitionSpec = { SubGraphAnimations.enteringAnimation },
            popTransitionSpec = { SubGraphAnimations.leavingAnimation },
            predictivePopTransitionSpec = { SubGraphAnimations.predictiveBackAnimation },
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

class PredictiveFadeOutScene (
    backStack: List<NavEntry<Route>>
) : Scene<Route> {
    override val key: Any = backStack.last().contentKey
    override val entries: List<NavEntry<Route>> = backStack.takeLast(2)
    override val previousEntries: List<NavEntry<Route>> = backStack.dropLast(2)
    override val content: @Composable (() -> Unit) = {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            entries.forEach { entry ->
                entry.Content()
            }
        }
    }
}

class PredictiveFadeOutSceneStrategy(): SceneStrategy<Route> {
    override fun SceneStrategyScope<Route>.calculateScene(
        entries: List<NavEntry<Route>>
    ): Scene<Route>? {
        if (!(entries.last().contentKey as String).contains("Video")) {
            return null
        }
        return PredictiveFadeOutScene(entries)
    }
}