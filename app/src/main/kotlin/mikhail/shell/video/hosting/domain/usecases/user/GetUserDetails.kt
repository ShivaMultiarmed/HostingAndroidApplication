package mikhail.shell.video.hosting.domain.usecases.user

import mikhail.shell.video.hosting.domain.providers.UserDetails
import mikhail.shell.video.hosting.domain.providers.UserDetailsProvider
import javax.inject.Inject

class GetUserDetails @Inject constructor(
    private val userDetailsProvider: UserDetailsProvider
) {
    operator fun invoke(): UserDetails {
        return userDetailsProvider.get()
    }
}