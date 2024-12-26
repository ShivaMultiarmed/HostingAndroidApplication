package mikhail.shell.video.hosting.data.api

import mikhail.shell.video.hosting.data.dto.VideoDto
import mikhail.shell.video.hosting.data.dto.VideoDetailsDto
import mikhail.shell.video.hosting.data.dto.VideoWithChannelDto
import mikhail.shell.video.hosting.domain.models.LikingState
import okhttp3.MultipartBody
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface VideoApi {
    @GET("videos/{videoId}")
    suspend fun fetchVideo(
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
    suspend fun fetchVideoList(
        @Path("channelId") channelId: Long,
        @Query("userId") userId: Long,
        @Query("partNumber") partNumber: Long,
        @Query("partSize") partSize: Int
    ): List<VideoDto>
    @GET("videos/search")
    suspend fun fetchVideoListByQuery(
        @Query("query") query: String,
        @Query("partNumber") partNumber: Long,
        @Query("partSize") partSize: Int
    ): List<VideoWithChannelDto>
    @Multipart
    @POST("videos/upload")
    suspend fun uploadVideo(
        @Part("video") video: VideoDto,
        @Part source: MultipartBody.Part,
        @Part cover: MultipartBody.Part?
    ): VideoDto
}