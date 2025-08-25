package mikhail.shell.video.hosting.data.api

import mikhail.shell.video.hosting.data.dto.UserDto
import mikhail.shell.video.hosting.data.repositories.UserEditingRequest
import okhttp3.MultipartBody
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.Part
import retrofit2.http.Path

interface UserApi {
    @GET("users/{userId}")
    suspend fun get(@Path("userId") userId: Long): UserDto
    @PATCH("users")
    @Multipart
    suspend fun edit(
        @Part("user") request: UserEditingRequest,
        @Part avatar: MultipartBody.Part?
    ): UserDto
    @DELETE("users")
    suspend fun remove()
}