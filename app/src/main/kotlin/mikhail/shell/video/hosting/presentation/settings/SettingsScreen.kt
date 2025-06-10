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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import mikhail.shell.video.hosting.R
import mikhail.shell.video.hosting.presentation.utils.Toggle
import mikhail.shell.video.hosting.presentation.utils.TopBar
import mikhail.shell.video.hosting.ui.theme.Theme
import mikhail.shell.video.hosting.ui.theme.VideoHostingTheme
import mikhail.shell.video.hosting.ui.theme.getLocale
import mikhail.shell.video.hosting.ui.theme.getThemeSelected
import mikhail.shell.video.hosting.ui.theme.setLocale
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
                title = stringResource(R.string.settings_title),
                onPopup = onPopup,
                actions = listOf(
                    {
                        IconButton(
                            onClick = onEdit
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.BorderColor,
                                tint = MaterialTheme.colorScheme.onSurface,
                                contentDescription = stringResource(R.string.go_back_button)
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
                text = stringResource(R.string.theme_title)
            )
            var selectedTheme by rememberSaveable { mutableStateOf(context.getThemeSelected()) }
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
            Text(
                modifier = Modifier.padding(10.dp),
                text = stringResource(R.string.language_title)
            )
            var selectedLocale by rememberSaveable { mutableStateOf(context.getLocale()) }
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Toggle(
                    modifier = Modifier.fillMaxWidth(),
                    key = selectedLocale,
                    values = mapOf(
                        Locale.RUSSIAN to Locale.RUSSIAN.label,
                        Locale.ENGLISH to Locale.ENGLISH.label
                    ),
                    onValueChanged = {
                        context.setLocale(it)
                        selectedLocale = it
                    }
                )
            }
        }
    }
}

enum class Locale(val label: String, val iso: String) {
    RUSSIAN("Русский", "ru"),
    ENGLISH("English", "en");

    companion object {
        fun ofTag(tag: String): Locale {
            return Locale.entries.find { it.iso == tag }?: throw IllegalArgumentException("Invalid locale tag")
        }
    }
}

@Composable
@Preview
fun SettingsScreenPreview() {
    VideoHostingTheme {
        SettingsScreen()
    }
}