package mikhail.shell.video.hosting.presentation.channel.edit

import mikhail.shell.video.hosting.domain.errors.Error
import mikhail.shell.video.hosting.domain.errors.FileError
import mikhail.shell.video.hosting.domain.errors.TextError
import mikhail.shell.video.hosting.domain.models.EditAction

data class ChannelEditingInputState(
    val title: String,
    val titleError: Error? = null,
    val alias: String,
    val aliasError: Error? = null,
    val description: String,
    val descriptionError: TextError? = null,
    val header: String? = null,
    val headerAction: EditAction = EditAction.KEEP,
    val headerError: FileError? = null,
    val logo: String? = null,
    val logoAction: EditAction = EditAction.KEEP,
    val logoError: FileError? = null
)
