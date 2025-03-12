package mikhail.shell.video.hosting.presentation.utils

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Send
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import mikhail.shell.video.hosting.domain.utils.isNotBlank

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(
    title: String? = null,
    onPopup: (() -> Unit)? = null,
    actions: List<@Composable () -> Unit>? = null
) {
    TopAppBar(
        modifier = Modifier
            .fillMaxWidth()
            .borderBottom(
                color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.4f),
                strokeWidth = 3
            ),
        title = {
            if (title != null) {
                Title(
                    modifier = Modifier.fillMaxWidth(),
                    text = title
                )
            }
        },
        navigationIcon = {
            if (onPopup != null) {
                IconButton(
                    onClick = onPopup
                ) {
                    Icon(
                        imageVector = Icons.Rounded.ArrowBack,
                        tint = MaterialTheme.colorScheme.onSurface,
                        contentDescription = "Вернуться назад"
                    )
                }
            }
        },
        actions = {
            actions?.forEach { it() }
        }
    )
}

@Composable
fun TopBar(
    title: String? = null,
    onPopup: (() -> Unit)? = null,
    onSubmit: (() -> Unit)? = null,
    inProgress: Boolean = false
) {
    TopBar(
        title = title,
        onPopup = onPopup,
        actions = listOf(
            {
                if (onSubmit != null) {
                    PrimaryButton(
                        onClick = onSubmit
                    ) {
                        if (!inProgress) {
                            Icon(
                                imageVector = Icons.Rounded.Send,
                                tint = MaterialTheme.colorScheme.onPrimary,
                                contentDescription = null
                            )
                        } else {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                }
            }
        )
    )
}

@Composable
fun SearchTopBar(
    value: String?,
    onPopup: (() -> Unit)? = null,
    onValueChange: (String) -> Unit,
    onSubmit: (String) -> Unit
) {
    ConstraintLayout(
        modifier = Modifier
            .fillMaxWidth()
            .borderBottom(
                color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.4f),
                strokeWidth = 3
            )
    ) {
        val popupRef = createRef()
        if (onPopup != null) {
            IconButton(
                modifier = Modifier.constrainAs(popupRef) {
                    start.linkTo(parent.start, 10.dp)
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                },
                onClick = onPopup
            ) {
                Icon(
                    imageVector = Icons.Rounded.ArrowBack,
                    tint = Color.White,
                    contentDescription = "Вернуться назад"
                )
            }
        }
        val button = createRef()
        var errorMsg by rememberSaveable { mutableStateOf<String?>(null) }
        if (value != null) {
            InputField(
                modifier = Modifier.fillMaxWidth(),
                value = value,
                onValueChange = onValueChange,
                errorMsg = errorMsg,
                placeholder = "Искать",
                icon = Icons.Rounded.Search
            )
        }
        if (value.isNotBlank()) {
            PrimaryButton(
                modifier = Modifier.constrainAs(button) {
                    end.linkTo(parent.end, 10.dp)
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                },
                icon = Icons.Rounded.Send,
                onClick = {
                    errorMsg = null
                    onSubmit(value!!)
                }
            )
        }
    }
}