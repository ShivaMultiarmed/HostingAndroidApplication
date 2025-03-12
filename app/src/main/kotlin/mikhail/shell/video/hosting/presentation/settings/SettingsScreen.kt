package mikhail.shell.video.hosting.presentation.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ModeNight
import androidx.compose.material.icons.rounded.Timelapse
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import mikhail.shell.video.hosting.presentation.utils.Toggle
import mikhail.shell.video.hosting.presentation.utils.TopBar
import mikhail.shell.video.hosting.ui.theme.Theme
import mikhail.shell.video.hosting.ui.theme.VideoHostingTheme
import mikhail.shell.video.hosting.ui.theme.getThemeSelected
import mikhail.shell.video.hosting.ui.theme.setTheme

@Composable
fun SettingsScreen() {
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .verticalScroll(scrollState)
    ) {
        TopBar(
            title = "Настройки"
        )
        Text(
            text = "Язык"
        )
        var selectedLanguage by remember { mutableStateOf(Language.RUSSIAN) }
        val languages = mapOf(
            Language.RUSSIAN to "Русский",
            Language.ENGLISH to "English"
        )
        Box (
            modifier = Modifier.fillMaxWidth()
        ) {
            Toggle(
                key = selectedLanguage,
                values = languages,
                onValueChanged = {
                    selectedLanguage = it
                }
            )
        }
        Text(
            text = "Тема"
        )
        var selectedTheme by remember { mutableStateOf(context.getThemeSelected()) }
        Box (
            modifier = Modifier.fillMaxWidth()
        ) {
            Toggle(
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