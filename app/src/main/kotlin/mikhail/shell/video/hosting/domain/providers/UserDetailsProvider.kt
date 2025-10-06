package mikhail.shell.video.hosting.domain.providers

import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable

@Serializable
data class UserDetails(
    val userId: Long = 0,
    val token: String = ""
)

interface UserDetailsProvider {
    val userDetails: StateFlow<UserDetails>
    fun get(): UserDetails
    fun getUserId(): Long
    fun getJwt(): String
    suspend fun save(userDetails: UserDetails)
    suspend fun remove()
}