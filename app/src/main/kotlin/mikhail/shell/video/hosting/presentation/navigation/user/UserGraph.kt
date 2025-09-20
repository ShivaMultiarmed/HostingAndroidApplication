package mikhail.shell.video.hosting.presentation.navigation.user

import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.media3.common.Player
import androidx.navigation3.runtime.EntryProviderBuilder
import androidx.navigation3.runtime.entry
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSavedStateNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import androidx.navigation3.ui.rememberSceneSetupNavEntryDecorator
import mikhail.shell.video.hosting.domain.providers.UserDetailsProvider
import mikhail.shell.video.hosting.presentation.navigation.channel.channelGraph
import mikhail.shell.video.hosting.presentation.navigation.channel.createChannelRoute
import mikhail.shell.video.hosting.presentation.navigation.common.Route
import mikhail.shell.video.hosting.presentation.navigation.video.uploadVideoRoute

fun EntryProviderBuilder<Route>.userGraph(
    rootBackStack: MutableList<Route>,
    userBackStack: MutableList<Route>,
    userDetailsProvider: UserDetailsProvider,
    player: Player
) {
    entry<Route.User> {
        NavDisplay(
            backStack = userBackStack,
            entryDecorators = listOf(
                rememberSceneSetupNavEntryDecorator(),
                rememberSavedStateNavEntryDecorator(),
                rememberViewModelStoreNavEntryDecorator()
            ),
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
                editUserRoute(
                    rootBackStack = rootBackStack,
                    userBackStack = userBackStack,
                    userDetailsProvider = userDetailsProvider,
                    player = player
                )
                uploadVideoRoute(
                    rootBackStack = rootBackStack,
                    userBackStack = userBackStack,
                    userDetailsProvider = userDetailsProvider
                )
                createChannelRoute(
                    rootBackStack = rootBackStack,
                    userBackStack = userBackStack
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