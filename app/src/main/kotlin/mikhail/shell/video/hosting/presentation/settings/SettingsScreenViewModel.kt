package mikhail.shell.video.hosting.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import mikhail.shell.video.hosting.domain.providers.UiPreferencesProvider
import mikhail.shell.video.hosting.domain.providers.setLocale
import mikhail.shell.video.hosting.domain.providers.setTheme
import mikhail.shell.video.hosting.ui.theme.Theme
import javax.inject.Inject
import mikhail.shell.video.hosting.presentation.settings.SettingsScreenAction as ScreenAction
import mikhail.shell.video.hosting.presentation.settings.SettingsScreenEvent as ScreenEvent
import mikhail.shell.video.hosting.presentation.settings.SettingsScreenState as ScreenState

@HiltViewModel
class SettingsScreenViewModel @Inject constructor(
    private val uiPreferencesProvider: UiPreferencesProvider
): ViewModel() {
    private val _state = MutableStateFlow(ScreenState(uiSettings = uiPreferencesProvider.get()))
    val state = _state.asStateFlow()

    private val _events = MutableSharedFlow<ScreenEvent>()
    val events = _events.asSharedFlow()

    fun onAction(action: ScreenAction) {
        when (action) {
            is ScreenAction.ChangeLocale -> onLocaleChanged(action.locale)
            is ScreenAction.ChangeTheme -> onThemeChanged(action.theme)
            ScreenAction.Cancel -> viewModelScope.launch {
                _events.emit(ScreenEvent.Cancelled)
            }
            ScreenAction.EditProfile -> viewModelScope.launch {
                _events.emit(ScreenEvent.ProfileEditingRequested)
            }
        }
    }

    private fun onLocaleChanged(locale: Locale) {
        _state.update {
            it.copy(
                uiSettings = it.uiSettings.copy(locale = locale)
            )
        }
        viewModelScope.launch {
            uiPreferencesProvider.setLocale(locale)
        }
    }

    private fun onThemeChanged(theme: Theme) {
        _state.update {
            it.copy(
                uiSettings = it.uiSettings.copy(theme = theme)
            )
        }
        viewModelScope.launch {
            uiPreferencesProvider.setTheme(theme)
        }
    }
}