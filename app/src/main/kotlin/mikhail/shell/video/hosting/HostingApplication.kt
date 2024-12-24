package mikhail.shell.video.hosting

import android.app.Application
import coil.Coil
import coil.ImageLoader
import dagger.hilt.android.HiltAndroidApp
import mikhail.shell.video.hosting.data.TokenInterceptor
import mikhail.shell.video.hosting.di.ApiModule

@HiltAndroidApp
class HostingApplication: Application() {
    override fun onCreate() {
        super.onCreate()
        Coil.setImageLoader(
            ImageLoader.Builder(this).okHttpClient(
                ApiModule.provideHttpClient(TokenInterceptor())
            ).build()
        )
    }
}