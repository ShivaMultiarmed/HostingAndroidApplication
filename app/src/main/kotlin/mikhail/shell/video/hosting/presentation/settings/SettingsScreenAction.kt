package mikhail.shell.video.hosting.presentation.settings

import mikhail.shell.video.hosting.ui.theme.Theme
import mikhail.shell.video.hosting.presentation.settings.SettingsScreenAction as ScreenAction

sealed class SettingsScreenAction {
    data object EditProfile : ScreenAction()
    data object Cancel : ScreenAction()
    data class ChangeTheme(val theme: Theme) : ScreenAction()
    data class ChangeLocale(val locale: Locale) : ScreenAction()
}