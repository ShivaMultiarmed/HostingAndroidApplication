package mikhail.shell.video.hosting.domain.utils

sealed class SharedPreferencesUtils(val fileName: String) {
    object Ui: SharedPreferencesUtils("ui_preferences") {
        const val theme = "theme"
        const val language = "language"
    }
}