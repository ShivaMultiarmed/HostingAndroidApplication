package mikhail.shell.video.hosting.domain.providers

import mikhail.shell.video.hosting.domain.models.File
import java.io.InputStream

interface FileProvider {
    fun getFileName(uri: String): String?
    fun getFileAsInputStream(uri: String): InputStream?
    fun getFileMimeType(uri: String): String?
    fun getFileSize(uri: String): Long?
    fun exists(uri: String): Boolean
    fun getFile(uri: String): File?
}