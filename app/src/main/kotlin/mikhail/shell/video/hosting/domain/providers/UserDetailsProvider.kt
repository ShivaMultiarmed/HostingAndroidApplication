package mikhail.shell.video.hosting.domain.providers

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class UserDetailsProvider @Inject constructor(
    @ApplicationContext private val appContext: Context
) {
    private val _sharePref = appContext.getSharedPreferences("user_details", Context.MODE_PRIVATE)
    fun getUserId(): Long {
        return _sharePref.getLong("userId", 0)
    }
    fun getJwt(): String {
        return _sharePref.getString("token", "")?: ""
    }
}