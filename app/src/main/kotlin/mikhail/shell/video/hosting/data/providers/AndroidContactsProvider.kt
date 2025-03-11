package mikhail.shell.video.hosting.data.providers

import android.content.Context
import android.provider.ContactsContract
import android.telephony.SmsManager
import mikhail.shell.video.hosting.domain.models.Contact
import mikhail.shell.video.hosting.domain.providers.ContactsProvider

class AndroidContactsProvider(private val context: Context): ContactsProvider {

    private val contentResolver = context.contentResolver

    override fun search(query: String): List<Contact> {
        val contacts = mutableListOf<Contact>()
        val cursor = contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            arrayOf(
                ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.NUMBER
            ),
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " LIKE ?",
            arrayOf("%$query%"),
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
        )
        cursor?.use { it ->
            val idColIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID)
            val nameColIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
            val numberColIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
            while (it.moveToNext()) {
                val contactId = it.getString(idColIndex)
                val number = it.getString(numberColIndex)
                if (contacts.count { it.id == contactId } == 0) {
                    val name = it.getString(nameColIndex)
                    contacts.add(Contact(contactId, name, listOf(number)))
                } else {
                    val existingContact = contacts.last()
                    val newPhoneList = existingContact.phones.toMutableList().apply { add(number) }
                    contacts[contacts.size - 1] = existingContact.copy(phones = newPhoneList)
                }
            }
        }
        return contacts
    }

    override fun invite(number: String) {
        val smsManager = context.getSystemService(SmsManager::class.java)
        smsManager.sendTextMessage(number, null, "Приглашаю Вас в приложение Trendy!", null, null)
    }
}