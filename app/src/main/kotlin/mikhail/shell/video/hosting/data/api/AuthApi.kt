package mikhail.shell.video.hosting.data.api

import mikhail.shell.video.hosting.data.dto.UserCreationRequest
import mikhail.shell.video.hosting.domain.models.AuthModel
import mikhail.shell.video.hosting.domain.usecases.user.validation.UserNameCheckPurpose
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query

interface AuthApi {
    @POST("auth/signin/password")
    suspend fun signInWithPassword(
        @Query("user_name") email: String,
        @Query("password") password: String
    ): AuthModel
    @POST("auth/signup/password/request")
    suspend fun requestSignUpWithPassword(@Query("user_name") userName: String)
    @POST("auth/signup/password/verification")
    suspend fun verifySignUpWithPassword(
        @Query("user_name") userName: String,
        @Query("code") code: String
    ): String
    @POST("auth/signup/password/confirm")
    suspend fun confirmSignUpWithPassword(
        @Header("Authorization") token: String,
        @Body user: UserCreationRequest
    ): AuthModel
    @POST("auth/signout")
    suspend fun signOut()
    @POST("auth/reset/password/request")
    suspend fun requestResetPassword(@Query("user_name") userName: String)
    @POST("auth/reset/password/verify")
    suspend fun verifyResetPassword(
        @Query("user_name") userName: String,
        @Query("code") code: String
    ): String
    @POST("auth/reset/password/confirm")
    suspend fun confirmResetPassword(
        @Header("Authorization") token: String,
        @Query("password") password: String
    ): AuthModel
    @GET("auth/existence")
    suspend fun checkUserName(
        @Query("purpose") purpose: UserNameCheckPurpose,
        @Query("user_name") userName: String
    )
}