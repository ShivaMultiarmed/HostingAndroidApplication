package mikhail.shell.video.hosting.data.providers

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import mikhail.shell.video.hosting.domain.models.File
import mikhail.shell.video.hosting.domain.providers.FileProvider
import java.io.InputStream

class AndroidFileProvider(context: Context) : FileProvider {

    private val contentResolver = context.contentResolver

    override fun getFileAsInputStream(uri: Uri): InputStream? {
        return contentResolver.openInputStream(uri)
    }

    override fun getFileMimeType(uri: Uri): String? {
        return contentResolver.getType(uri)
    }

    override fun getFileSize(uri: Uri): Long? {
        return contentResolver.query(uri, arrayOf(OpenableColumns.SIZE), null, null, null)?.use {
            if (it.moveToFirst()) {
                it.getLong(0)
            } else {
                null
            }
        }
    }

    override fun getFile(uri: Uri): File? {
        return File(
            uri = uri.toString(),
            mimeType = getFileMimeType(uri),
            size = getFileSize(uri)
        )
    }

    override fun exists(uri: Uri): Boolean {
        return contentResolver.query(uri, arrayOf(OpenableColumns.SIZE), null, null, null)?.use {
            it.moveToFirst()
        }?: false
    }
}