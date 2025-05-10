package mikhail.shell.video.hosting.ui.theme

import android.content.Context
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.edit
import mikhail.shell.video.hosting.ui.theme.Theme.BY_TIME
import mikhail.shell.video.hosting.ui.theme.Theme.DARK
import mikhail.shell.video.hosting.ui.theme.Theme.LIGHT

private val DarkColorScheme = darkColorScheme(
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

private val LightColorScheme = lightColorScheme(
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

enum class Theme {
    DARK, LIGHT, BY_TIME
}

fun Context.getThemeSelected(): Theme {
    val str = this.getSharedPreferences("theme", Context.MODE_PRIVATE).getString("theme", BY_TIME.name)?: BY_TIME.name
    return Theme.valueOf(str)
}

fun Context.getColorScheme(
    isSystemDarkTheme: Boolean
): ColorScheme {
    return when(this.getThemeSelected()) {
        BY_TIME -> when {
            isSystemDarkTheme -> DarkColorScheme
            else -> LightColorScheme
        }
        DARK -> DarkColorScheme
        LIGHT -> LightColorScheme
    }
}

fun Context.setTheme(theme: Theme) {
    this.getSharedPreferences("theme", Context.MODE_PRIVATE).edit {
        putString("theme", theme.name)
        commit()
    }
}

@Composable
fun VideoHostingTheme(
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val isDark = isSystemInDarkTheme()
    val selectedColorScheme = context.getColorScheme(isDark)
    var colorScheme by remember { mutableStateOf(selectedColorScheme) }
    context.getSharedPreferences("theme", Context.MODE_PRIVATE)
        .registerOnSharedPreferenceChangeListener { sharedPreferences, key ->
            if (key == "theme") {
                colorScheme = context.getColorScheme(isDark)
            }
        }
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}