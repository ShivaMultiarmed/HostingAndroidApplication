package mikhail.shell.video.hosting.presentation.utils

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

@Composable
fun <K, V> Toggle(
    modifier: Modifier = Modifier,
    key: K,
    values: Map<K, V>,
    onValueChanged: (K) -> Unit
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally)
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