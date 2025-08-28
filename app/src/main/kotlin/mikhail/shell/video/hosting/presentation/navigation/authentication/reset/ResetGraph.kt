package mikhail.shell.video.hosting.presentation.navigation.authentication.reset

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.EntryProviderBuilder
import androidx.navigation3.runtime.entry
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSavedStateNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import androidx.navigation3.ui.rememberSceneSetupNavEntryDecorator
import mikhail.shell.video.hosting.presentation.navigation.common.Route

fun EntryProviderBuilder<Route>.resetGraph(
    rootBackStack: MutableList<Route>
) {
    entry<Route.Authentication.Reset> {
        val resettingBackStack = rememberSaveable {
            mutableStateListOf<Route>()
        }
        NavDisplay(
            backStack = resettingBackStack,
            entryDecorators = listOf(
                rememberSceneSetupNavEntryDecorator(),
                rememberSavedStateNavEntryDecorator(),
                rememberViewModelStoreNavEntryDecorator()
            ),
            entryProvider = entryProvider {
                requestResetRoute(resettingBackStack)
                verifyResetRoute(resettingBackStack)
                confirmResetRoute(
                    rootBackStack = rootBackStack,
                    resettingBackStack = resettingBackStack
                )
            }
        )
    }
}