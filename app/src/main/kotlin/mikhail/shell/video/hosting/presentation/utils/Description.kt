package mikhail.shell.video.hosting.presentation.utils

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp

@Composable
fun Description(
    modifier: Modifier = Modifier,
    isVisible: Boolean,
    description: String
) {
    AnimatedVisibility(
        modifier = modifier,
        visible = isVisible,
        enter = expandVertically(
            tween(durationMillis = 300)
        ),
        exit = shrinkVertically(
            tween(durationMillis = 300)
        )
    ) {
        Text(
            modifier = modifier.fillMaxWidth(),
            text = description,
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}