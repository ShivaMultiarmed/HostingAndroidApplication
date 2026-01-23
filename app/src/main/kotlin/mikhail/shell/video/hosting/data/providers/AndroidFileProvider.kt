package mikhail.shell.video.hosting.data.providers

import android.content.Context
import android.provider.OpenableColumns
import androidx.core.net.toUri
import dagger.hilt.android.qualifiers.ApplicationContext
import mikhail.shell.video.hosting.domain.models.File
import mikhail.shell.video.hosting.domain.providers.FileProvider
import org.apache.tika.Tika
import java.io.InputStream

class AndroidFileProvider(
    @ApplicationContext context: Context
) : FileProvider {
    private val contentResolver = context.contentResolver

    override fun get(uri: String): File? {
        return contentResolver.query(
            uri.toUri(),
            arrayOf(
                OpenableColumns.DISPLAY_NAME,
                OpenableColumns.SIZE
            ),
            null,
            null,
            null
        )?.use {
            if (it.moveToFirst()) {
                File(
                    uri = uri,
                    name = it.getString(0)?: "",
                    mimeType = Tika().detect(uri)?: "application/octet-stream",
                    size = it.getLong(1)
                )
            } else null
        }
    }

    override fun getAsInputStream(uri: String): InputStream? {
        return contentResolver.openInputStream(uri.toUri())
    }
}