package mikhail.shell.video.hosting.presentation.navigation.channel

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.EntryProviderBuilder
import androidx.navigation3.runtime.entry
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSavedStateNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import androidx.navigation3.ui.rememberSceneSetupNavEntryDecorator
import mikhail.shell.video.hosting.domain.providers.UserDetailsProvider
import mikhail.shell.video.hosting.presentation.navigation.common.Route

fun EntryProviderBuilder<Route>.channelGraph(
    rootBackStack: MutableList<Route>,
    currentTabBackStack: MutableList<Route>,
    userDetailsProvider: UserDetailsProvider
) {
    entry<Route.Channel> {
        val channelBackStack = rememberSaveable {
            mutableStateListOf<Route>(Route.Channel.View(it.channelId))
        }
        NavDisplay(
            backStack = channelBackStack,
            entryDecorators = listOf(
                rememberSceneSetupNavEntryDecorator(),
                rememberSavedStateNavEntryDecorator(),
                rememberViewModelStoreNavEntryDecorator()
            ),
            entryProvider = entryProvider {
                channelRoute(
                    rootBackStack = rootBackStack,
                    channelBackStack = channelBackStack,
                    userDetailsProvider = userDetailsProvider
                )
                editChannelRoute(
                    rootBackStack = rootBackStack,
                    channelBackStack = channelBackStack
                )
            }
        )
    }
}