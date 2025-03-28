package mikhail.shell.video.hosting.presentation.utils

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector

@Composable
fun <K, V> Toggle(
    key: K,
    values: Map<K, V>,
    onValueChanged: (K) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth()
    ) {
        values.forEach { (k, v) ->
            Button(
                onClick = {
                    onValueChanged(k)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = when (key == k) {
                        true -> MaterialTheme.colorScheme.primary
                        else -> MaterialTheme.colorScheme.tertiaryContainer
                    },
                    contentColor = when (key == k) {
                        true -> MaterialTheme.colorScheme.onPrimary
                        else -> MaterialTheme.colorScheme.onTertiaryContainer
                    },
                )
            ) {
                if (v is ImageVector) {
                    Icon(
                        imageVector = v,
                        contentDescription = null
                    )
                } else if (v is String) {
                    Text(
                        text = v
                    )
                }
            }
        }
    }
}