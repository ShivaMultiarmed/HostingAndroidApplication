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
import mikhail.shell.video.hosting.data.LocalDateTimeDeserializer
import mikhail.shell.video.hosting.data.TokenInterceptor
import mikhail.shell.video.hosting.data.api.AuthApi
import mikhail.shell.video.hosting.data.api.ChannelApi
import mikhail.shell.video.hosting.data.api.VideoApi
import mikhail.shell.video.hosting.domain.providers.AndroidFileProvider
import mikhail.shell.video.hosting.domain.providers.FileProvider
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create
import java.time.LocalDateTime
import javax.inject.Singleton

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
    fun provideHttpClient(
        tokenInterceptor: TokenInterceptor
    ) = OkHttpClient.Builder()
        .addInterceptor(tokenInterceptor)
        .addInterceptor(
            HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }
        )
        .build()
    @Provides
    @Singleton
    fun provideGson() = GsonBuilder()
        .registerTypeAdapter(LocalDateTime::class.java, LocalDateTimeDeserializer())
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
        .baseUrl("http://192.168.1.2:9999/api/v1/")
        //.baseUrl("http://somespace.ru:9999/api/v1/")
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
    fun provideFCM() = Firebase.messaging
}