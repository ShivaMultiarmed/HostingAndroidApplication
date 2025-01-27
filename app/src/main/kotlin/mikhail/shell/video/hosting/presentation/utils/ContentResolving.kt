package mikhail.shell.video.hosting.presentation.utils

import android.content.ContentResolver
import android.net.Uri
import android.webkit.MimeTypeMap
import mikhail.shell.video.hosting.domain.models.File
import java.io.ByteArrayOutputStream
import kotlin.math.min

fun ContentResolver.getFileBytes(uri: Uri): ByteArray? {
    val inputStream = this.openInputStream(uri) ?: return null
    return inputStream.use { input ->
        val outputStream = ByteArrayOutputStream()
        outputStream.use { output ->
            val buffer = ByteArray(1024)
            var bytesRead: Int
            while (input.read(buffer).also { bytesRead = it } != -1) {
                output.write(buffer, 0, bytesRead)
            }
            output.toByteArray()
        }
    }
}

fun ContentResolver.uriToFile(uri: Uri): File {
    val mimeType = this.getType(uri)
    val extension =
        MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType)
    return File(
        name = uri.lastPathSegment + "." + extension,
        mimeType = this.getType(uri),
        content = this.getFileBytes(uri),
    )
}