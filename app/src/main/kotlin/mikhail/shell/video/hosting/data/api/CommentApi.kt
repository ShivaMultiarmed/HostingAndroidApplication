package mikhail.shell.video.hosting.data.api

import mikhail.shell.video.hosting.data.dto.CommentWithUserDto
import mikhail.shell.video.hosting.data.repositories.CommentCreationRequest
import mikhail.shell.video.hosting.data.repositories.CommentEditingRequest
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query
import kotlin.time.Instant

interface CommentApi {
    @POST("comments")
    suspend fun save(@Body comment: CommentCreationRequest): CommentWithUserDto
    @GET("comments/videos/{video_id}")
    suspend fun fetch(
        @Path("video_id") videoId: Long,
        @Query("before") before: Instant?,
        @Query("part_size") partSize: Int
    ): List<CommentWithUserDto>
    @DELETE("comments/{comment_id}")
    suspend fun remove(@Path("comment_id") commentId: Long)
    @PUT("comments")
    suspend fun edit(@Body comment: CommentEditingRequest): CommentWithUserDto
}