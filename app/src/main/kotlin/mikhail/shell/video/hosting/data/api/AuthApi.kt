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
    @POST("auth/signup/password/request")
    suspend fun requestSignUpWithPassword(@Query("userName") userName: String)
    @POST("auth/signup/password/verify")
    suspend fun verifySignUpWithPassword(
        @Query("userName") userName: String,
        @Query("code") code: String
    ): String
    @POST("auth/signup/password/confirm")
    suspend fun confirmSignUpWithPassword(@Body signUpDto: SignUpDto): AuthModel
    @POST("auth/signout")
    suspend fun signOut()
}