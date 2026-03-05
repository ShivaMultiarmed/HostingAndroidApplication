package mikhail.shell.video.hosting.data.api

import mikhail.shell.video.hosting.data.dto.UserDto
import mikhail.shell.video.hosting.data.repositories.UserEditingRequest
import mikhail.shell.video.hosting.domain.models.NickCheckPurpose
import okhttp3.MultipartBody
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface UserApi {
    @GET("users/{user_id}")
    suspend fun get(@Path("user_id") userId: Long): UserDto
    @PUT("users")
    @Multipart
    suspend fun edit(
        @Part("user") user: UserEditingRequest,
        @Part avatar: MultipartBody.Part?
    ): UserDto
    @DELETE("users")
    suspend fun remove()
    @GET("users/existence")
    suspend fun existsByNick(
        @Query("purpose") purpose: NickCheckPurpose,
        @Query("nick") nick: String
    )
    @POST("users/notifications/subscription")
    suspend fun subscribeToNotifications(@Header("Messaging-Token") messagingToken: String)
    @DELETE("users/notifications/subscription")
    suspend fun unsubscribeFromNotifications(@Header("Messaging-Token") messagingToken: String)
}