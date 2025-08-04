package mikhail.shell.video.hosting.data.repositories

import android.content.Context
import android.webkit.MimeTypeMap
import com.google.firebase.messaging.FirebaseMessaging
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
import mikhail.shell.video.hosting.data.api.ChannelApi
import mikhail.shell.video.hosting.data.dto.toDomain
import mikhail.shell.video.hosting.data.dto.toDto
import mikhail.shell.video.hosting.data.utils.httpExceptionHandler
import mikhail.shell.video.hosting.data.utils.request
import mikhail.shell.video.hosting.domain.errors.CompoundError
import mikhail.shell.video.hosting.domain.errors.Error
import mikhail.shell.video.hosting.domain.errors.ValidationException
import mikhail.shell.video.hosting.domain.errors.channel.ChannelCreationError
import mikhail.shell.video.hosting.domain.errors.channel.ChannelLoadingError
import mikhail.shell.video.hosting.domain.errors.channel.DeleteChannelError
import mikhail.shell.video.hosting.domain.errors.channel.EditChannelError
import mikhail.shell.video.hosting.domain.models.Channel
import mikhail.shell.video.hosting.domain.models.ChannelWithUser
import mikhail.shell.video.hosting.domain.models.EditAction
import mikhail.shell.video.hosting.domain.models.Result
import mikhail.shell.video.hosting.domain.models.SubscriptionState
import mikhail.shell.video.hosting.domain.providers.FileProvider
import mikhail.shell.video.hosting.domain.repositories.ChannelRepository
import mikhail.shell.video.hosting.domain.validation.ValidationRules
import java.io.File
import javax.inject.Inject

class ChannelRepositoryWithApi @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val _channelApi: ChannelApi,
    private val gson: Gson,
    private val fcm: FirebaseMessaging,
    private val fileProvider: FileProvider
) : ChannelRepository {

    private val BUFFER_SIZE = 10 * 1024 * 1024

    override suspend fun fetchChannelForUser(
        channelId: Long,
        userId: Long
    ): Result<ChannelWithUser, Error> = request (
        httpExceptionHandler(400) { ChannelLoadingError.USER_NOT_FOUND },
        httpExceptionHandler(404) { ChannelLoadingError.NOT_FOUND }
    ) {
        _channelApi.fetchChannelDetails(channelId, userId).toDomain()
    }

    override suspend fun createChannel(
        channel: Channel,
        avatar: File?,
        cover: File?
    ): Result<Channel, Error> = request(
        httpExceptionHandler(400) { e ->
            val responseBody = e.response()?.errorBody()?.string()
            val type = object : TypeToken<CompoundError<ChannelCreationError>>() {}.type
            gson.fromJson<CompoundError<ChannelCreationError>>(responseBody, type)?: ChannelCreationError.UNEXPECTED
        }
    ) {
        val compoundError = CompoundError<ChannelCreationError>()
        avatar?.let {
            if (!it.exists()) {
                compoundError.add(ChannelCreationError.AVATAR_NOT_FOUND)
            } else {
                val mimeType =
                    MimeTypeMap.getSingleton().getMimeTypeFromExtension(it.extension)
                if (!mimeType!!.contains("image")) {
                    compoundError.add(ChannelCreationError.AVATAR_TYPE_NOT_VALID)
                }
                if (it.length() > ValidationRules.MAX_IMAGE_SIZE) {
                    compoundError.add(ChannelCreationError.AVATAR_TOO_LARGE)
                }
            }
        }
        cover?.let {
            if (!it.exists()) {
                compoundError.add(ChannelCreationError.COVER_NOT_FOUND)
            } else {
                val mimeType =
                    MimeTypeMap.getSingleton().getMimeTypeFromExtension(it.extension)
                if (!mimeType!!.contains("image")) {
                    compoundError.add(ChannelCreationError.COVER_TYPE_NOT_VALID)
                }
                if (it.length() > ValidationRules.MAX_IMAGE_SIZE) {
                    compoundError.add(ChannelCreationError.COVER_TOO_LARGE)
                }
            }
        }
        if (compoundError.isNotNull()) {
            throw ValidationException(compoundError)
        }
        val avatarPart = avatar?.toPart("avatar")
        val coverPart = cover?.toPart("cover")
        val response = _channelApi.createChannel(
            channel.toDto(),
            avatarPart,
            coverPart
        )
        response.toDomain()
    }

    override suspend fun fetchChannelsByOwner(userId: Long): Result<List<Channel>, Error> = request(
        httpExceptionHandler(404) { ChannelLoadingError.USER_NOT_FOUND }
    ) {
        _channelApi.getChannelsByOwner(userId).map { it.toDomain() }
    }

    override suspend fun fetchChannelsBySubscriber(userId: Long): Result<List<Channel>, Error> =
        request(
            httpExceptionHandler(404) { ChannelLoadingError.USER_NOT_FOUND }
        ) {
            _channelApi.getChannelsBySubscriber(userId).map { it.toDomain() }
        }

    override suspend fun subscribe(
        channelId: Long,
        userId: Long,
        subscriptionState: SubscriptionState
    ): Result<ChannelWithUser, Error> = request {
        _channelApi.subscribe(
            channelId = channelId,
            userId = userId,
            token = fcm.token.await(),
            subscriptionState = subscriptionState
        ).toDomain()
    }

    override suspend fun subscribeToNotifications(
        userId: Long
    ): Result<Unit, Error> = request {
        _channelApi.subscribeToChannelNotifications(userId, fcm.token.await())
    }

    override suspend fun unsubscribeFromNotifications(
        userId: Long
    ): Result<Unit, Error> = request {
        _channelApi.unsubscribeFromChannelNotifications(userId, fcm.token.await())
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
            gson.fromJson<CompoundError<EditChannelError>>(responseBody, type)?: EditChannelError.UNEXPECTED
        },
        httpExceptionHandler(403) { EditChannelError.FORBIDDEN },
        httpExceptionHandler(404) { EditChannelError.CHANNEL_NOT_EXIST }
    ) {
        val coverPart = if (editCoverAction == EditAction.UPDATE) fileProvider.uriToPart(
            cover!!,
            "cover"
        ) else null
        val avatarPart = if (editAvatarAction == EditAction.UPDATE) fileProvider.uriToPart(
            avatar!!,
            "avatar"
        ) else null
        _channelApi.editChannel(
            channelDto = channel.toDto(),
            avatar = avatarPart,
            cover = coverPart,
            editCoverAction = editCoverAction,
            editAvatarAction = editAvatarAction
        ).toDomain()
    }

    override suspend fun fetchChannel(channelId: Long): Result<Channel, Error> = request (
        httpExceptionHandler(404) { ChannelLoadingError.NOT_FOUND }
    ) {
        _channelApi.fetchChannel(channelId).toDomain()
    }

    override suspend fun removeChannel(channelId: Long): Result<Unit, Error> = request (
        httpExceptionHandler(403) { DeleteChannelError.FORBIDDEN },
        httpExceptionHandler(404) { DeleteChannelError.CHANNEL_NOT_EXISTS }
    ) {
        _channelApi.removeChannel(channelId)
    }
}