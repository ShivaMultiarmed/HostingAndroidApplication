package mikhail.shell.video.hosting.data

import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class TokenInterceptor @Inject constructor(): Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val requestBuilder = originalRequest.newBuilder()
        val token = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIiwiaWF0IjoxNzM1MDEzNDQyLCJleHAiOjE3MzU4Nzc0NDJ9.atuqn9hAgf-MWKskGr4nh-Zk_grmXtPIP6svCxJ-Doc"
        if (!originalRequest.url.toString().contains("auth"))
            requestBuilder.addHeader("Authorization", "Bearer $token")
        return chain.proceed(requestBuilder.build())
    }
}