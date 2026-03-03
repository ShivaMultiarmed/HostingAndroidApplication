package mikhail.shell.video.hosting.presentation.utils

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import mikhail.shell.video.hosting.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(
    title: String,
    onPopup: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {}
) {
    TopAppBar(
        modifier = Modifier
            .fillMaxWidth()
            .borderBottom(
                color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.4f),
                strokeWidth = 3
            ),
        title = {
            Title(
                modifier = Modifier.fillMaxWidth(),
                text = title
            )
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
            actionIconContentColor = MaterialTheme.colorScheme.onSurface
        ),
        navigationIcon = {
            if (onPopup != null) {
                IconButton(
                    onClick = onPopup
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                        tint = MaterialTheme.colorScheme.onSurface,
                        contentDescription = stringResource(R.string.go_back_button)
                    )
                }
            }
        },
        actions = actions
    )
}

@Composable
fun TopBar(
    title: String,
    onPopup: (() -> Unit)? = null,
    onSubmit: (() -> Unit)? = null,
    inProgress: Boolean = false,
    complete: Boolean = false
) {
    TopBar(
        title = title,
        onPopup = onPopup,
        actions = {
            if (onSubmit != null) {
                PrimaryProgressButton(
                    inProgress = inProgress,
                    complete = complete,
                    onClick = onSubmit,
                    icon = Icons.AutoMirrored.Rounded.Send
                )
            }
        }
    )
}