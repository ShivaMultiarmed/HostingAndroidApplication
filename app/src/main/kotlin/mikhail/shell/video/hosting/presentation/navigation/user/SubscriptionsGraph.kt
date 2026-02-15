package mikhail.shell.video.hosting.presentation.navigation.user

import androidx.compose.runtime.MutableState
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import mikhail.shell.video.hosting.presentation.navigation.channel.channelGraph
import mikhail.shell.video.hosting.presentation.navigation.common.Route
import mikhail.shell.video.hosting.presentation.navigation.common.SubGraphAnimations
import mikhail.shell.video.hosting.presentation.navigation.common.defaultNavDecorators

fun EntryProviderScope<Route>.subscriptionsGraph(
    rootBackStack: MutableList<Route>,
    subscriptionsBackStack: MutableList<Route>,
    currentTabBackStack: MutableState<MutableList<Route>>
) {
    entry <Route.Subscriptions> {
        NavDisplay(
            backStack = subscriptionsBackStack,
            entryDecorators = defaultNavDecorators,
            transitionSpec = { SubGraphAnimations.enteringAnimation },
            popTransitionSpec = { SubGraphAnimations.leavingAnimation },
            predictivePopTransitionSpec = { SubGraphAnimations.predictiveBackAnimation },
            entryProvider = entryProvider {
                subscriptionsRoute(
                    rootBackStack = rootBackStack,
                    subscriptionsBackStack = subscriptionsBackStack
                )
                channelGraph(
                    rootBackStack = rootBackStack,
                    currentTabBackStack = currentTabBackStack
                )
                userGraph(
                    rootBackStack = rootBackStack,
                    currentTabBackStack = currentTabBackStack
                )
            }
        )
    }
}