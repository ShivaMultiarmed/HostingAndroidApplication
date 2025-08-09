package mikhail.shell.video.hosting.domain.repositories

import mikhail.shell.video.hosting.domain.errors.Error
import mikhail.shell.video.hosting.domain.models.Channel
import mikhail.shell.video.hosting.domain.models.ChannelWithUser
import mikhail.shell.video.hosting.domain.models.EditAction
import mikhail.shell.video.hosting.domain.models.Result
import java.io.File

interface ChannelRepository {
    suspend fun fetchChannelForUser(channelId: Long): Result<ChannelWithUser, Error>

    suspend fun createChannel(
        channel: Channel,
        avatar: File?,
        cover: File?
    ): Result<Channel, Error>

    suspend fun fetchChannelsByOwner(userId: Long): Result<List<Channel>, Error>

    suspend fun fetchChannelsBySubscriber(userId: Long): Result<List<Channel>, Error>

    suspend fun subscribe(channelId: Long): Result<Unit, Error>

    suspend fun subscribeToNotifications(): Result<Unit, Error>

    suspend fun unsubscribeFromNotifications(): Result<Unit, Error>

    suspend fun editChannel(
        channel: Channel,
        editCoverAction: EditAction,
        cover: String?,
        editAvatarAction: EditAction,
        avatar: String?
    ): Result<Channel, Error>

    suspend fun fetchChannel(channelId: Long): Result<Channel, Error>

    suspend fun removeChannel(channelId: Long): Result<Unit, Error>
}