package mikhail.shell.video.hosting.presentation.utils

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.webkit.MimeTypeMap
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

const val IO_BUFFER_SIZE = 100 * 1024

fun ContentResolver.getFileBytes(uri: Uri): ByteArray? {
    val inputStream = this.openInputStream(uri) ?: return null
    return inputStream.use { input ->
        val outputStream = ByteArrayOutputStream()
        outputStream.use { output ->
            val buffer = ByteArray(IO_BUFFER_SIZE)
            var bytesRead: Int
            while (input.read(buffer).also { bytesRead = it } != -1) {
                output.write(buffer, 0, bytesRead)
            }
            output.toByteArray()
        }
    }
}

fun Context.uriToFile(uri: Uri): File? {
    val inputStream = this.contentResolver.openInputStream(uri)?: return null
    val mimeType = this.contentResolver.getType(uri)
    val extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType)
    val tmpFile = File(cacheDir.absolutePath, "tmp_file_${System.currentTimeMillis()}.$extension")
    val outputStream = FileOutputStream(tmpFile)
    inputStream.use {
        val buffer = ByteArray(IO_BUFFER_SIZE)
        while (inputStream.read(buffer) != -1) {
            outputStream.write(buffer)
        }
    }
    return tmpFile
}