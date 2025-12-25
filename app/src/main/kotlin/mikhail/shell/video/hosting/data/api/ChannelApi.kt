package mikhail.shell.video.hosting.data.api

import mikhail.shell.video.hosting.data.dto.ChannelDto
import mikhail.shell.video.hosting.data.dto.ChannelWithUserDto
import mikhail.shell.video.hosting.data.repositories.ChannelCreationRequest
import mikhail.shell.video.hosting.data.repositories.ChannelEditingRequest
import okhttp3.MultipartBody
import retrofit2.http.DELETE
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface ChannelApi {
    @GET("channels/{channel_id}/details")
    suspend fun fetchChannelDetails(@Path("channel_id") channelId: Long): ChannelWithUserDto
    @Multipart
    @POST("channels")
    suspend fun createChannel(
        @Part("channel") channel: ChannelCreationRequest,
        @Part logo: MultipartBody.Part?,
        @Part header: MultipartBody.Part?
    ): ChannelDto
    @Multipart
    @PUT("channels")
    suspend fun editChannel(
        @Part("channel") channel: ChannelEditingRequest,
        @Part logo: MultipartBody.Part?,
        @Part header: MultipartBody.Part?
    ): ChannelDto
    @GET("channels/owner/{user_id}")
    suspend fun getChannelsByOwner(
        @Path("user_id") userId: Long,
        @Query("part_index") partIndex: Int,
        @Query("part_size") partSize: Int
    ): List<ChannelDto>
    @GET("channels/subscriptions")
    suspend fun getSubscriptions(
        @Query("part_index") partIndex: Long,
        @Query("part_size") partSize: Int
    ): List<ChannelDto>
    @PATCH("channels/{channel_id}/subscription")
    @FormUrlEncoded
    suspend fun subscribe(
        @Path("channel_id") channelId: Long,
        @Field("subscription") subscription: String,
        @Header("Messaging-Token") messagingToken: String
    ): ChannelWithUserDto
    @GET("channels/{channel_id}")
    suspend fun fetchChannel(@Path("channel_id") channelId: Long): ChannelDto
    @DELETE("channels/{channel_id}")
    suspend fun removeChannel(@Path("channel_id") channelId: Long)
    @GET("channels/existence")
    suspend fun existsByTitle(
        @Query("channel_id") channelId: Long?,
        @Query("title") title: String
    )
    @GET("channels/existence")
    suspend fun existsByAlias(
        @Query("channel_id") channelId: Long?,
        @Query("alias") alias: String
    )
}