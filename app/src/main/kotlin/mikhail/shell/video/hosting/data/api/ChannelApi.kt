package mikhail.shell.video.hosting.data.api

import mikhail.shell.video.hosting.data.dto.ChannelDto
import mikhail.shell.video.hosting.data.dto.ChannelWithUserDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ChannelApi {
    @GET("channels/{channelId}/details")
    suspend fun fetchChannelDetails(
        @Path("channelId") channelId: Long,
        @Query("userId") userId: Long
    ): ChannelWithUserDto
    @POST("channels/create")
    suspend fun createChannel(
        @Body channelDto: ChannelDto
    ): ChannelDto
}