package mikhail.shell.video.hosting.presentation.utils

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

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

fun Context.uriToFile(uri: Uri): File? {
    val BUFFER_SIZE = 100 * 1024
    val inputStream = this.contentResolver.openInputStream(uri)?: return null
    val tmpFile = File(cacheDir.absolutePath, "tmp_file_${System.currentTimeMillis()}.tmp")
    val outputStream = FileOutputStream(tmpFile)
    inputStream.use {
        val buffer = ByteArray(BUFFER_SIZE)
        while (inputStream.read(buffer) != -1) {
            outputStream.write(buffer)
        }
    }
    return tmpFile
}