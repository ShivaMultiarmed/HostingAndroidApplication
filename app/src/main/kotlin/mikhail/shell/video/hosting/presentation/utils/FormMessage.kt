package mikhail.shell.video.hosting.presentation.utils

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun FormMessage(text: String) {
    Text(
        text = text,
        color = MaterialTheme.colorScheme.primary
    )
}

@Composable
fun ErrorMessage(text: String) {
    Text(
        text = text,
        color = MaterialTheme.colorScheme.error
    )
}