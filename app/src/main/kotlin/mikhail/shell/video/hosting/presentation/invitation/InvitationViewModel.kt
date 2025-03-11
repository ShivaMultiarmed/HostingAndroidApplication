package mikhail.shell.video.hosting.presentation.invitation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import mikhail.shell.video.hosting.domain.usecases.contacts.Invite
import mikhail.shell.video.hosting.domain.usecases.contacts.SearchContacts
import javax.inject.Inject

@HiltViewModel
class InvitationViewModel @Inject constructor(
    private val _searchContacts: SearchContacts,
    private val _invite: Invite
): ViewModel() {
    private val _state = MutableStateFlow(InvitationScreenState())
    val state = _state.asStateFlow()
    init {
        searchContacts()
    }
    fun searchContacts(query: String = "") {
        _state.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            _state.update {
                it.copy(
                    isLoading = false,
                    contacts = _searchContacts(query)
                )
            }
        }
    }
    fun invite(number: String) {
        _invite(number)
    }
}