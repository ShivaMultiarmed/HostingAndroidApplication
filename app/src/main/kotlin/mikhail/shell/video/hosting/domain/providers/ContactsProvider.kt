package mikhail.shell.video.hosting.domain.providers

import mikhail.shell.video.hosting.domain.models.Contact

interface ContactsProvider {
    fun getAll(): List<Contact>
    fun search(query: String): List<Contact>
}