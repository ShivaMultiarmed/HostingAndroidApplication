package mikhail.shell.video.hosting.ui.theme

import android.app.LocaleManager
import android.content.Context
import android.os.Build
import android.os.LocaleList
import androidx.activity.compose.LocalActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.os.LocaleListCompat
import androidx.core.view.WindowCompat
import androidx.datastore.core.Serializer
import androidx.datastore.dataStore
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import mikhail.shell.video.hosting.presentation.settings.Locale
import mikhail.shell.video.hosting.ui.theme.Theme.DARK
import mikhail.shell.video.hosting.ui.theme.Theme.LIGHT
import mikhail.shell.video.hosting.ui.theme.Theme.SYSTEM
import java.io.InputStream
import java.io.OutputStream

val DarkColorScheme = darkColorScheme(
    primary = Blue500,
    onPrimary = White,

    secondary = Blue900,
    onSecondary = White,

    secondaryContainer = Gray,
    onSecondaryContainer = White,

    tertiary = LighterGray,
    onTertiary = Black,

    tertiaryContainer = Gray,
    onTertiaryContainer = LighterGray,

    background = Black,
    onBackground = White,

    surface = Black,
    surfaceContainer = Black,
    onSurface = White,
    onSurfaceVariant = White,

    error = Red,
    onError = White
)

val LightColorScheme = lightColorScheme(
    primary = Blue700,
    onPrimary = White,

    secondary = Blue50,
    onSecondary = Black,

    secondaryContainer = White,
    onSecondaryContainer = Black,

    tertiary = Gray,
    onTertiary = Black,

    tertiaryContainer = LightGray,
    onTertiaryContainer = Black,

    background = White,
    onBackground = Black,

    surface = White,
    surfaceVariant = LightGray,
    surfaceContainer = White,
    onSurface = Black,

    error = Red,
    onError = White
)

val ColorScheme.disabled: Color
    @Composable get() {
        return tertiaryContainer.copy(alpha = 0.6f)
    }

val ColorScheme.onDisabled: Color
    @Composable get() {
        return onTertiaryContainer.copy(alpha = 0.6f)
    }

enum class Theme {
    DARK, LIGHT, SYSTEM
}

@Serializable
data class UiPreferences(
    val theme: Theme = Theme.SYSTEM,
    val locale: Locale = Locale.ENGLISH
)

val Context.uiPreferences by dataStore("ui_preferences.json", UiPreferencesSerializer())

class UiPreferencesSerializer : Serializer<UiPreferences> {
    override suspend fun readFrom(input: InputStream): UiPreferences = input.use {
        val json = it.readBytes().decodeToString()
        Json.decodeFromString(deserializer = UiPreferences.serializer(), string = json)
    }

    override suspend fun writeTo(
        t: UiPreferences, output: OutputStream
    ) {
        val bytes = Json.encodeToString(serializer = UiPreferences.serializer(), value = t)
            .encodeToByteArray()
        output.use {
            it.write(bytes)
        }
    }

    override val defaultValue = UiPreferences()
}

@Composable
fun VideoHostingTheme(
    uiPreferences: UiPreferences = UiPreferences(),
    content: @Composable () -> Unit
) {
    val activity = LocalActivity.current!!
    val view = LocalView.current
    val context = LocalContext.current
    val colorScheme = when (uiPreferences.theme) {
        SYSTEM -> when {
            isSystemInDarkTheme() -> DarkColorScheme
            else -> LightColorScheme
        }
        DARK -> DarkColorScheme
        LIGHT -> LightColorScheme
    }
    LaunchedEffect(uiPreferences.theme) {
        val statusBarIconsColor = colorScheme.onSurface
        WindowCompat.getInsetsController(activity.window, view).isAppearanceLightStatusBars =
            (statusBarIconsColor != DarkColorScheme.onSurface)
    }
    LaunchedEffect(uiPreferences.locale) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.getSystemService(LocaleManager::class.java).applicationLocales =
                LocaleList.forLanguageTags(uiPreferences.locale.iso)
        } else {
            AppCompatDelegate.setApplicationLocales(
                LocaleListCompat.forLanguageTags(uiPreferences.locale.iso)
            )
        }
    }
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}