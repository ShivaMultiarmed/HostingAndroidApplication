package mikhail.shell.video.hosting.presentation.navigation

import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import mikhail.shell.video.hosting.presentation.invitation.InvitationScreen
import mikhail.shell.video.hosting.presentation.invitation.InvitationViewModel

fun NavGraphBuilder.inviteUserRoute(
    navController: NavController
) {
    composable<Route.Invite> {
        val viewModel = hiltViewModel<InvitationViewModel>()
        val state by viewModel.state.collectAsStateWithLifecycle()
        InvitationScreen(
            state = state,
            onContactClick = viewModel::invite,
            onIvitationPermited = { viewModel.searchContacts() },
            onSubmit = viewModel::searchContacts,
            onPopup = navController::popBackStack
        )
    }
}
