package mikhail.shell.video.hosting.presentation.utils

import android.content.ContentResolver
import android.net.Uri
import android.webkit.MimeTypeMap
import mikhail.shell.video.hosting.domain.models.File

fun ContentResolver.getFileBytes(uri: Uri): ByteArray? {
    val inputStream = this.openInputStream(uri)
    return inputStream?.readBytes()
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