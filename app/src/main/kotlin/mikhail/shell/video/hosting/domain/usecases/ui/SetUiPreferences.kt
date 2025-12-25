package mikhail.shell.video.hosting.domain.usecases.ui

import mikhail.shell.video.hosting.domain.providers.UiPreferencesProvider
import mikhail.shell.video.hosting.ui.theme.UiPreferences
import javax.inject.Inject

class SetUiPreferences @Inject constructor(
    private val uiPreferencesProvider: UiPreferencesProvider
) {
    suspend operator fun invoke(preferences: UiPreferences) {
        uiPreferencesProvider.set(preferences)
    }
}