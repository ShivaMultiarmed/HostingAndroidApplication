package mikhail.shell.video.hosting.domain.providers

import kotlinx.coroutines.flow.StateFlow
import mikhail.shell.video.hosting.presentation.settings.Locale
import mikhail.shell.video.hosting.ui.theme.Theme
import mikhail.shell.video.hosting.ui.theme.UiPreferences

interface UiPreferencesProvider {
    val preferences: StateFlow<UiPreferences>
    fun get(): UiPreferences
    suspend fun set(preferences: UiPreferences)
}

suspend fun UiPreferencesProvider.setLocale(locale: Locale) {
    set(get().copy(locale = locale))
}

suspend fun UiPreferencesProvider.setTheme(theme: Theme) {
    set(get().copy(theme = theme))
}