package mikhail.shell.video.hosting.presentation.navigation.channel

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import mikhail.shell.video.hosting.domain.providers.UserDetailsProvider
import mikhail.shell.video.hosting.presentation.navigation.common.Route

fun EntryProviderScope<Route>.channelGraph(
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
                rememberSaveableStateHolderNavEntryDecorator(),
                rememberViewModelStoreNavEntryDecorator()
            ),
            entryProvider = entryProvider {
                channelRoute(
                    rootBackStack = rootBackStack,
                    channelBackStack = channelBackStack,
                    userDetailsProvider = userDetailsProvider
                )
                channelEditingRoute(
                    rootBackStack = rootBackStack,
                    channelBackStack = channelBackStack
                )
            }
        )
    }
}