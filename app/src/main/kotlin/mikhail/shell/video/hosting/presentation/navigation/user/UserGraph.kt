package mikhail.shell.video.hosting.presentation.navigation.user

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import mikhail.shell.video.hosting.presentation.navigation.channel.channelCreationRoute
import mikhail.shell.video.hosting.presentation.navigation.channel.channelGraph
import mikhail.shell.video.hosting.presentation.navigation.common.Route
import mikhail.shell.video.hosting.presentation.navigation.common.SubGraphAnimations
import mikhail.shell.video.hosting.presentation.navigation.common.defaultNavDecorators
import mikhail.shell.video.hosting.presentation.navigation.video.videoUploadingRoute

fun EntryProviderScope<Route>.userGraph(
    rootBackStack: MutableList<Route>,
    currentTabBackStack: MutableState<MutableList<Route>>,
    userBackStack: MutableList<Route>? = null
) {
    entry<Route.User> { graph ->
        val userBackStack = userBackStack ?: rememberSaveable {
            mutableStateListOf<Route>(Route.User.Profile(graph.userId))
        }
        NavDisplay(
            backStack = userBackStack,
            entryDecorators = defaultNavDecorators,
            transitionSpec = { SubGraphAnimations.enteringAnimation },
            popTransitionSpec = { SubGraphAnimations.leavingAnimation },
            predictivePopTransitionSpec = { SubGraphAnimations.predictiveBackAnimation },
            entryProvider = entryProvider {
                profileRoute(
                    rootBackStack = rootBackStack,
                    currentTabBackStack = userBackStack
                )
                channelGraph(
                    rootBackStack = rootBackStack,
                    currentTabBackStack = currentTabBackStack
                )
                settingsRoute(profileBackStack = userBackStack)
                userEditingRoute(
                    rootBackStack = rootBackStack,
                    userBackStack = userBackStack
                )
                channelCreationRoute(
                    rootBackStack = rootBackStack,
                    userBackStack = userBackStack
                )
                videoUploadingRoute(
                    rootBackStack = rootBackStack,
                    userBackStack = userBackStack
                )
            }
        )
    }
}