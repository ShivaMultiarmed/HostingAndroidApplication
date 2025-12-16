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
    entry<Route.User> {
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
                settingsRoute(
                    profileBackStack = userBackStack
                )
                profileRoute(
                    rootBackStack = rootBackStack,
                    currentTabBackStack = userBackStack,
                    userDetailsProvider = userDetailsProvider,
                    player = player
                )
                userEditingRoute(
                    rootBackStack = rootBackStack,
                    userBackStack = userBackStack,
                    userDetailsProvider = userDetailsProvider,
                    player = player
                )
                videoUploadingRoute(
                    rootBackStack = rootBackStack,
                    userBackStack = userBackStack,
                    userDetailsProvider = userDetailsProvider
                )
                channelCreationRoute(
                    rootBackStack = rootBackStack,
                    userBackStack = userBackStack,
                    userDetailsProvider = userDetailsProvider
                )
                channelGraph(
                    rootBackStack = rootBackStack,
                    currentTabBackStack = userBackStack,
                    userDetailsProvider = userDetailsProvider
                )
            }
        )
    }
}