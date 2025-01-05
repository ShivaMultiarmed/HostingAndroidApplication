package mikhail.shell.video.hosting.presentation.utils

import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp

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

data class MenuItem(
    val title: String,
    val onClick: () -> Unit
)