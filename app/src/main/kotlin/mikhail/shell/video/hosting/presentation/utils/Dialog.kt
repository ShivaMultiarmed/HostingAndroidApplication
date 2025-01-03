package mikhail.shell.video.hosting.presentation.utils

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp

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
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.clickable { onSubmit() }
                )
            }
        },
        text = {
            if (dialogDescription != null) {
                Text(
                    text = dialogDescription,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.clickable { onSubmit() }
                )
            }
        },
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surfaceContainer),
        confirmButton = {
            Text(
                text = "Ок",
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.clickable { onSubmit() }
            )
        },
        dismissButton = {
            Text(
                text = "Отмена",
                modifier = Modifier.clickable(onClick = onDismiss)
            )
        },
        onDismissRequest = onDismiss
    )
}