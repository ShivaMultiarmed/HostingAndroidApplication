package mikhail.shell.video.hosting.data.api

import mikhail.shell.video.hosting.data.dto.ChannelDto
import mikhail.shell.video.hosting.data.dto.ChannelWithUserDto
import okhttp3.MultipartBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
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
    @GET("channels/owner/{userId}")
    suspend fun getChannelsByOwner(
        @Path("userId") userId: Long
    ): List<ChannelDto>
    @GET("channels/subscriber/{userId}")
    suspend fun getChannelsBySubscriber(
        @Path("userId") userId: Long
    ): List<ChannelDto>
}