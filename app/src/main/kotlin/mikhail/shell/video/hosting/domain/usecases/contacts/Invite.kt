package mikhail.shell.video.hosting.domain.usecases.contacts

import mikhail.shell.video.hosting.domain.providers.ContactsProvider
import javax.inject.Inject

class Invite @Inject constructor(
    private val contactsProvider: ContactsProvider
) {
    operator fun invoke(number: String) {
        contactsProvider.invite(number)
    }
}