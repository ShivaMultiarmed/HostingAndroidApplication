package mikhail.shell.video.hosting.data.player

import mikhail.shell.video.hosting.domain.providers.UserDetailsProvider
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class TokenInterceptor @Inject constructor(
    private val provider: UserDetailsProvider
): Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val requestBuilder = originalRequest.newBuilder()
        val token = provider.getJwt()
        if (!originalRequest.url.toString().contains("auth"))
            requestBuilder.addHeader("Authorization", "Bearer $token")
        return chain.proceed(requestBuilder.build())
    }
}