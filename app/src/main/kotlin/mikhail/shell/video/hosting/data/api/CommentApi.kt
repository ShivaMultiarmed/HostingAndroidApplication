package mikhail.shell.video.hosting.data.api

import mikhail.shell.video.hosting.data.dto.CommentDto
import mikhail.shell.video.hosting.data.dto.CommentWithUserDto
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import kotlin.time.Instant

interface CommentApi {
    @POST("comments")
    suspend fun save(@Body comment: CommentDto)
    @GET("comments/videos/{video_id}")
    suspend fun fetch(
        @Path("video_id") videoId: Long,
        @Query("before") before: Instant
    ): List<CommentWithUserDto>
    @DELETE("comments/{comment_id}")
    suspend fun remove(@Path("comment_id") commentId: Long)
}