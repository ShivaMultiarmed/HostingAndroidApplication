package mikhail.shell.video.hosting.presentation.navigation.user

import androidx.navigation3.runtime.EntryProviderScope
import mikhail.shell.video.hosting.presentation.navigation.common.Route
import mikhail.shell.video.hosting.presentation.settings.SettingsScreen

fun EntryProviderScope<Route>.settingsRoute(
    profileBackStack: MutableList<Route>
) {
    entry<Route.User.Settings> {
        SettingsScreen(
            onPopup = profileBackStack::removeLastOrNull,
            onEdit = {
                profileBackStack.add(Route.User.Edit)
            }
        )
    }
}