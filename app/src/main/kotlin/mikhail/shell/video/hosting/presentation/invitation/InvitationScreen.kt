package mikhail.shell.video.hosting.presentation.invitation

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import mikhail.shell.video.hosting.domain.models.Contact

@Composable
fun InvitationScreen(
    state: InvitationScreenState,
    onContactClick: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {
        items(state.contacts) {
            ContactComponent(
                contact = it,
                onClick = onContactClick
            )
        }
    }
}

@Composable
fun ContactComponent(
    contact: Contact,
    onClick: (String) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = contact.name
        )
    }
}