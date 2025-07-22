package mikhail.shell.video.hosting.presentation.utils

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import mikhail.shell.video.hosting.R
import mikhail.shell.video.hosting.ui.theme.disabled
import mikhail.shell.video.hosting.ui.theme.onDisabled

@Composable
fun PrimaryToggleButton(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(horizontal = 10.dp),
    enabled: Boolean = true,
    needsCaution: Boolean = false,
    toggled: Boolean = false,
    colors: ButtonColors = ButtonDefaults.buttonColors(
        containerColor = when {
            !toggled && !needsCaution -> MaterialTheme.colorScheme.primary
            !toggled && needsCaution -> MaterialTheme.colorScheme.error
            else -> MaterialTheme.colorScheme.tertiaryContainer
        },
        contentColor = when {
            !toggled && !needsCaution -> MaterialTheme.colorScheme.onPrimary
            !toggled && needsCaution -> MaterialTheme.colorScheme.onError
            else -> MaterialTheme.colorScheme.onTertiaryContainer
        },
        disabledContainerColor = MaterialTheme.colorScheme.disabled,
        disabledContentColor = MaterialTheme.colorScheme.onDisabled
    ),
    onClick: () -> Unit,
    toggledOffText: String? = null,
    toggledOffIcon: ImageVector? = null,
    toggledOnText: String? = toggledOffText,
    toggledOnIcon: ImageVector? = toggledOffIcon
) {
    PrimaryStandardButton(
        modifier = modifier,
        contentPadding = contentPadding,
        enabled = enabled,
        colors = colors,
        needsCaution = needsCaution,
        onClick = onClick,
        text = if (!toggled) toggledOffText else toggledOnText,
        icon = if (!toggled) toggledOffIcon else toggledOnIcon
    )
}

@Composable
fun PrimaryProgressButton(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(horizontal = 10.dp),
    enabled: Boolean = true,
    needsCaution: Boolean = false,
    inProgress: Boolean = false,
    complete: Boolean = false,
    colors: ButtonColors = ButtonDefaults.buttonColors(
        containerColor = if (!needsCaution) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
        contentColor = if (!needsCaution) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onError,
        disabledContainerColor = MaterialTheme.colorScheme.disabled,
        disabledContentColor = MaterialTheme.colorScheme.onDisabled
    ),
    onClick: () -> Unit,
    text: String? = null,
    icon: ImageVector? = null
) {
    PrimaryButton(
        modifier = modifier,
        contentPadding = contentPadding,
        enabled = enabled && !complete && !inProgress,
        needsCaution = needsCaution,
        colors = colors,
        onClick = onClick,
    ) {
        if (inProgress) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = MaterialTheme.colorScheme.onDisabled
            )
        } else {
            if (text != null) {
                Text(
                    text = text
                )
            }
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = stringResource(R.string.primary_button_hint)
                )
            }
        }
    }
}

@Composable
fun PrimaryStandardButton(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(horizontal = 10.dp),
    enabled: Boolean = true,
    needsCaution: Boolean = false,
    colors: ButtonColors = ButtonDefaults.buttonColors(
        containerColor = if (!needsCaution) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
        contentColor = if (!needsCaution) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onError,
        disabledContainerColor = MaterialTheme.colorScheme.disabled,
        disabledContentColor = MaterialTheme.colorScheme.onDisabled
    ),
    onClick: () -> Unit,
    text: String? = null,
    icon: ImageVector? = null
) {
    PrimaryButton(
        modifier = modifier,
        contentPadding = contentPadding,
        enabled = enabled,
        needsCaution = needsCaution,
        colors = colors,
        onClick = onClick
    ) {
        if (text != null) {
            Text(
                text = text
            )
        }
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = stringResource(R.string.primary_button_hint)
            )
        }
    }
}

@Composable
fun PrimaryButton(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(horizontal = 10.dp),
    enabled: Boolean = true,
    needsCaution: Boolean = false,
    colors: ButtonColors = ButtonDefaults.buttonColors(
        containerColor = if (!needsCaution) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
        contentColor = if (!needsCaution) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onError,
        disabledContainerColor = MaterialTheme.colorScheme.disabled,
        disabledContentColor = MaterialTheme.colorScheme.onDisabled
    ),
    onClick: () -> Unit,
    content: @Composable RowScope.() -> Unit
) {
    Button(
        modifier = modifier,
        contentPadding = contentPadding,
        enabled = enabled,
        colors = colors,
        onClick = onClick,
        content = content
    )
}

@Composable
@Preview
private fun PrimaryButtonPreview() {
    PrimaryProgressButton(
        onClick = {},
        text = "A button"
    )
}