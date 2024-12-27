package mikhail.shell.video.hosting.presentation.video.upload

import mikhail.shell.video.hosting.domain.errors.CompoundError
import mikhail.shell.video.hosting.domain.errors.UploadVideoError
import mikhail.shell.video.hosting.domain.models.Channel
import mikhail.shell.video.hosting.domain.models.Video

data class UploadVideoScreenState(
    val channels: List<Channel>? = null,
    val video: Video? = null,
    val isLoading: Boolean = false,
    val error: CompoundError<UploadVideoError>? = null
)