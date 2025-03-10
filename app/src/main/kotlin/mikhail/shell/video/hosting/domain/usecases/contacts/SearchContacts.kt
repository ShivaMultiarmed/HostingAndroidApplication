package mikhail.shell.video.hosting.domain.usecases.contacts

import mikhail.shell.video.hosting.domain.models.Contact
import mikhail.shell.video.hosting.domain.providers.ContactsProvider
import javax.inject.Inject

class SearchContacts @Inject constructor(
    private val contactsProvider: ContactsProvider
) {
    suspend operator fun invoke(query: String): List<Contact> {
        return contactsProvider.search(query)
    }
}