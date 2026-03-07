package mikhail.shell.video.hosting.presentation.utils

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import mikhail.shell.video.hosting.R

@Composable
fun MoreButton(
    modifier: Modifier = Modifier,
    isVisible: Boolean,
    onClick: () -> Unit
) {
    IconButton(
        modifier = modifier.size(24.dp),
        onClick = onClick
    ) {
        Icon(
            imageVector = when (isVisible) {
                true -> Icons.Rounded.KeyboardArrowUp
                false -> Icons.Rounded.KeyboardArrowDown
            },
            contentDescription = stringResource(R.string.more_button)
        )
    }
}