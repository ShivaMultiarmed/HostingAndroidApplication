package mikhail.shell.video.hosting.data.repositories

import com.google.firebase.messaging.FirebaseMessaging
import com.google.gson.Gson
import kotlinx.coroutines.tasks.await
import mikhail.shell.video.hosting.BuildConfig.API_BASE_URL
import mikhail.shell.video.hosting.data.api.ChannelApi
import mikhail.shell.video.hosting.data.dto.ChannelCreationErrorResponse
import mikhail.shell.video.hosting.data.dto.ChannelEditingErrorResponse
import mikhail.shell.video.hosting.data.dto.toDomain
import mikhail.shell.video.hosting.data.utils.httpExceptionHandler
import mikhail.shell.video.hosting.data.utils.request
import mikhail.shell.video.hosting.data.utils.uriToPart
import mikhail.shell.video.hosting.domain.errors.Error
import mikhail.shell.video.hosting.domain.errors.TextError
import mikhail.shell.video.hosting.domain.errors.channel.ChannelCreationError
import mikhail.shell.video.hosting.domain.errors.channel.ChannelEditingError
import mikhail.shell.video.hosting.domain.models.Channel
import mikhail.shell.video.hosting.domain.models.ChannelCreationModel
import mikhail.shell.video.hosting.domain.models.ChannelEditingModel
import mikhail.shell.video.hosting.domain.models.ChannelForUser
import mikhail.shell.video.hosting.domain.models.EditAction
import mikhail.shell.video.hosting.domain.models.EditingAction
import mikhail.shell.video.hosting.domain.models.ImageSize
import mikhail.shell.video.hosting.domain.models.Result
import mikhail.shell.video.hosting.domain.models.Subscription
import mikhail.shell.video.hosting.domain.providers.FileProvider
import mikhail.shell.video.hosting.domain.repositories.ChannelRepository
import javax.inject.Inject

class ChannelRepositoryWithApi @Inject constructor(
    private val channelApi: ChannelApi,
    private val fcm: FirebaseMessaging,
    private val fileProvider: FileProvider,
    private val gson: Gson
) : ChannelRepository {

    override suspend fun existsByAlias(
        channelId: Long?,
        alias: String
    ): Result<Unit, Error> = request (
        httpExceptionHandler(409) {
            TextError.EXISTS
        }
    ) {
        channelApi.existsByAlias(channelId = channelId, alias = alias)
    }

    override fun constructHeaderUrl(
        channelId: Long,
        size: ImageSize
    ): String {
        return "$API_BASE_URL/channels/$channelId/header?size=${size.name.lowercase()}"
    }

    override suspend fun create(channel: ChannelCreationModel): Result<Channel, Error> = request(
        httpExceptionHandler(400) {
            val json = it.response()?.errorBody()!!.string()
            val response = gson.fromJson(json, ChannelCreationErrorResponse::class.java)
            ChannelCreationError(
                titleError = response.titleError,
                aliasError = response.aliasError,
                descriptionError = response.descriptionError,
                headerError = response.headerError,
                logoError = response.logoError
            )
        }
    ) {
        channelApi.createChannel(
            channel = ChannelCreationRequest(
                title = channel.title,
                alias = channel.alias,
                description = channel.description
            ),
            logo = channel.logo?.let {
                fileProvider.uriToPart(it, "logo")
            },
            header = channel.header?.let {
                fileProvider.uriToPart(it, "header")
            }
        ).toDomain()
    }

    override fun constructLogoUrl(channelId: Long, size: ImageSize): String {
        return "$API_BASE_URL/channels/$channelId/logo?size=${size.name.lowercase()}"
    }

    override suspend fun existsByTitle(
        channelId: Long?,
        title: String
    ): Result<Unit, Error> = request(
        httpExceptionHandler(409) {
            TextError.EXISTS
        }
    ) {
        channelApi.existsByTitle(channelId = channelId, title = title)
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
            subscription = subscription.name.lowercase(),
            messagingToken = fcm.token.await()
        ).toDomain()
    }

    override suspend fun edit(channel: ChannelEditingModel): Result<Channel, Error> = request(
        httpExceptionHandler(400) {
            val json = it.response()?.errorBody()!!.string()
            val response = gson.fromJson(json, ChannelEditingErrorResponse::class.java)
            ChannelEditingError(
                titleError = response.titleError,
                aliasError = response.aliasError,
                descriptionError = response.descriptionError,
                headerError = response.headerError,
                logoError = response.logoError
            )
        }
    ) {
        val headerPart = when (channel.header) {
            is EditingAction.Edit -> fileProvider.uriToPart(uri = channel.header.value, partName = "header")
            else -> null
        }
        val logoPart = when (channel.logo){
            is EditingAction.Edit -> fileProvider.uriToPart(uri = channel.logo.value, partName = "logo")
            else -> null
        }
        channelApi.editChannel(
            channel = ChannelEditingRequest(
                channelId = channel.channelId,
                title = channel.title,
                alias = channel.alias,
                description = channel.description,
                headerAction = when (channel.header) {
                    is EditingAction.Edit -> EditAction.EDIT
                    EditingAction.Keep -> EditAction.KEEP
                    EditingAction.Remove -> EditAction.REMOVE
                },
                logoAction = when (channel.logo) {
                    is EditingAction.Edit -> EditAction.EDIT
                    EditingAction.Keep -> EditAction.KEEP
                    EditingAction.Remove -> EditAction.REMOVE
                }
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
    val channelId: Long,
    val title: String,
    val alias: String?,
    val description: String?,
    val headerAction: EditAction,
    val logoAction: EditAction
)