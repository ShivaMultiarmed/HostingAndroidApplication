package mikhail.shell.video.hosting.presentation.settings

import mikhail.shell.video.hosting.presentation.settings.SettingsScreenEvent as ScreenEvent

sealed class SettingsScreenEvent {
    data object Cancelled : ScreenEvent()
    data object ProfileEditingRequested : ScreenEvent()
}