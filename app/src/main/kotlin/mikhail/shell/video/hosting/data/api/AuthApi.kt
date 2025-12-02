package mikhail.shell.video.hosting.data.api

import mikhail.shell.video.hosting.data.dto.UserCreationRequest
import mikhail.shell.video.hosting.domain.models.AuthModel
import mikhail.shell.video.hosting.domain.usecases.user.validation.UserNameCheckPurpose
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query

interface AuthApi {
    @POST("auth/signin/password")
    @FormUrlEncoded
    suspend fun signInWithPassword(
        @Field("user_name") email: String,
        @Field("password") password: String
    ): AuthModel
    @POST("auth/signup/password/request")
    @FormUrlEncoded
    suspend fun requestSignUpWithPassword(@Field("user_name") userName: String)
    @POST("auth/signup/password/verification")
    @FormUrlEncoded
    suspend fun verifySignUpWithPassword(
        @Field("user_name") userName: String,
        @Field("code") code: String
    ): String
    @POST("auth/signup/password/confirm")
    suspend fun confirmSignUpWithPassword(
        @Header("Authorization") token: String,
        @Body user: UserCreationRequest
    ): AuthModel
    @POST("auth/signout")
    suspend fun signOut()
    @POST("auth/reset/password/request")
    @FormUrlEncoded
    suspend fun requestResetPassword(@Field("user_name") userName: String): Long
    @POST("auth/reset/password/verification")
    @FormUrlEncoded
    suspend fun verifyResetPassword(
        @Field("user_id") userId: Long,
        @Field("code") code: String
    ): String
    @POST("auth/reset/password/confirmation")
    @FormUrlEncoded
    suspend fun confirmResetPassword(
        @Header("Authorization") token: String,
        @Field("password") password: String
    ): AuthModel
    @GET("auth/existence")
    suspend fun checkUserName(
        @Query("purpose") purpose: UserNameCheckPurpose,
        @Query("user_name") userName: String
    )
}