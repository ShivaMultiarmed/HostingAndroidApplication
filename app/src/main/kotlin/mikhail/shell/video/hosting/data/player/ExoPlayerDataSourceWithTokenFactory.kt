package mikhail.shell.video.hosting.data.player

import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.HttpDataSource
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
        return DefaultHttpDataSource.Factory()
    }
}