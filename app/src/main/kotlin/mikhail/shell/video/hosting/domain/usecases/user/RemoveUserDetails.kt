package mikhail.shell.video.hosting.domain.usecases.user

import mikhail.shell.video.hosting.domain.providers.UserDetailsProvider
import javax.inject.Inject

class RemoveUserDetails @Inject constructor(
    private val userDetailsProvider: UserDetailsProvider
) {
    suspend operator fun invoke() {
        userDetailsProvider.remove()
    }
}