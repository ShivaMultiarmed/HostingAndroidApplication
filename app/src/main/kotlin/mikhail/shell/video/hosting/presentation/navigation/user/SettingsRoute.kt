package mikhail.shell.video.hosting.presentation.navigation.user

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import mikhail.shell.video.hosting.presentation.navigation.Route
import mikhail.shell.video.hosting.presentation.settings.SettingsScreen

fun NavGraphBuilder.settingsRoute(
    navController: NavController
) {
    composable<Route.User.Settings> {
        SettingsScreen(
            onPopup = {
                navController.popBackStack()
            }
        )
    }
}