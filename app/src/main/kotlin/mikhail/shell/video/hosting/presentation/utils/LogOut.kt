package mikhail.shell.video.hosting.presentation.utils

import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.navigation.NavController
import mikhail.shell.video.hosting.presentation.navigation.common.Route

fun logOut(
    sharedPref: SharedPreferences,
    navController: NavController
) {
    sharedPref.edit {
        clear()
        commit()
    }
    navController.navigate(Route.Authentication) {
        popUpTo<Route.Authentication> {
            inclusive = true
        }
    }
}