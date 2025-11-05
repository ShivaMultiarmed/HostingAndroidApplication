package mikhail.shell.video.hosting.presentation.utils

import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun ContextMenu(
    modifier: Modifier = Modifier,
    isExpanded: Boolean,
    menuItems: List<MenuItem>,
    onDismiss: () -> Unit
) {
    DropdownMenu(
        modifier = modifier,
        expanded = isExpanded,
        onDismissRequest = onDismiss
    ) {
        menuItems.forEach {
            DropdownMenuItem(
                text = {
                    Text(
                        text = it.title
                    )
                },
                onClick = {
                    it.onClick()
                    onDismiss()
                }
            )
        }
    }
}

data class MenuItem (
    val title: String,
    val onClick: () -> Unit
)