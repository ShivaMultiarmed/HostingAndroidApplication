package mikhail.shell.video.hosting.domain.providers

import android.net.Uri
import java.io.InputStream

interface FileProvider {
    fun getFileAsInputStream(uri: Uri): InputStream
    fun getFileMimeType(uri: Uri): String?
    fun getFileSize(uri: Uri): Long?
}