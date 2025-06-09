package mikhail.shell.video.hosting.presentation.utils

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import mikhail.shell.video.hosting.R

@Composable
fun Dialog(
    onSubmit: () -> Unit,
    onDismiss: () -> Unit = {},
    dialogTitle: String? = null,
    dialogDescription: String? = null
) {
    AlertDialog(
        title = {
            if (dialogTitle != null) {
                Text(
                    text = dialogTitle,
                    color = MaterialTheme.colorScheme.error
                )
            }
        },
        text = {
            if (dialogDescription != null) {
                Text(
                    text = dialogDescription,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        },
        shape = RoundedCornerShape(10.dp),
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        confirmButton = {
            Button(
                modifier = Modifier.clip(CircleShape),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer,
                ),
                onClick = onSubmit
            ) {
                Text(
                    text = stringResource(R.string.ok_button)
                )
            }
        },
        dismissButton = {
            Button(
                modifier = Modifier.clip(CircleShape),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                ),
                onClick = onDismiss
            ) {
                Text(
                    text = stringResource(R.string.cancel_button)
                )
            }
        },
        onDismissRequest = onDismiss
    )
}