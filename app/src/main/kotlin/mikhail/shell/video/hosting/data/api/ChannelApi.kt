package mikhail.shell.video.hosting.data.api

import mikhail.shell.video.hosting.domain.models.ExtendedChannelInfo
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ChannelApi {
    @GET("channels/{channelId}/extended")
    suspend fun fetchExtendedChannelInfo(
        @Path("channelId") channelId: Long,
        @Query("userId") userId: Long
    ): ExtendedChannelInfo
}