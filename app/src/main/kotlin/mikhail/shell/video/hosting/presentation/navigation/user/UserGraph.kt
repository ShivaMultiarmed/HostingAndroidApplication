package mikhail.shell.video.hosting.presentation.navigation.user

import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.media3.common.Player
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import mikhail.shell.video.hosting.domain.providers.UserDetailsProvider
import mikhail.shell.video.hosting.presentation.navigation.channel.channelCreationRoute
import mikhail.shell.video.hosting.presentation.navigation.channel.channelGraph
import mikhail.shell.video.hosting.presentation.navigation.common.Route
import mikhail.shell.video.hosting.presentation.navigation.common.SubGraphAnimations
import mikhail.shell.video.hosting.presentation.navigation.video.videoUploadingRoute

fun EntryProviderScope<Route>.userGraph(
    rootBackStack: MutableList<Route>,
    userBackStack: MutableList<Route>,
    userDetailsProvider: UserDetailsProvider,
    player: Player
) {
    entry<Route.User> { userGraph ->
        NavDisplay(
            backStack = userBackStack,
            entryDecorators = listOf(
                rememberSaveableStateHolderNavEntryDecorator(),
                rememberViewModelStoreNavEntryDecorator()
            ),
            transitionSpec = { SubGraphAnimations.enteringAnimation },
            popTransitionSpec = { SubGraphAnimations.leavingAnimation },
            predictivePopTransitionSpec = { SubGraphAnimations.leavingAnimation },
            entryProvider = entryProvider {
                profileRoute(
                    rootBackStack = rootBackStack,
                    currentTabBackStack = userBackStack,
                    userDetailsProvider = userDetailsProvider,
                    player = player
                )
                channelGraph(
                    rootBackStack = rootBackStack,
                    currentTabBackStack = userBackStack,
                    userDetailsProvider = userDetailsProvider
                )
                if (userDetailsProvider.getUserId() == userGraph.userId) {
                    settingsRoute(
                        profileBackStack = userBackStack
                    )
                    userEditingRoute(
                        rootBackStack = rootBackStack,
                        userBackStack = userBackStack,
                        userDetailsProvider = userDetailsProvider,
                        player = player
                    )
                    channelCreationRoute(
                        rootBackStack = rootBackStack,
                        userBackStack = userBackStack,
                        userDetailsProvider = userDetailsProvider
                    )
                    videoUploadingRoute(
                        rootBackStack = rootBackStack,
                        userBackStack = userBackStack,
                        userDetailsProvider = userDetailsProvider
                    )
                }
            }
        )
    }
}