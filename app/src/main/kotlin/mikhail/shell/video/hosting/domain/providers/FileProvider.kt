package mikhail.shell.video.hosting.domain.providers

import android.net.Uri
import mikhail.shell.video.hosting.domain.models.File
import java.io.InputStream

interface FileProvider {
    fun getFileAsInputStream(uri: Uri): InputStream?
    fun getFileMimeType(uri: Uri): String?
    fun getFileSize(uri: Uri): Long?
    fun exists(uri: Uri): Boolean
    fun getFile(uri: Uri): File?
}