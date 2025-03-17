package mikhail.shell.video.hosting.domain.usecases

import android.net.Uri
import mikhail.shell.video.hosting.domain.errors.CompoundError
import mikhail.shell.video.hosting.domain.errors.EditChannelError
import mikhail.shell.video.hosting.domain.models.Channel
import mikhail.shell.video.hosting.domain.models.EditAction
import mikhail.shell.video.hosting.domain.models.Result
import mikhail.shell.video.hosting.domain.providers.FileProvider
import mikhail.shell.video.hosting.domain.repositories.ChannelRepository
import mikhail.shell.video.hosting.presentation.utils.FILE_MAX_SIZE
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
        if (channel.title.length > 255) {
            error.add(EditChannelError.TITLE_TOO_LARGE)
        }
        if ((channel.alias?.length?: 0) > 255) {
            error.add(EditChannelError.ALIAS_TOO_LARGE)
        }
        if ((channel.description?.length?:0) > 5000) {
            error.add(EditChannelError.DESCRIPTION_TOO_LARGE)
        }
        coverUri?.let {
            val uri = Uri.parse(it)
            if (fileProvider.exists(uri)) {
                if (fileProvider.getFileSize(uri)!! > FILE_MAX_SIZE) {
                    error.add(EditChannelError.COVER_TOO_LARGE)
                }
            } else {
                error.add(EditChannelError.COVER_NOT_EXIST)
            }
        }
        avatarUri?.let {
            val uri = Uri.parse(it)
            if (fileProvider.exists(uri)) {
                if (fileProvider.getFileSize(uri)!! > FILE_MAX_SIZE) {
                    error.add(EditChannelError.AVATAR_TOO_LARGE)
                }
            } else {
                error.add(EditChannelError.AVATAR_NOT_EXIST)
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
