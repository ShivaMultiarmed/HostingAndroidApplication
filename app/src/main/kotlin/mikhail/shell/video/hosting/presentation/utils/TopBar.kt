package mikhail.shell.video.hosting.presentation.utils

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBackIosNew
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun TopBar(
    topBarTitle: String,
    onPopup: () -> Unit,
    canPopUp: Boolean = true,
    buttonTitle: String? = null,
    onSubmit: (() -> Unit)? = null,
    inProccess: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth()
            .borderBottom(color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.4f), strokeWidth = 3)
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        if (canPopUp) {
            Icon(
                imageVector = Icons.Rounded.ArrowBackIosNew,
                contentDescription = "Вернуться назад",
                modifier = Modifier.clickable(onClick = onPopup)
            )
        }
        Title(
            text = topBarTitle,
        )
        if (onSubmit != null) {
            if (!inProccess) {
                PrimaryButton(
                    modifier = Modifier.padding(start = 10.dp),
                    text = buttonTitle,
                    onClick = onSubmit
                )
            } else {
                PrimaryButton(
                    modifier = Modifier.padding(start = 10.dp),
                    onClick = {},
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
    }
}