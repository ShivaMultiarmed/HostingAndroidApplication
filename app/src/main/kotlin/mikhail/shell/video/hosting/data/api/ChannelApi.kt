package mikhail.shell.video.hosting.data.api

import mikhail.shell.video.hosting.data.dto.ChannelDto
import mikhail.shell.video.hosting.data.dto.ChannelWithUserDto
import mikhail.shell.video.hosting.domain.models.EditAction
import mikhail.shell.video.hosting.domain.models.SubscriptionState
import okhttp3.MultipartBody
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface ChannelApi {
    @GET("channels/{channelId}/details")
    suspend fun fetchChannelDetails(
        @Path("channelId") channelId: Long,
        @Query("userId") userId: Long
    ): ChannelWithUserDto
    @Multipart
    @POST("channels/create")
    suspend fun createChannel(
        @Part("channel") channelDto: ChannelDto,
        @Part avatar: MultipartBody.Part?,
        @Part cover: MultipartBody.Part?
    ): ChannelDto
    @Multipart
    @PATCH("channels/edit")
    suspend fun editChannel(
        @Part("channel") channelDto: ChannelDto,
        @Part avatar: MultipartBody.Part?,
        @Part cover: MultipartBody.Part?,
        @Part("editCoverAction") editCoverAction: EditAction,
        @Part("editAvatarAction") editAvatarAction: EditAction
    ): ChannelDto
    @GET("channels/owner/{userId}")
    suspend fun getChannelsByOwner(
        @Path("userId") userId: Long
    ): List<ChannelDto>
    @GET("channels/subscriber/{userId}")
    suspend fun getChannelsBySubscriber(
        @Path("userId") userId: Long
    ): List<ChannelDto>
    @PATCH("channels/{channelId}/subscribe")
    suspend fun subscribe(
        @Path("channelId") channelId: Long,
        @Query("userId") userId: Long,
        @Query("token") token: String,
        @Query("subscriptionState") subscriptionState: SubscriptionState
    ): ChannelWithUserDto
    @PATCH("channels/notifications/subscribe")
    suspend fun subscribeToChannelNotifications(
        @Query("userId") userId: Long,
        @Query("token") token: String
    )
    @PATCH("channels/notifications/unsubscribe")
    suspend fun unsubscribeFromChannelNotifications(
        @Query("userId") userId: Long,
        @Query("token") token: String
    )
    @GET("channels/{channelId}")
    suspend fun fetchChannel(
        @Path("channelId") channelId: Long
    ): ChannelDto
    @DELETE("channels/{channelId}")
    suspend fun removeChannel(
        @Path("channelId") channelId: Long
    )
}