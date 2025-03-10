package mikhail.shell.video.hosting.presentation.invitation

import mikhail.shell.video.hosting.domain.models.Contact

data class InvitationScreenState(
    val isLoading: Boolean = false,
    val contacts: List<Contact> = emptyList()
)
