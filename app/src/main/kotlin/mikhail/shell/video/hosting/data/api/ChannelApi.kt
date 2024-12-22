package mikhail.shell.video.hosting.data.api

import mikhail.shell.video.hosting.data.dto.ChannelDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ChannelApi {
    @GET("channels/{channelId}")
    suspend fun fetchChannelInfo(
        @Path("channelId") channelId: Long,
        @Query("userId") userId: Long
    ): ChannelDto
}