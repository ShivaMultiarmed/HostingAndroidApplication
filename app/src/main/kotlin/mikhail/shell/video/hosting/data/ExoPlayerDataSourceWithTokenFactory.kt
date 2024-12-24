package mikhail.shell.video.hosting.data

import android.content.Context
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.HttpDataSource
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import mikhail.shell.video.hosting.domain.providers.UserDetailsProvider
import javax.inject.Inject

@UnstableApi
class DSWithTokenFactory @Inject constructor(
    private val provider: UserDetailsProvider
) : HttpDataSource.Factory {
    override fun createDataSource(): HttpDataSource {
        val dataSource = DefaultHttpDataSource.Factory().createDataSource()
        dataSource.setRequestProperty("Authorization", "Bearer ${provider.getJwt()}")
        return dataSource
    }

    override fun setDefaultRequestProperties(defaultRequestProperties: MutableMap<String, String>): HttpDataSource.Factory {
//        provider.getJwt().let { jwt ->
//            defaultRequestProperties["Authorization"] = "Bearer $jwt"
//        }
//        return this
        return DefaultHttpDataSource.Factory()
    }
}