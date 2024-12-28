package mikhail.shell.video.hosting.domain.repositories

import mikhail.shell.video.hosting.domain.errors.ChannelCreationError
import mikhail.shell.video.hosting.domain.errors.ChannelError
import mikhail.shell.video.hosting.domain.errors.ChannelLoadingError
import mikhail.shell.video.hosting.domain.errors.CompoundError
import mikhail.shell.video.hosting.domain.models.Channel
import mikhail.shell.video.hosting.domain.models.ChannelWithUser
import mikhail.shell.video.hosting.domain.models.File
import mikhail.shell.video.hosting.domain.models.Result
import mikhail.shell.video.hosting.domain.models.SubscriptionState

interface ChannelRepository {
    suspend fun fetchChannelForUser(
        channelId: Long,
        userId: Long
    ): Result<ChannelWithUser, ChannelLoadingError>
    suspend fun createChannel(
        channel: Channel,
        avatar: File?,
        cover: File?
    ): Result<Channel, CompoundError<ChannelCreationError>>

    suspend fun fetchChannelsByOwner(
        userId: Long
    ): Result<List<Channel>, ChannelLoadingError>

    suspend fun fetchChannelsBySubscriber(
        userId: Long
    ): Result<List<Channel>, ChannelLoadingError>

    suspend fun subscribe(
        channelId: Long,
        userId: Long,
        subscriptionState: SubscriptionState
    ): Result<ChannelWithUser, ChannelLoadingError>
}