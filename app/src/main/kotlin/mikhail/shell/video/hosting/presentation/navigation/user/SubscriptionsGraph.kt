package mikhail.shell.video.hosting.presentation.navigation.user

import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import mikhail.shell.video.hosting.domain.providers.UserDetailsProvider
import mikhail.shell.video.hosting.presentation.navigation.channel.channelGraph
import mikhail.shell.video.hosting.presentation.navigation.common.Route

fun EntryProviderScope<Route>.subscriptionsGraph(
    rootBackStack: MutableList<Route>,
    subscriptionsBackStack: MutableList<Route>,
    userDetailsProvider: UserDetailsProvider
) {
    entry <Route.Subscriptions> {
        NavDisplay(
            backStack = subscriptionsBackStack,
            entryDecorators = listOf(
                rememberSaveableStateHolderNavEntryDecorator(),
                rememberViewModelStoreNavEntryDecorator()
            ),
            entryProvider = entryProvider {
                subscriptionsRoute(
                    rootBackStack = rootBackStack,
                    subscriptionsBackStack = subscriptionsBackStack
                )
                channelGraph(
                    rootBackStack = rootBackStack,
                    currentTabBackStack = subscriptionsBackStack,
                    userDetailsProvider = userDetailsProvider
                )
            }
        )
    }
}