package mikhail.shell.video.hosting.data.providers

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import mikhail.shell.video.hosting.domain.providers.UserDetailsProvider
import javax.inject.Inject

class AndroidUserDetailsProvider @Inject constructor(
    @ApplicationContext private val appContext: Context
): UserDetailsProvider {
    private val _sharePref = appContext.getSharedPreferences("user_details", Context.MODE_PRIVATE)
    override fun getUserId(): Long {
        return _sharePref.getLong("userId", 0)
    }
    override fun getJwt(): String {
        return _sharePref.getString("token", "")?: ""
    }
}