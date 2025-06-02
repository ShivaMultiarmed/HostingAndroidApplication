package mikhail.shell.video.hosting.domain.usecases.channels

import android.net.Uri
import mikhail.shell.video.hosting.domain.errors.CompoundError
import mikhail.shell.video.hosting.domain.errors.EditChannelError
import mikhail.shell.video.hosting.domain.models.Channel
import mikhail.shell.video.hosting.domain.models.EditAction
import mikhail.shell.video.hosting.domain.models.Result
import mikhail.shell.video.hosting.domain.providers.FileProvider
import mikhail.shell.video.hosting.domain.repositories.ChannelRepository
import mikhail.shell.video.hosting.domain.validation.ValidationRules
import javax.inject.Inject

class EditChannel @Inject constructor(
    private val channelRepository: ChannelRepository,
    private val fileProvider: FileProvider
) {
    suspend operator fun invoke(
        channel: Channel,
        coverAction: EditAction,
        coverUri: String?,
        avatarAction: EditAction,
        avatarUri: String?
    ): Result<Channel, CompoundError<EditChannelError>> {
        val error = CompoundError<EditChannelError>()
        if (channel.title.length > ValidationRules.MAX_TITLE_LENGTH) {
            error.add(EditChannelError.TITLE_TOO_LARGE)
        }
        if ((channel.alias?.length?: 0) > ValidationRules.MAX_TITLE_LENGTH) {
            error.add(EditChannelError.ALIAS_TOO_LARGE)
        }
        if ((channel.description?.length?:0) > ValidationRules.MAX_TEXT_LENGTH) {
            error.add(EditChannelError.DESCRIPTION_TOO_LARGE)
        }
        coverUri?.let {
            val uri = Uri.parse(it)
            if (fileProvider.exists(uri)) {
                if (fileProvider.getFileMimeType(uri)?.contains("image") != true) {
                    error.add(EditChannelError.COVER_TYPE_NOT_VALID)
                }
                if (fileProvider.getFileSize(uri)!! > ValidationRules.MAX_IMAGE_SIZE) {
                    error.add(EditChannelError.COVER_TOO_LARGE)
                }
            } else {
                error.add(EditChannelError.COVER_NOT_FOUND)
            }
        }
        avatarUri?.let {
            val uri = Uri.parse(it)
            if (fileProvider.exists(uri)) {
                if (fileProvider.getFileMimeType(uri)?.contains("image") != true) {
                    error.add(EditChannelError.AVATAR_TYPE_NOT_VALID)
                }
                if (fileProvider.getFileSize(uri)!! > ValidationRules.MAX_IMAGE_SIZE) {
                    error.add(EditChannelError.AVATAR_TOO_LARGE)
                }
            } else {
                error.add(EditChannelError.AVATAR_NOT_FOUND)
            }
        }
        return if (error.isNotNull()) {
            Result.Failure(error)
        } else {
            channelRepository.editChannel(
                channel,
                coverAction,
                coverUri,
                avatarAction,
                avatarUri
            )
        }
    }
}
