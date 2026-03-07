package mikhail.shell.video.hosting.domain.validation

import kotlin.math.floor

object ValidationRules {
    const val FILE_NAME_REGEX = "^\\S+\\.\\S+$"
    const val MIN_PASSWORD_LENGTH = 8
    const val MAX_PASSWORD_LENGTH = 20
    const val MAX_TITLE_LENGTH = 100
    const val MAX_NAME_LENGTH = 50
    const val MAX_TEXT_LENGTH = 255
    const val MAX_USERNAME_LENGTH = 50
    const val MAX_EMAIL_LENGTH = 50
    const val MAX_TEL_LENGTH = 15
    const val MIN_TEL_LENGTH = 8
    const val MAX_IMAGE_SIZE = 5 * 1024 * 1024
    const val MAX_VIDEO_SIZE = 1024 * 1024 * 1024
    const val CODE_LENGTH = 4
    val PASSWORD_REGEX = Regex("^(?=.*[0-9])(?=.*[^a-zA-Z0-9])\\S{$MIN_PASSWORD_LENGTH,$MAX_PASSWORD_LENGTH}$")
    val EMAIL_REGEX = Regex("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}\$")
    val TEL_REGEX = Regex("^\\d{$MIN_TEL_LENGTH,$MAX_TEL_LENGTH}\$")
}

inline val Int.mb
    get() = floor(toDouble() / 1024 / 1024).toInt()