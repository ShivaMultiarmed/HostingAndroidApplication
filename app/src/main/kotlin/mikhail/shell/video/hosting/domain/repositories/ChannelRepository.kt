package mikhail.shell.video.hosting.domain.repositories

import mikhail.shell.video.hosting.domain.errors.Error
import mikhail.shell.video.hosting.domain.models.Channel
import mikhail.shell.video.hosting.domain.models.ChannelForUser
import mikhail.shell.video.hosting.domain.models.EditAction
import mikhail.shell.video.hosting.domain.models.Result
import mikhail.shell.video.hosting.domain.models.Subscription

interface ChannelRepository {
    suspend fun existsByTitle(
        channelId: Long? = null,
        title: String
    ): Result<Unit, Error>

    suspend fun fetchChannelForUser(channelId: Long): Result<ChannelForUser, Error>

    suspend fun fetchChannelsByOwner(
        userId: Long,
        partIndex: Int,
        partSize: Int
    ): Result<List<Channel>, Error>

    suspend fun fetchSubscriptions(
        partIndex: Long,
        partSize: Int
    ): Result<List<Channel>, Error>

    suspend fun subscribe(channelId: Long, subscription: Subscription): Result<ChannelForUser, Error>

    suspend fun subscribeToNotifications(): Result<Unit, Error>

    suspend fun unsubscribeFromNotifications(): Result<Unit, Error>

    suspend fun editChannel(
        channel: Channel,
        headerAction: EditAction,
        header: String?,
        logoAction: EditAction,
        logo: String?
    ): Result<Channel, Error>

    suspend fun fetchChannel(channelId: Long): Result<Channel, Error>

    suspend fun removeChannel(channelId: Long): Result<Unit, Error>
    suspend fun create(
        channel: Channel,
        logo: String?,
        header: String?
    ): Result<Channel, Error>

    suspend fun existsByAlias(
        channelId: Long? = null,
        alias: String
    ): Result<Unit, Error>
}