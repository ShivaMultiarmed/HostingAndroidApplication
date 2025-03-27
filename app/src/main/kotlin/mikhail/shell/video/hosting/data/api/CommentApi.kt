package mikhail.shell.video.hosting.data.api

import kotlinx.datetime.Instant
import mikhail.shell.video.hosting.data.dto.CommentDto
import mikhail.shell.video.hosting.data.dto.CommentWithUserDto
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface CommentApi {
    @POST("comments/save")
    suspend fun save(@Body comment: CommentDto)
    @GET("comments/videos/{videoId}")
    suspend fun fetch(
        @Path("videoId") videoId: Long,
        @Query("before") before: Instant
    ): List<CommentWithUserDto>
    @DELETE("comments/remove")
    suspend fun remove(@Query("videoId") videoId: Long)
}