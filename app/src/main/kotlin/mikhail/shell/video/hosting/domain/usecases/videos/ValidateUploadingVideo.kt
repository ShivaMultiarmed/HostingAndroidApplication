package mikhail.shell.video.hosting.domain.usecases.videos

import android.net.Uri
import mikhail.shell.video.hosting.domain.errors.CompoundError
import mikhail.shell.video.hosting.domain.errors.UploadVideoError
import mikhail.shell.video.hosting.domain.models.Video
import mikhail.shell.video.hosting.domain.providers.FileProvider
import mikhail.shell.video.hosting.domain.validation.ValidationRules
import javax.inject.Inject

class ValidateUploadingVideo @Inject constructor(
    private val fileProvider: FileProvider
) {
    operator fun invoke(video: Video, source: String, cover: String?): CompoundError<UploadVideoError>? {
        val compoundError = CompoundError<UploadVideoError>()
        if (video.title.length > ValidationRules.MAX_TITLE_LENGTH) {
            compoundError.add(UploadVideoError.TITLE_TOO_LARGE)
        }
        val sourceUri = Uri.parse(source)
        val sourceMime = fileProvider.getFileMimeType(sourceUri)
        val sourceSize = fileProvider.getFileSize(sourceUri)!!
        if (!fileProvider.exists(sourceUri)) {
            compoundError.add(UploadVideoError.SOURCE_NOT_FOUND)
        } else if (!sourceMime!!.startsWith("video")) {
            compoundError.add(UploadVideoError.SOURCE_TYPE_NOT_VALID)
        } else if (sourceSize > ValidationRules.MAX_VIDEO_SIZE) {
            compoundError.add(UploadVideoError.SOURCE_TOO_LARGE)
        }
        cover?.let { notNullCover ->
            val coverUri = Uri.parse(notNullCover)
            val coverMime = fileProvider.getFileMimeType(coverUri)
            if (!fileProvider.exists(coverUri)) {
                compoundError.add(UploadVideoError.COVER_NOT_FOUND)
            } else if (!coverMime!!.contains("image")) {
                compoundError.add(UploadVideoError.COVER_TYPE_NOT_VALID)
            } else if ((fileProvider.getFileSize(coverUri) ?: 0) > ValidationRules.MAX_IMAGE_SIZE) {
                compoundError.add(UploadVideoError.COVER_TOO_LARGE)
            }
        }
        return compoundError.takeIf { it.isNotNull() }
    }
}