package mikhail.shell.video.hosting.presentation.navigation.video

import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import mikhail.shell.video.hosting.presentation.navigation.channel.channelGraph
import mikhail.shell.video.hosting.presentation.navigation.common.Route

fun EntryProviderScope<Route>.searchGraph(
    rootBackStack: MutableList<Route>,
    searchBackStack: MutableList<Route>
) {
    entry <Route.Search> {
        NavDisplay(
            backStack = searchBackStack,
            entryDecorators = listOf(
                rememberSaveableStateHolderNavEntryDecorator(),
                rememberViewModelStoreNavEntryDecorator()
            ),
            entryProvider = entryProvider {
                searchRoute(rootBackStack = rootBackStack)
                channelGraph(rootBackStack = rootBackStack)
            }
        )
    }
}