package mikhail.shell.video.hosting.presentation.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BorderColor
import androidx.compose.material.icons.rounded.ModeNight
import androidx.compose.material.icons.rounded.Timelapse
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import mikhail.shell.video.hosting.presentation.utils.Toggle
import mikhail.shell.video.hosting.presentation.utils.TopBar
import mikhail.shell.video.hosting.ui.theme.Theme
import mikhail.shell.video.hosting.ui.theme.VideoHostingTheme
import mikhail.shell.video.hosting.ui.theme.getThemeSelected
import mikhail.shell.video.hosting.ui.theme.setTheme

@Composable
fun SettingsScreen(
    onPopup: () -> Unit = {},
    onEdit: () -> Unit = {}
) {
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface),
        topBar = {
            TopBar(
                title = "Настройки",
                onPopup = onPopup,
                actions = listOf(
                    {
                        IconButton(
                            onClick = onEdit
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.BorderColor,
                                tint = MaterialTheme.colorScheme.onSurface,
                                contentDescription = "Вернуться назад"
                            )
                        }
                    }
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(10.dp)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                modifier = Modifier.padding(10.dp),
                text = "Тема"
            )
            var selectedTheme by remember { mutableStateOf(context.getThemeSelected()) }
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Toggle(
                    modifier = Modifier.fillMaxWidth(),
                    key = selectedTheme,
                    values = mapOf(
                        Theme.LIGHT to Icons.Rounded.WbSunny,
                        Theme.BY_TIME to Icons.Rounded.Timelapse,
                        Theme.DARK to Icons.Rounded.ModeNight
                    ),
                    onValueChanged = {
                        context.setTheme(it)
                        selectedTheme = it
                    }
                )
            }
        }
    }
}

enum class Language {
    RUSSIAN, ENGLISH
}

@Composable
@Preview
fun SettingsScreenPreview() {
    VideoHostingTheme {
        SettingsScreen()
    }
}