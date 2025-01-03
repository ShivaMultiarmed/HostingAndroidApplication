package mikhail.shell.video.hosting.presentation.utils

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.TurnLeft
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ActionButton(
    icon: ImageVector? = null,
    text: String? = null,
    onClick: () -> Unit
) {
    Button(
        contentPadding = PaddingValues(horizontal = 10.dp),
        onClick = onClick,
        modifier = Modifier.height(28.dp),
        colors = ButtonDefaults.buttonColors(
            contentColor = MaterialTheme.colorScheme.onSurface,
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        shape = CircleShape
    ) {
        Row(
            modifier = Modifier,
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            icon?.let {
                Icon(
                    imageVector = it,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(14.dp)
                )
            }
            text?.let {
                Text(
                    text = text,
                    fontSize = 12.sp
                )
            }
        }
    }
}
@Composable
fun RemoveButton(
    onClick: () -> Unit
) {
    ActionButton (
        icon = Icons.Rounded.Close,
        onClick = onClick
    )
}
@Composable
@Preview
fun RevertButton(
    onClick: () -> Unit = {}
) {
    ActionButton(
        icon = Icons.Rounded.TurnLeft,
        onClick = onClick
    )
}