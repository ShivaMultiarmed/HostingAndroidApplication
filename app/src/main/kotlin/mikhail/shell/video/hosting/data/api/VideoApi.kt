package mikhail.shell.video.hosting.data.api

import mikhail.shell.video.hosting.domain.models.ExtendedVideoInfo
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
    ) : ExtendedVideoInfo
    @PATCH("videos/{videoId}/rate")
    suspend fun rateVideo(
        @Path("videoId") videoId: Long,
        @Query("userId") userId: Long,
        @Query("liking") liking: Boolean
    ) : ExtendedVideoInfo
}