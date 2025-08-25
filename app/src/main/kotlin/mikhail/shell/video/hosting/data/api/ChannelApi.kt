package mikhail.shell.video.hosting.data.api

import mikhail.shell.video.hosting.data.dto.ChannelDto
import mikhail.shell.video.hosting.data.dto.ChannelWithUserDto
import mikhail.shell.video.hosting.data.repositories.ChannelCreationRequest
import mikhail.shell.video.hosting.data.repositories.ChannelEditingRequest
import mikhail.shell.video.hosting.domain.models.Subscription
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
        @Path("channelId") channelId: Long
    ): ChannelWithUserDto
    @Multipart
    @POST("channels")
    suspend fun createChannel(
        @Part("channel") channel: ChannelCreationRequest,
        @Part logo: MultipartBody.Part?,
        @Part header: MultipartBody.Part?
    ): ChannelDto
    @Multipart
    @PATCH("channels")
    suspend fun editChannel(
        @Part("channel") channel: ChannelEditingRequest,
        @Part avatar: MultipartBody.Part?,
        @Part cover: MultipartBody.Part?
    ): ChannelDto
    @GET("channels/owner/{userId}")
    suspend fun getChannelsByOwner(
        @Path("userId") userId: Long,
        @Query("partIndex") partIndex: Long,
        @Query("partSize") partSize: Int
    ): List<ChannelDto>
    @GET("channels/subscriptions")
    suspend fun getSubscriptions(
        partIndex: Long,
        partSize: Int
    ): List<ChannelDto>
    @PATCH("channels/{channelId}/subscription")
    suspend fun subscribe(
        @Path("channelId") channelId: Long,
        @Query("subscription") subscription: Subscription,
        @Query("fcmToken") fcmToken: String
    ): ChannelWithUserDto
    @POST("channels/notifications/subscription")
    suspend fun subscribeToChannelNotifications(
        @Query("fcmToken") fcmToken: String
    )
    @DELETE("channels/notifications/subscription")
    suspend fun unsubscribeFromChannelNotifications(
        @Query("fcmToken") fcmToken: String
    )
    @GET("channels/{channelId}")
    suspend fun fetchChannel(
        @Path("channelId") channelId: Long
    ): ChannelDto
    @DELETE("channels/{channelId}")
    suspend fun removeChannel(
        @Path("channelId") channelId: Long
    )
    @GET("channels/existence/title")
    fun existsByTitle(@Query("title") title: String): Boolean
    @GET("channels/existence/alias")
    fun existsByAlias(@Query("alias") alias: String): Boolean
}