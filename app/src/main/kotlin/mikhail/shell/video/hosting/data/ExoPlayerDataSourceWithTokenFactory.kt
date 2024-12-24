package mikhail.shell.video.hosting.data

import android.content.Context
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.HttpDataSource
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory


@UnstableApi
class DSWithTokenFactory(private val token: String) : HttpDataSource.Factory {
    override fun createDataSource(): HttpDataSource {
        val dataSource = DefaultHttpDataSource.Factory().createDataSource()
        dataSource.setRequestProperty("Authorization", "Bearer $token")
        return dataSource
    }

    override fun setDefaultRequestProperties(defaultRequestProperties: MutableMap<String, String>): HttpDataSource.Factory {
        return DefaultHttpDataSource.Factory()
    }
}