package mikhail.shell.video.hosting.domain.models

data class File(
    val uri: String,
    val name: String,
    val mimeType: String,
    val size: Long
)