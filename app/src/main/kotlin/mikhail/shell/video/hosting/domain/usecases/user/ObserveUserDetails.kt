package mikhail.shell.video.hosting.domain.usecases.user

import kotlinx.coroutines.flow.StateFlow
import mikhail.shell.video.hosting.domain.providers.UserDetails
import mikhail.shell.video.hosting.domain.providers.UserDetailsProvider
import javax.inject.Inject

class ObserveUserDetails @Inject constructor(
    private val userDetailsProvider: UserDetailsProvider
) {
    operator fun invoke(): StateFlow<UserDetails> {
        return userDetailsProvider.userDetails
    }
}