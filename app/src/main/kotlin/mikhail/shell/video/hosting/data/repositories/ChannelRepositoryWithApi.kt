package mikhail.shell.video.hosting.data.repositories

import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.tasks.await
import kotlinx.serialization.json.Json
import mikhail.shell.video.hosting.data.api.ChannelApi
import mikhail.shell.video.hosting.data.dto.ChannelCreationErrorResponse
import mikhail.shell.video.hosting.data.dto.ChannelEditingErrorResponse
import mikhail.shell.video.hosting.data.dto.toDomain
import mikhail.shell.video.hosting.data.utils.httpExceptionHandler
import mikhail.shell.video.hosting.data.utils.request
import mikhail.shell.video.hosting.data.utils.uriToPart
import mikhail.shell.video.hosting.domain.errors.Error
import mikhail.shell.video.hosting.domain.errors.channel.ChannelCreationError
import mikhail.shell.video.hosting.domain.errors.channel.ChannelEditingError
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
    ): Result<Channel, Error> = request (
        httpExceptionHandler(400) {
            val response = Json.decodeFromString<ChannelCreationErrorResponse>(it.response()?.body() as String)
            ChannelCreationError(
                titleError = response.title,
                aliasError = response.alias,
                descriptionError = response.description,
                headerError = response.header,
                logoError = response.logo
            )
        }
    ) {
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


    override suspend fun fetchChannelsByOwner(
        userId: Long,
        partIndex: Int,
        partSize: Int
    ): Result<List<Channel>, Error> =
        request {
            channelApi.getChannelsByOwner(
                userId = userId,
                partIndex = partIndex,
                partSize = partSize
            ).map { it.toDomain() }
        }

    override suspend fun fetchSubscriptions(
        partIndex: Long,
        partSize: Int
    ): Result<List<Channel>, Error> = request {
        channelApi.getSubscriptions(
            partIndex = partIndex,
            partSize = partSize
        ).map { it.toDomain() }
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
        headerAction: EditAction,
        header: String?,
        logoAction: EditAction,
        logo: String?
    ): Result<Channel, Error> = request (
        httpExceptionHandler(400) {
            val response = Json.decodeFromString<ChannelEditingErrorResponse>(it.response()?.body() as String)
            ChannelEditingError(
                titleError = response.title,
                aliasError = response.alias,
                descriptionError = response.description,
                headerError = response.header,
                logoError = response.logo
            )
        }
    ) {
        val headerPart = header?.let {
            fileProvider.uriToPart(uri = it, partName = "header")
        }
        val logoPart = logo?.let {
            fileProvider.uriToPart(uri = it, partName = "logo")
        }
        channelApi.editChannel(
            channel = ChannelEditingRequest(
                title = channel.title,
                alias = channel.alias,
                description = channel.description,
                headerAction = headerAction,
                logoAction = logoAction
            ),
            logo = logoPart,
            header = headerPart
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

data class ChannelEditingRequest(
    val title: String,
    val alias: String?,
    val description: String?,
    val headerAction: EditAction,
    val logoAction: EditAction
)