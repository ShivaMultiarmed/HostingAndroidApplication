package mikhail.shell.video.hosting.presentation.channel.create

import mikhail.shell.video.hosting.domain.errors.Error
import mikhail.shell.video.hosting.domain.errors.FileError
import mikhail.shell.video.hosting.domain.errors.TextError

data class ChannelCreationScreenState(
    val title: String = "",
    val titleError: Error? = null,
    val owner: Long,
    val alias: String = "",
    val aliasError: Error? = null,
    val logo: String? = null,
    val logoError: FileError? = null,
    val header: String? = null,
    val headerError: FileError? = null,
    val description: String = "",
    val descriptionError: TextError? = null,
    val channelId: Long? = null,
    val isCreating: Boolean = false,
    val creationError: Error? = null,
)