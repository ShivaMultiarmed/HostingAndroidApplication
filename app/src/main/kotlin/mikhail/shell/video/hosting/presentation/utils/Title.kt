package mikhail.shell.video.hosting.presentation.utils

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp

@Composable
fun Title(text: String) {
    Text(
        modifier = Modifier,
        text = text,
        color = MaterialTheme.colorScheme.onSurface,
        fontSize = 22.sp,
        textAlign = TextAlign.Center
    )
}