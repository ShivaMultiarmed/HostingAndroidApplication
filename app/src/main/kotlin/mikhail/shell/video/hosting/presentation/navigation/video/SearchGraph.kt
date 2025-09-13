package mikhail.shell.video.hosting.presentation.navigation.video

import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.EntryProviderBuilder
import androidx.navigation3.runtime.entry
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSavedStateNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import androidx.navigation3.ui.rememberSceneSetupNavEntryDecorator
import mikhail.shell.video.hosting.domain.providers.UserDetailsProvider
import mikhail.shell.video.hosting.presentation.navigation.channel.channelGraph
import mikhail.shell.video.hosting.presentation.navigation.common.Route

fun EntryProviderBuilder<Route>.searchGraph(
    rootBackStack: MutableList<Route>,
    searchBackStack: MutableList<Route>,
    userDetailsProvider: UserDetailsProvider
) {
    entry <Route.Search> {
        NavDisplay(
            backStack = searchBackStack,
            entryDecorators = listOf(
                rememberSceneSetupNavEntryDecorator(),
                rememberSavedStateNavEntryDecorator(),
                rememberViewModelStoreNavEntryDecorator()
            ),
            entryProvider = entryProvider {
                searchRoute(rootBackStack)
                channelGraph(
                    rootBackStack = rootBackStack,
                    currentTabBackStack = searchBackStack,
                    userDetailsProvider = userDetailsProvider
                )
            }
        )
    }
}