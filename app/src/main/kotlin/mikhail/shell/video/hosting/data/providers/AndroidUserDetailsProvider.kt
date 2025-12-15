package mikhail.shell.video.hosting.data.providers

import android.content.Context
import androidx.datastore.core.Serializer
import androidx.datastore.dataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.serialization.json.Json
import mikhail.shell.video.hosting.data.utils.CryptoUtils
import mikhail.shell.video.hosting.domain.providers.UserDetails
import mikhail.shell.video.hosting.domain.providers.UserDetailsProvider
import java.io.InputStream
import java.io.OutputStream
import javax.inject.Inject


class AndroidUserDetailsProvider @Inject constructor(
    @param:ApplicationContext private val appContext: Context,
) : UserDetailsProvider {
    private val coroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private val dataStore = appContext.userDetailsDataStore
    override val userDetails = dataStore.data.stateIn(
        scope = coroutineScope,
        started = SharingStarted.Eagerly,
        initialValue = UserDetails()
    )

    override fun get() = userDetails.value

    override fun getUserId() = get().userId

    override fun getJwt() = get().token

    override suspend fun save(userDetails: UserDetails) {
        dataStore.updateData {
            userDetails
        }
    }

    override suspend fun remove() {
        dataStore.updateData {
            UserDetails()
        }
    }
}

val Context.userDetailsDataStore by dataStore("user_details_ds.json", UserDetailsSerializer())

class UserDetailsSerializer : Serializer<UserDetails> {
    override val defaultValue = UserDetails()

    override suspend fun readFrom(input: InputStream): UserDetails {
        return input.use {
            it
                .readBytes()
                .decodeToString()
                .let { json ->
                    val ud = Json.decodeFromString(
                        deserializer = UserDetails.serializer(),
                        string = json
                    )
                    ud.copy(token = CryptoUtils.decrypt(ud.token))
                }
        }
    }

    override suspend fun writeTo(t: UserDetails, output: OutputStream) {
        Json
            .encodeToString(
                serializer = UserDetails.serializer(),
                value = t.copy(token = CryptoUtils.encrypt(t.token))
            )
            .encodeToByteArray()
            .let { bytes ->
                output.use {
                    it.write(bytes)
                }
            }
    }
}