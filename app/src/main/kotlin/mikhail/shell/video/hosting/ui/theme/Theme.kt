package mikhail.shell.video.hosting.ui.theme

import android.app.LocaleManager
import android.content.Context
import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.content.res.Configuration
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.content.edit
import androidx.core.os.LocaleListCompat
import androidx.core.view.WindowCompat
import mikhail.shell.video.hosting.domain.utils.SharedPreferencesUtils.Ui
import mikhail.shell.video.hosting.presentation.settings.Locale
import mikhail.shell.video.hosting.ui.theme.Theme.DARK
import mikhail.shell.video.hosting.ui.theme.Theme.LIGHT
import mikhail.shell.video.hosting.ui.theme.Theme.SYSTEM

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

enum class Theme {
    DARK, LIGHT, SYSTEM
}

fun Context.getCurrentTheme(): Theme {
    val str = getSharedPreferences(Ui.fileName, Context.MODE_PRIVATE)
        .getString(Ui.theme, SYSTEM.name) ?: SYSTEM.name
    return Theme.valueOf(str)
}

fun Context.getColorScheme(
    isSystemDarkTheme: Boolean
): ColorScheme {
    return when (getCurrentTheme()) {
        SYSTEM -> when {
            isSystemDarkTheme -> DarkColorScheme
            else -> LightColorScheme
        }

        DARK -> DarkColorScheme
        LIGHT -> LightColorScheme
    }
}

fun Context.setTheme(theme: Theme) {
    getSharedPreferences(Ui.fileName, Context.MODE_PRIVATE).edit {
        putString(Ui.theme, theme.name)
        commit()
    }
}

fun Context.getLocale(): Locale {
    return getSharedPreferences(Ui.fileName, Context.MODE_PRIVATE)
        .getString(Ui.language, Locale.ENGLISH.iso)!!
        .let { Locale.ofTag(it) }
}

fun Context.setLocale(locale: Locale) {
    val config = Configuration(resources.configuration)
    config.setLocales(LocaleList.forLanguageTags(locale.iso))
    getSharedPreferences(Ui.fileName, Context.MODE_PRIVATE).edit {
        putString(Ui.language, locale.iso)
        commit()
    }
}

@Composable
fun VideoHostingTheme(
    content: @Composable () -> Unit
) {
    val activity = LocalActivity.current!!
    val view = LocalView.current
    val context = LocalContext.current
    val isDark = isSystemInDarkTheme()
    val selectedColorScheme = context.getColorScheme(isDark)
    var colorScheme by remember { mutableStateOf(selectedColorScheme) }
    val uiPreferences = context.getSharedPreferences(Ui.fileName, Context.MODE_PRIVATE)
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
    val statusBarIconsColor = selectedColorScheme.onSurface
    LaunchedEffect(colorScheme) {
        WindowCompat.getInsetsController(activity.window, view).isAppearanceLightStatusBars = (statusBarIconsColor != DarkColorScheme.onSurface)
    }
    DisposableEffect(Unit) {
        val uiPreferencesListener = object : OnSharedPreferenceChangeListener {
            override fun onSharedPreferenceChanged(
                sharedPreferences: SharedPreferences?,
                key: String?
            ) {
                if (key == Ui.theme) {
                    colorScheme = context.getColorScheme(isDark)
                } else if (key == Ui.language) {
                    val localeTag = context.getLocale().iso
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        context.getSystemService(LocaleManager::class.java).applicationLocales =
                            LocaleList.forLanguageTags(localeTag)
                    } else {
                        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(localeTag))
                    }
                }
            }
        }
        uiPreferences.registerOnSharedPreferenceChangeListener(uiPreferencesListener)
        onDispose {
            uiPreferences.unregisterOnSharedPreferenceChangeListener(uiPreferencesListener)
        }
    }
}