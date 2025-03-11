package mikhail.shell.video.hosting.domain.providers

import mikhail.shell.video.hosting.domain.models.Contact

interface ContactsProvider {
    fun search(query: String): List<Contact>
    fun invite(number: String)
}