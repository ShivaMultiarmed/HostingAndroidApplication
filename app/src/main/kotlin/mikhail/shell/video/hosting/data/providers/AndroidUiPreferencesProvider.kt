package mikhail.shell.video.hosting.data.providers

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import mikhail.shell.video.hosting.domain.providers.UiPreferencesProvider
import mikhail.shell.video.hosting.presentation.settings.Locale
import mikhail.shell.video.hosting.ui.theme.UiPreferences
import mikhail.shell.video.hosting.ui.theme.uiPreferences
import javax.inject.Inject
import java.util.Locale as JavaLocale

class AndroidUiPreferencesProvider @Inject constructor(
    @param:ApplicationContext private val context: Context
): UiPreferencesProvider {
    private val coroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val dataStore = context.uiPreferences

    private val defaultLocale = JavaLocale.getDefault()

    override val preferences: StateFlow<UiPreferences> = dataStore.data.stateIn(
        scope = coroutineScope,
        started = SharingStarted.WhileSubscribed(3000),
        initialValue = UiPreferences(
            locale = try {
                Locale.ofTag(defaultLocale.language)
            } catch (_: Exception) {
                Locale.ENGLISH
            }
        )
    )

    override fun get(): UiPreferences {
        return preferences.value
    }

    override suspend fun set(preferences: UiPreferences) {
        dataStore.updateData { preferences }
    }
}