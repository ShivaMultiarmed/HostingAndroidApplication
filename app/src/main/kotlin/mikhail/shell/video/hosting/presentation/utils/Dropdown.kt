package mikhail.shell.video.hosting.presentation.utils

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> Dropdown(
    modifier: Modifier = Modifier,
    placeHolder: String,
    selected: T?,
    values: Map<T, String>,
    onValueChange: (T) -> Unit,
    errorMsg: String? = null,
    icon: ImageVector? = null
) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    Column {
        Box {
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = {
                    expanded = it
                }
            ) {
                InputField(
                    modifier = modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable),
                    readOnly = true,
                    value = values[selected] ?: "",
                    onValueChange = {},
                    icon = icon,
                    placeholder = placeHolder,
                    errorMsg = errorMsg
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = {
                        expanded = false
                    }
                ) {
                    values.forEach {
                        DropdownMenuItem(
                            onClick = {
                                expanded = false
                                onValueChange(it.key)
                            },
                            text = {
                                Text(
                                    text = it.value
                                )
                            }
                        )
                    }
                }
            }
            Box(
                modifier = modifier.matchParentSize().clickable {
                    expanded = true
                }
            )
        }
    }
}