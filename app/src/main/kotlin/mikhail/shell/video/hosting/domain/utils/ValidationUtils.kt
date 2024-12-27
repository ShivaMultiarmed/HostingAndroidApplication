package mikhail.shell.video.hosting.domain.utils

fun String?.isNotBlank(): Boolean {
    return this != null && this != ""
}

fun String?.isBlank(): Boolean {
    return this == null || this == ""
}