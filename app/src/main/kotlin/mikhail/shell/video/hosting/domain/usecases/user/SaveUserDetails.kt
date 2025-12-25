package mikhail.shell.video.hosting.domain.usecases.user

import mikhail.shell.video.hosting.domain.providers.UserDetails
import mikhail.shell.video.hosting.domain.providers.UserDetailsProvider
import javax.inject.Inject

class SaveUserDetails @Inject constructor(
    private val userDetailsProvider: UserDetailsProvider
){
    suspend operator fun invoke(details: UserDetails) {
        userDetailsProvider.save(details)
    }
}