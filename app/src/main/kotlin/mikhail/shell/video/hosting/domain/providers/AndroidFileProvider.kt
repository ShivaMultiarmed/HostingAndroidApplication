package mikhail.shell.video.hosting.domain.providers

import android.content.Context
import android.net.Uri
import java.io.InputStream

class AndroidFileProvider(private val context: Context) : FileProvider {

    override fun getFileAsInputStream(uri: Uri): InputStream {
        val contentResolver = context.contentResolver
        return contentResolver.openInputStream(uri)
            ?: throw IllegalArgumentException("Unable to open input stream for URI: $uri")
    }

    override fun getFileMimeType(uri: Uri): String? {
        val contentResolver = context.contentResolver
        return contentResolver.getType(uri)
    }
}