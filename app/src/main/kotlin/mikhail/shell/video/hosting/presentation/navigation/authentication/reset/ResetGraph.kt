package mikhail.shell.video.hosting.presentation.navigation.authentication.reset

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import mikhail.shell.video.hosting.domain.providers.UserDetailsProvider
import mikhail.shell.video.hosting.presentation.navigation.common.Route

fun EntryProviderScope<Route>.resetGraph(
    rootBackStack: MutableList<Route>,
    authBackStack: SnapshotStateList<Route>,
    userDetailsProvider: UserDetailsProvider
) {
    entry<Route.Authentication.Reset> {
        val resettingBackStack = rememberSaveable {
            mutableStateListOf<Route>(Route.Authentication.Reset.Request)
        }
        NavDisplay(
            backStack = resettingBackStack,
            entryDecorators = listOf(
                rememberSaveableStateHolderNavEntryDecorator(),
                rememberViewModelStoreNavEntryDecorator()
            ),
            entryProvider = entryProvider {
                resetRequestingRoute(authBackStack, resettingBackStack)
                resetVerificationRoute(resettingBackStack)
                resetConfirmationRoute(rootBackStack,resettingBackStack, userDetailsProvider)
            }
        )
    }
}