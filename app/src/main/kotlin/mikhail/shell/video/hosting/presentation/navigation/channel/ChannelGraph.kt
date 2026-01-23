package mikhail.shell.video.hosting.presentation.navigation.channel

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import mikhail.shell.video.hosting.presentation.navigation.common.Route
import mikhail.shell.video.hosting.presentation.navigation.common.SubGraphAnimations
import mikhail.shell.video.hosting.presentation.navigation.common.defaultNavDecorators

fun EntryProviderScope<Route>.channelGraph(
    rootBackStack: MutableList<Route>
) {
    entry<Route.Channel> { graph ->
        val channelBackStack = rememberSaveable {
            mutableStateListOf<Route>(Route.Channel.View(graph.channelId))
        }
        NavDisplay(
            backStack = channelBackStack,
            entryDecorators = defaultNavDecorators,
            transitionSpec = { SubGraphAnimations.enteringAnimation },
            popTransitionSpec = { SubGraphAnimations.leavingAnimation },
            predictivePopTransitionSpec = { SubGraphAnimations.leavingAnimation },
            entryProvider = entryProvider {
                channelRoute(
                    rootBackStack = rootBackStack,
                    channelBackStack = channelBackStack
                )
                channelEditingRoute(
                    rootBackStack = rootBackStack,
                    channelBackStack = channelBackStack
                )
            }
        )
    }
}