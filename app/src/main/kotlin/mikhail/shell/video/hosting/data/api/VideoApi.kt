package mikhail.shell.video.hosting.data.api

import mikhail.shell.video.hosting.data.dto.VideoDetailsDto
import mikhail.shell.video.hosting.data.dto.VideoDto
import mikhail.shell.video.hosting.data.dto.VideoWithChannelDto
import mikhail.shell.video.hosting.domain.models.EditAction
import mikhail.shell.video.hosting.domain.models.LikingState
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Streaming

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
    @POST("videos/upload/details")
    suspend fun uploadVideoDetails(
        @Body video: VideoDto
    ): VideoDto
    @POST("videos/upload/{videoId}/cover")
    suspend fun uploadVideoCover(
        @Path("videoId") videoId: Long,
        @Query("extension") extension: String,
        @Body cover: RequestBody
    ): Boolean
    @POST("videos/upload/{videoId}/source")
    suspend fun uploadVideoSource(
        @Path("videoId") videoId: Long,
        @Query("extension") extension: String,
        @Body source: RequestBody
    ): Boolean
    @POST("videos/upload/{videoId}/confirm")
    suspend fun confirmVideoUpload(
        @Path("videoId") videoId: Long
    ): Boolean
    @PATCH("videos/{videoId}/increment-views")
    suspend fun incrementViews(
        @Path("videoId") videoId: Long
    ): Long
    @Multipart
    @PATCH("videos/edit")
    suspend fun editVideo(
        @Part("video") video: VideoDto,
        @Part("coverAction") coverAction: EditAction,
        @Part cover: MultipartBody.Part?
    ): VideoDto
    @DELETE("videos/{videoId}")
    suspend fun deleteVideo(
        @Path("videoId") videoId: Long
    ): Void
    @GET("videos/{videoId}/play")
    @Streaming
    suspend fun playVideo(
        @Path("videoId") videoId: Long,
        @Header("Range") byteRange: String
    ): Response<ResponseBody>
    @GET("videos/{videoId}/download")
    @Streaming
    suspend fun downloadVideo(
        @Path("videoId") videoId: Long,
        @Header("Range") byteRange: String
    ): Response<ResponseBody>
    @GET("videos/recommendations/users/{userId}")
    suspend fun fetchVideoRecommendationsPart(
        @Path("userId") userId: Long,
        @Query("partIndex") partIndex: Long,
        @Query("partSize") partSize: Int
    ): List<VideoWithChannelDto>
}