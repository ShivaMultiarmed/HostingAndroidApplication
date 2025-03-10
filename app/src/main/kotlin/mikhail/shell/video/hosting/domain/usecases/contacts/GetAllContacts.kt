package mikhail.shell.video.hosting.domain.usecases.contacts

import mikhail.shell.video.hosting.domain.models.Contact
import mikhail.shell.video.hosting.domain.providers.ContactsProvider
import javax.inject.Inject

class GetAllContacts @Inject constructor(
    private val contactsProvider: ContactsProvider
) {
    suspend operator fun invoke(): List<Contact> {
        return contactsProvider.getAll()
    }
}