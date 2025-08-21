package mikhail.shell.video.hosting.data.providers

import android.content.Context
import android.provider.OpenableColumns
import androidx.core.net.toUri
import mikhail.shell.video.hosting.domain.models.File
import mikhail.shell.video.hosting.domain.providers.FileProvider
import java.io.InputStream

class AndroidFileProvider(context: Context) : FileProvider {

    private val contentResolver = context.contentResolver

    override fun getFileAsInputStream(uri: String): InputStream? {
        return contentResolver.openInputStream(uri.toUri())
    }

    override fun getFileMimeType(uri: String): String? {
        return contentResolver.getType(uri.toUri())
    }

    override fun getFileSize(uri: String): Long? {
        return contentResolver.query(uri.toUri(), arrayOf(OpenableColumns.SIZE), null, null, null)?.use {
            if (it.moveToFirst()) {
                it.getLong(0)
            } else {
                null
            }
        }
    }

    override fun getFile(uri: String): File? {
        return File(
            uri = uri,
            mimeType = getFileMimeType(uri),
            size = getFileSize(uri)
        )
    }

    override fun exists(uri: String): Boolean {
        return contentResolver
            .query(uri.toUri(), arrayOf(OpenableColumns.SIZE), null, null, null)
            ?.use { it.moveToFirst() } == true
    }
}