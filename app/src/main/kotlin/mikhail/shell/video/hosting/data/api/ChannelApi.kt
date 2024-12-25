package mikhail.shell.video.hosting.data.api

import mikhail.shell.video.hosting.data.dto.ChannelDto
import mikhail.shell.video.hosting.data.dto.ChannelWithUserDto
import mikhail.shell.video.hosting.domain.models.ChannelWithUser
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ChannelApi {
    @GET("channels/{channelId}/details")
    suspend fun fetchChannelInfo(
        @Path("channelId") channelId: Long,
        @Query("userId") userId: Long
    ): ChannelWithUserDto
}