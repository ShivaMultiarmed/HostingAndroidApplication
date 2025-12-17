package mikhail.shell.video.hosting.presentation.navigation.user

import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.EntryProviderScope
import mikhail.shell.video.hosting.presentation.navigation.common.Route
import mikhail.shell.video.hosting.presentation.settings.SettingsScreen
import mikhail.shell.video.hosting.presentation.settings.SettingsScreenViewModel
import mikhail.shell.video.hosting.presentation.utils.observe
import mikhail.shell.video.hosting.presentation.settings.SettingsScreenEvent as ScreenEvent

fun EntryProviderScope<Route>.settingsRoute(
    profileBackStack: MutableList<Route>
) {
    entry<Route.User.Settings> {
        val viewModel = hiltViewModel<SettingsScreenViewModel>()
        val state by viewModel.state.collectAsStateWithLifecycle()
        val events = viewModel.events
        SettingsScreen(
            state = state,
            onAction = viewModel::onAction
        )
        events.observe { event ->
            when (event) {
                ScreenEvent.Cancelled -> profileBackStack.removeLastOrNull()
                ScreenEvent.ProfileEditingRequested -> profileBackStack.add(Route.User.Edit)
            }
        }
    }
}