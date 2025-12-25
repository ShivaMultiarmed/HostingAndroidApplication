package mikhail.shell.video.hosting.domain.usecases.ui

import kotlinx.coroutines.flow.StateFlow
import mikhail.shell.video.hosting.domain.providers.UiPreferencesProvider
import mikhail.shell.video.hosting.ui.theme.UiPreferences
import javax.inject.Inject

class ObserveUiPreferences @Inject constructor(
    private val uiPreferencesProvider: UiPreferencesProvider
) {
    operator fun invoke(): StateFlow<UiPreferences> {
        return uiPreferencesProvider.preferences
    }
}