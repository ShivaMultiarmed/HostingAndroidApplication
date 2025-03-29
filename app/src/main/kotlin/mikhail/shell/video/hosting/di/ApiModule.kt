package mikhail.shell.video.hosting.di

import android.content.Context
import com.google.firebase.Firebase
import com.google.firebase.messaging.messaging
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.datetime.Instant
import mikhail.shell.video.hosting.BuildConfig
import mikhail.shell.video.hosting.data.api.AuthApi
import mikhail.shell.video.hosting.data.api.ChannelApi
import mikhail.shell.video.hosting.data.api.CommentApi
import mikhail.shell.video.hosting.data.api.UserApi
import mikhail.shell.video.hosting.data.api.VideoApi
import mikhail.shell.video.hosting.data.converters.InstantConverter
import mikhail.shell.video.hosting.data.converters.LocalDateTimeDeserializer
import mikhail.shell.video.hosting.data.player.TokenInterceptor
import mikhail.shell.video.hosting.data.providers.AndroidContactsProvider
import mikhail.shell.video.hosting.data.providers.AndroidFileProvider
import mikhail.shell.video.hosting.domain.providers.ContactsProvider
import mikhail.shell.video.hosting.domain.providers.FileProvider
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.time.LocalDateTime
import javax.inject.Singleton
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager


@Module
@InstallIn(SingletonComponent::class)
object ApiModule {
    @Provides
    @Singleton
    fun provideAndroidFileProvider(
        @ApplicationContext appContext: Context
    ): FileProvider = AndroidFileProvider(appContext)

    @Provides
    @Singleton
    fun provideAndroidContactsProvider(
        @ApplicationContext appContext: Context
    ): ContactsProvider = AndroidContactsProvider(appContext)

    @Provides
    @Singleton
    fun provideHttpClient(
        tokenInterceptor: TokenInterceptor
    ) = OkHttpClient.Builder()
        .addInterceptor(tokenInterceptor)
        .addInterceptor(
            HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }
        ).apply {
            if (BuildConfig.DEBUG) {
                val trustManager = object : X509TrustManager {
                    override fun checkClientTrusted(
                        chain: Array<X509Certificate>,
                        authType: String
                    ) {}

                    override fun checkServerTrusted(
                        chain: Array<X509Certificate>,
                        authType: String
                    ) {}

                    override fun getAcceptedIssuers(): Array<X509Certificate> {
                        return arrayOf()
                    }
                }
                val sslContext = SSLContext.getInstance("SSL")
                sslContext.init(null, arrayOf<TrustManager>(trustManager), SecureRandom())
                this.sslSocketFactory(sslContext.socketFactory, trustManager)
                this.hostnameVerifier { hostname, session -> true }
            }
        }
        .build()

    @Provides
    @Singleton
    fun provideGson() = GsonBuilder()
        .registerTypeAdapter(LocalDateTime::class.java, LocalDateTimeDeserializer())
        .registerTypeAdapter(Instant::class.java, InstantConverter())
        .create()

    @Provides
    @Singleton
    fun provideGsonConverterFactory(gson: Gson) = GsonConverterFactory.create(gson)

    @Provides
    @Singleton
    fun provideRetrofit(
        httpClient: OkHttpClient,
        converterFactory: GsonConverterFactory
    ) = Retrofit.Builder()
        .client(httpClient)
        .baseUrl(BuildConfig.API_BASE_URL)
        .addConverterFactory(converterFactory)
        .build()

    @Provides
    @Singleton
    fun provideVideoApi(
        retrofit: Retrofit
    ) = retrofit.create<VideoApi>()

    @Provides
    @Singleton
    fun provideChannelApi(
        retrofit: Retrofit
    ) = retrofit.create<ChannelApi>()

    @Provides
    @Singleton
    fun provideAuthApi(
        retrofit: Retrofit
    ) = retrofit.create<AuthApi>()

    @Provides
    @Singleton
    fun provideCommentApi(
        retrofit: Retrofit
    ) = retrofit.create<CommentApi>()

    @Provides
    @Singleton
    fun provideUserApi(
        retrofit: Retrofit
    ) = retrofit.create<UserApi>()

    @Provides
    @Singleton
    fun provideFCM() = Firebase.messaging
}