package mikhail.shell.video.hosting.presentation.navigation.video

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import mikhail.shell.video.hosting.presentation.navigation.channel.channelGraph
import mikhail.shell.video.hosting.presentation.navigation.common.Route
import mikhail.shell.video.hosting.presentation.navigation.common.defaultNavDecorators

fun EntryProviderScope<Route>.recommendationsGraph(
    rootBackStack: MutableList<Route>,
    recommendationsBackStack: MutableList<Route>
) {
    entry <Route.Recommendations> {
        NavDisplay(
            backStack = recommendationsBackStack,
            entryDecorators = defaultNavDecorators,
            entryProvider = entryProvider {
                recommendationsRoute(rootBackStack = rootBackStack)
                channelGraph(rootBackStack = rootBackStack)
            }
        )
    }
}