package mikhail.shell.video.hosting.presentation.utils

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn

context(viewModel: ViewModel)
fun <T> Flow<T>.stateIn(initialValue: T) = stateIn(
    scope = viewModel.viewModelScope,
    started = SharingStarted.WhileSubscribed(3000),
    initialValue = initialValue
)