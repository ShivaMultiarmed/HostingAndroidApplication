package mikhail.shell.video.hosting.domain.providers

import mikhail.shell.video.hosting.domain.models.File
import java.io.InputStream

interface FileProvider {
    fun get(uri: String): File?
    fun getAsInputStream(uri: String): InputStream?
}