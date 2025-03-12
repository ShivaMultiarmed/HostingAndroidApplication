package mikhail.shell.video.hosting.presentation.invitation

import android.Manifest
import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import kotlinx.coroutines.launch
import mikhail.shell.video.hosting.domain.models.Contact
import mikhail.shell.video.hosting.presentation.utils.SearchTopBar
import mikhail.shell.video.hosting.ui.theme.VideoHostingTheme

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun InvitationScreen(
    state: InvitationScreenState,
    onIvitationPermited: () -> Unit,
    onContactClick: (String) -> Unit,
    onSubmit: (String) -> Unit,
    onPopup: () -> Unit
) {
    val context = LocalContext.current
    ActivityCompat.requestPermissions(
        context as Activity,
        arrayOf(
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.SEND_SMS
        ),
        0
    )
    val canReadContacts = rememberPermissionState(Manifest.permission.READ_CONTACTS).status.isGranted
    val canSendSms = rememberPermissionState(Manifest.permission.SEND_SMS).status.isGranted
    if (canReadContacts && canSendSms) {
        LaunchedEffect(Unit) {
            onIvitationPermited()
        }
        var query by rememberSaveable { mutableStateOf("") }
        val snackbarHostState = remember { SnackbarHostState() }
        val coroutineScope = rememberCoroutineScope()
        Scaffold(
            topBar = {
                SearchTopBar(
                    value = query,
                    onPopup = onPopup,
                    onValueChange = { query = it },
                    onSubmit = onSubmit
                )
            },
            snackbarHost = {
                SnackbarHost(hostState = snackbarHostState)
            }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(it)
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(state.contacts) {contact ->
                        ContactComponent(
                            contact = contact,
                            onClick = {
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("Вы пригласили ${contact.name}.")
                                }
                                onContactClick(it)
                            }
                        )
                    }
                }
            }
        }
    } else {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Вы не предоставили доступ к контактам и/или запретили отправлять SMS.",
                textAlign = TextAlign.Center
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
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.tertiaryContainer)
            .padding(16.dp)
    ) {
        Text(
            text = contact.name,
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onTertiaryContainer,
            modifier = Modifier.weight(1f),
            maxLines = 1
        )
        Column(
            modifier = Modifier
                .wrapContentWidth()
                .padding(start = 16.dp),
            horizontalAlignment = Alignment.End
        ) {
            for (it in contact.phones) {
                Text(
                    text = it,
                    modifier = Modifier.clickable { onClick(it) }
                )
            }
        }
    }
}

@Composable
@Preview
fun ContactComponentPreview() {
    VideoHostingTheme {
        ContactComponent(
            contact = Contact(
                "100500",
                "Иван Петрович",
                listOf("+79135678912")
            ),
            onClick = {}
        )
    }
}