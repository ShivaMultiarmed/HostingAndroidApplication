package mikhail.shell.video.hosting.data.api

import mikhail.shell.video.hosting.domain.errors.VideoError
import mikhail.shell.video.hosting.domain.models.ExtendedVideoInfo
import mikhail.shell.video.hosting.domain.models.VideoDetails
import mikhail.shell.video.hosting.domain.models.VideoInfo
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.Path
import retrofit2.http.Query

interface VideoApi {
    @GET("videos/{videoId}")
    suspend fun fetchVideoInfo(
        @Path("videoId") videoId: Long
    ) : VideoInfo
    @GET("videos/{videoId}/extended")
    suspend fun fetchExtendedVideoInfo(
        @Path("videoId") videoId: Long,
        @Query("userId") userId: Long
    ) : VideoDetails
    @PATCH("videos/{videoId}/rate")
    suspend fun rateVideo(
        @Path("videoId") videoId: Long,
        @Query("userId") userId: Long,
        @Query("liking") liking: Boolean
    ) : ExtendedVideoInfo
    @GET("videos/channel/{channelId}")
    suspend fun fetchVideoDetailsList(
        @Path("channelId") channelId: Long,
        @Query("userId") userId: Long,
        @Query("partNumber") partNumber: Long,
        @Query("partSize") partSize: Int
    ): List<VideoInfo>
}