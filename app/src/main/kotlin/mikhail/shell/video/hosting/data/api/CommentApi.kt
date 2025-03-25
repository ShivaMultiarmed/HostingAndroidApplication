package mikhail.shell.video.hosting.data.api

import mikhail.shell.video.hosting.data.dto.CommentDto
import mikhail.shell.video.hosting.data.dto.CommentWithUserDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query
import java.time.LocalDateTime

interface CommentApi {
    @POST("comments/post")
    suspend fun postComment(
        @Body comment: CommentDto
    )
    @GET("comments")
    suspend fun fetchComments(
        @Query("before") before: LocalDateTime,
        @Query("videoId") videoId: Long
    ): List<CommentWithUserDto>
}