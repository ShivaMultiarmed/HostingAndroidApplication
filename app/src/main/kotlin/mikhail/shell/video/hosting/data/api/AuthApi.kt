package mikhail.shell.video.hosting.data.api

import mikhail.shell.video.hosting.data.dto.SignUpDto
import mikhail.shell.video.hosting.domain.models.AuthModel
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query

interface AuthApi {
    @POST("auth/signin/password")
    suspend fun signInWithPassword(
        @Query("username") email: String,
        @Query("password") password: String
    ): AuthModel
    @POST("auth/signup/password")
    suspend fun signUpWithPassword(
        @Body signUpDto: SignUpDto
    ): AuthModel
}