package mikhail.shell.video.hosting.presentation.utils

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import mikhail.shell.video.hosting.ui.theme.VideoHostingTheme

@Composable
fun PrimaryButton(
    modifier: Modifier = Modifier,
    text: String? = null,
    icon: ImageVector? = null,
    onClick: () -> Unit,
    isActivated: Boolean = false
) {
    Button(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 10.dp),
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (!isActivated) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.tertiaryContainer,
            contentColor = if (!isActivated) MaterialTheme.colorScheme.onPrimary
            else MaterialTheme.colorScheme.onTertiaryContainer,
        )
    ) {
        if (icon != null) {
            Icon(
                imageVector = Icons.Rounded.Search,
                tint = Color.White,
                contentDescription = "Поиск"
            )
        }
        if (text != null) {
            Text(
                text = text
            )
        }
    }
}

@Composable
@Preview
fun PrimaryButtonPreview() {
    VideoHostingTheme {
        PrimaryButton(
            text = "Кнопка",
            onClick = {},
            isActivated = false
        )
    }
}