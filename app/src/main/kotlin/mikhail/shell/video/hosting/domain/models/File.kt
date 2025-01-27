package mikhail.shell.video.hosting.domain.models

data class File(
    val name: String? = null,
    val mimeType: String? = null,
    val content: ByteArray? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as File

        if (name != other.name) return false
        if (!content.contentEquals(other.content)) return false

        return true
    }

    override fun hashCode(): Int {
        return 31 * name.hashCode() + content.contentHashCode()
    }
}
