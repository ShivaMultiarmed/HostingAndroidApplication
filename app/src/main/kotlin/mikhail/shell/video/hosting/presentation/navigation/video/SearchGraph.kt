package mikhail.shell.video.hosting.presentation.navigation.video

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import mikhail.shell.video.hosting.presentation.navigation.channel.channelGraph
import mikhail.shell.video.hosting.presentation.navigation.common.Route
import mikhail.shell.video.hosting.presentation.navigation.common.defaultNavDecorators

fun EntryProviderScope<Route>.searchGraph(
    rootBackStack: MutableList<Route>,
    searchBackStack: MutableList<Route>
) {
    entry <Route.Search> {
        NavDisplay(
            backStack = searchBackStack,
            entryDecorators = defaultNavDecorators,
            entryProvider = entryProvider {
                searchRoute(rootBackStack = rootBackStack)
                channelGraph(rootBackStack = rootBackStack)
            }
        )
    }
}