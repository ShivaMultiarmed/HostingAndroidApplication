package mikhail.shell.video.hosting.data.api

import mikhail.shell.video.hosting.data.dto.VideoDto
import mikhail.shell.video.hosting.data.dto.VideoDetailsDto
import mikhail.shell.video.hosting.domain.models.LikingState
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.Path
import retrofit2.http.Query

interface VideoApi {
    @GET("videos/{videoId}")
    suspend fun fetchVideoDto(
        @Path("videoId") videoId: Long
    ) : VideoDto
    @GET("videos/{videoId}/details")
    suspend fun fetchVideoDetails(
        @Path("videoId") videoId: Long,
        @Query("userId") userId: Long
    ) : VideoDetailsDto
    @PATCH("videos/{videoId}/rate")
    suspend fun rateVideo(
        @Path("videoId") videoId: Long,
        @Query("userId") userId: Long,
        @Query("likingState") liking: LikingState
    ) : VideoDto
    @GET("videos/channel/{channelId}")
    suspend fun fetchVideoDetailsList(
        @Path("channelId") channelId: Long,
        @Query("userId") userId: Long,
        @Query("partNumber") partNumber: Long,
        @Query("partSize") partSize: Int
    ): List<VideoDto>
}