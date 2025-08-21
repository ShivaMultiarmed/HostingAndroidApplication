package mikhail.shell.video.hosting.data.repositories

import com.google.firebase.messaging.FirebaseMessaging
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.tasks.await
import mikhail.shell.video.hosting.data.api.ChannelApi
import mikhail.shell.video.hosting.data.dto.toDomain
import mikhail.shell.video.hosting.data.dto.toDto
import mikhail.shell.video.hosting.data.utils.httpExceptionHandler
import mikhail.shell.video.hosting.data.utils.request
import mikhail.shell.video.hosting.data.utils.uriToPart
import mikhail.shell.video.hosting.domain.errors.CompoundError
import mikhail.shell.video.hosting.domain.errors.Error
import mikhail.shell.video.hosting.domain.errors.UnexpectedError
import mikhail.shell.video.hosting.domain.errors.channel.EditChannelError
import mikhail.shell.video.hosting.domain.models.Channel
import mikhail.shell.video.hosting.domain.models.ChannelForUser
import mikhail.shell.video.hosting.domain.models.EditAction
import mikhail.shell.video.hosting.domain.models.Result
import mikhail.shell.video.hosting.domain.models.Subscription
import mikhail.shell.video.hosting.domain.providers.FileProvider
import mikhail.shell.video.hosting.domain.repositories.ChannelRepository
import javax.inject.Inject

class ChannelRepositoryWithApi @Inject constructor(
    private val channelApi: ChannelApi,
    private val gson: Gson,
    private val fcm: FirebaseMessaging,
    private val fileProvider: FileProvider
) : ChannelRepository {

    override suspend fun existsByTitle(title: String): Result<Boolean, Error> = request {
        channelApi.existsByTitle(title)
    }

    override suspend fun existsByAlias(alias: String): Result<Boolean, Error> = request {
        channelApi.existsByAlias(alias)
    }

    override suspend fun create(
        channel: Channel,
        logo: String?,
        header: String?
    ): Result<Channel, Error> = request {
        channelApi.createChannel(
            channel = ChannelCreationRequest(
                title = channel.title,
                alias = channel.alias,
                description = channel.description
            ),
            logo = logo?.let {
                fileProvider.uriToPart(it, "logo")
            },
            header = header?.let {
                fileProvider.uriToPart(it, "header")
            }
        ).toDomain()
    }

    override suspend fun fetchChannelForUser(channelId: Long): Result<ChannelForUser, Error> =
        request {
            channelApi.fetchChannelDetails(channelId).toDomain()
        }


    override suspend fun fetchChannelsByOwner(userId: Long): Result<List<Channel>, Error> =
        request {
            channelApi.getChannelsByOwner(userId).map { it.toDomain() }
        }

    override suspend fun fetchChannelsBySubscriber(): Result<List<Channel>, Error> = request {
        channelApi.getChannelsBySubscriber().map { it.toDomain() }
    }

    override suspend fun subscribe(
        channelId: Long,
        subscription: Subscription
    ): Result<ChannelForUser, Error> = request {
        channelApi.subscribe(
            channelId = channelId,
            subscription = subscription,
            fcmToken = fcm.token.await()
        ).toDomain()
    }

    override suspend fun subscribeToNotifications(): Result<Unit, Error> = request {
        channelApi.subscribeToChannelNotifications(fcm.token.await())
    }

    override suspend fun unsubscribeFromNotifications(): Result<Unit, Error> = request {
        channelApi.unsubscribeFromChannelNotifications(fcm.token.await())
    }

    override suspend fun editChannel(
        channel: Channel,
        editCoverAction: EditAction,
        cover: String?,
        editAvatarAction: EditAction,
        avatar: String?
    ): Result<Channel, Error> = request(
        httpExceptionHandler(400) { e ->
            val responseBody = e.response()?.errorBody()?.string()
            val type = object : TypeToken<CompoundError<EditChannelError>>() {}.type
            gson.fromJson<CompoundError<EditChannelError>>(responseBody, type) ?: UnexpectedError
        }
    ) {
        val coverPart = if (editCoverAction == EditAction.UPDATE) fileProvider.uriToPart(
            cover!!,
            "cover"
        ) else null
        val avatarPart = if (editAvatarAction == EditAction.UPDATE) fileProvider.uriToPart(
            avatar!!,
            "avatar"
        ) else null
        channelApi.editChannel(
            channelDto = channel.toDto(),
            avatar = avatarPart,
            cover = coverPart,
            editCoverAction = editCoverAction,
            editAvatarAction = editAvatarAction
        ).toDomain()
    }

    override suspend fun fetchChannel(channelId: Long): Result<Channel, Error> = request {
        channelApi.fetchChannel(channelId).toDomain()
    }

    override suspend fun removeChannel(channelId: Long): Result<Unit, Error> = request {
        channelApi.removeChannel(channelId)
    }
}

data class ChannelCreationRequest(
    val title: String,
    val alias: String?,
    val description: String?
)