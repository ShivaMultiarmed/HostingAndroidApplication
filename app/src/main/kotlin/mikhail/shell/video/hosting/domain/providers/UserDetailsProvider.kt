package mikhail.shell.video.hosting.domain.providers

interface UserDetailsProvider {
    fun getUserId(): Long
    fun getJwt(): String
}