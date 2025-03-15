package mikhail.shell.video.hosting.presentation.navigation.channel

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.navigation
import mikhail.shell.video.hosting.domain.providers.UserDetailsProvider
import mikhail.shell.video.hosting.presentation.navigation.Route

fun NavGraphBuilder.channelGraph(
    navController: NavController,
    userDetailsProvider: UserDetailsProvider
) {
    navigation<Route.Channel>(
        startDestination = Route.Channel.Create
    ) {
        channelRoute(navController, userDetailsProvider)
        channelRoute(navController, userDetailsProvider)
        editChannelRoute(navController)
    }
}