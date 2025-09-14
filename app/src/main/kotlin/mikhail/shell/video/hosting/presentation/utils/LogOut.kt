package mikhail.shell.video.hosting.presentation.utils

import mikhail.shell.video.hosting.domain.providers.UserDetailsProvider
import mikhail.shell.video.hosting.presentation.navigation.common.Route

suspend fun logOut(
    userDetailsProvider: UserDetailsProvider,
    rootBackStack: MutableList<Route>
) {
    userDetailsProvider.remove()
    rootBackStack.clear()
    rootBackStack.add(Route.Authentication)
}