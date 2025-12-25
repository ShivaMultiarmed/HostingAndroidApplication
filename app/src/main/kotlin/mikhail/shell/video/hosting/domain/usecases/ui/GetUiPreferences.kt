package mikhail.shell.video.hosting.domain.usecases.ui

import mikhail.shell.video.hosting.domain.providers.UiPreferencesProvider
import mikhail.shell.video.hosting.ui.theme.UiPreferences
import javax.inject.Inject

class GetUiPreferences @Inject constructor(
    private val uiPreferencesProvider: UiPreferencesProvider
) {
    operator fun invoke(): UiPreferences {
        return uiPreferencesProvider.get()
    }
}