package mikhail.shell.video.hosting

import android.app.Application
import coil.Coil
import coil.ImageLoader
import dagger.hilt.android.HiltAndroidApp
import mikhail.shell.video.hosting.data.TokenInterceptor
import mikhail.shell.video.hosting.di.ApiModule
import okhttp3.OkHttpClient
import javax.inject.Inject

@HiltAndroidApp
class HostingApplication: Application() {
    @Inject
    lateinit var httpClient: OkHttpClient
    private val imageLoader: ImageLoader by lazy {
        ImageLoader.Builder(this).okHttpClient(httpClient).build()
    }
    lateinit var CACHE_DIR: String
    override fun onCreate() {
        super.onCreate()
        Coil.setImageLoader(
            imageLoader
        )
        CACHE_DIR = cacheDir.absolutePath
    }
}