package mikhail.shell.video.hosting.domain.repositories

import mikhail.shell.video.hosting.domain.errors.ChannelCreationError
import mikhail.shell.video.hosting.domain.errors.ChannelLoadingError
import mikhail.shell.video.hosting.domain.errors.ChannelSubscriptionError
import mikhail.shell.video.hosting.domain.errors.CompoundError
import mikhail.shell.video.hosting.domain.errors.DeleteChannelError
import mikhail.shell.video.hosting.domain.errors.EditChannelError
import mikhail.shell.video.hosting.domain.models.Channel
import mikhail.shell.video.hosting.domain.models.ChannelWithUser
import mikhail.shell.video.hosting.domain.models.EditAction
import mikhail.shell.video.hosting.domain.models.Result
import mikhail.shell.video.hosting.domain.models.SubscriptionState
import java.io.File

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

    suspend fun subscribeToNotifications(
        userId: Long
    ): Result<Unit, ChannelSubscriptionError>

    suspend fun unsubscribeFromNotifications(
        userId: Long
    ): Result<Unit, ChannelSubscriptionError>

    suspend fun editChannel(
        channel: Channel,
        editCoverAction: EditAction,
        cover: String?,
        editAvatarAction: EditAction,
        avatar: String?
    ): Result<Channel, CompoundError<EditChannelError>>

    suspend fun fetchChannel(
        channelId: Long
    ): Result<Channel, ChannelLoadingError>

    suspend fun removeChannel(channelId: Long): Result<Unit, DeleteChannelError>
}