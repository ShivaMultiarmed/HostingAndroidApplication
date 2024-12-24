package mikhail.shell.video.hosting.data.api

import mikhail.shell.video.hosting.domain.models.AuthModel
import retrofit2.http.POST
import retrofit2.http.Query

interface AuthApi {
    @POST("auth/signin/password")
    suspend fun authWithEmailAndPassword(
        @Query("username") email: String,
        @Query("password") password: String
    ): AuthModel
}