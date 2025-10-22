package mikhail.shell.video.hosting.data.api

import mikhail.shell.video.hosting.data.dto.VideoDetailsDto
import mikhail.shell.video.hosting.data.dto.VideoDto
import mikhail.shell.video.hosting.data.dto.VideoWithChannelDto
import mikhail.shell.video.hosting.data.dto.VideoWithUserDto
import mikhail.shell.video.hosting.data.repositories.VideoEditingRequest
import mikhail.shell.video.hosting.data.repositories.VideoMetaData
import mikhail.shell.video.hosting.data.repositories.VideoUploadingRequest
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
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

interface VideoApi {
    @GET("videos/{video_id}")
    suspend fun fetchVideo(@Path("video_id") videoId: Long) : VideoDto
    @GET("videos/{video_id}/details")
    suspend fun fetchVideoDetails(@Path("video_id") videoId: Long) : VideoDetailsDto
    @PATCH("videos/{video_id}/rate")
    suspend fun rateVideo(
        @Path("video_id") videoId: Long,
        @Query("liking") liking: String
    ): VideoWithUserDto
    @GET("videos/channel/{channel_id}")
    suspend fun fetchVideoList(
        @Path("channel_id") channelId: Long,
        @Query("part_number") partNumber: Long,
        @Query("part_size") partSize: Int
    ): List<VideoDto>
    @GET("videos/search")
    suspend fun fetchVideoListByQuery(
        @Query("query") query: String,
        @Query("part_number") partNumber: Long?,
        @Query("part_size") partSize: Int
    ): List<VideoWithChannelDto>
    @Multipart
    @POST("videos")
    suspend fun uploadVideoDetails(
        @Part("video") video: VideoUploadingRequest,
        @Part("source") source: VideoMetaData,
        @Part cover: MultipartBody.Part?
    ): String
    @OptIn(ExperimentalUuidApi::class)
    @POST("videos/{upload_id}")
    suspend fun uploadVideoSource(
        @Path("upload_id") uploadId: Uuid,
        @Header("Content-Range") contentRange: String,
        @Body source: RequestBody
    )
    @OptIn(ExperimentalUuidApi::class)
    @POST("videos/{upload_id}/confirmation")
    suspend fun confirmVideoUpload(@Path("upload_id") uploadId: Uuid): VideoDto
    @PATCH("videos/{video_id}/views")
    suspend fun incrementViews(@Path("video_id") videoId: Long): VideoDto
    @Multipart
    @PUT("videos")
    suspend fun editVideo(
        @Part("video") video: VideoEditingRequest,
        @Part cover: MultipartBody.Part?
    ): VideoDto
    @DELETE("videos/{video_id}")
    suspend fun deleteVideo(@Path("video_id") videoId: Long)
    @GET("videos/{video_id}/source")
    suspend fun downloadVideo(
        @Path("video_id") videoId: Long,
        @Header("Range") byteRange: String
    ): Response<ResponseBody>
    @GET("videos/recommendations")
    suspend fun fetchVideoRecommendationsPart(
        @Query("part_index") partIndex: Long,
        @Query("part_size") partSize: Int
    ): List<VideoWithChannelDto>
}