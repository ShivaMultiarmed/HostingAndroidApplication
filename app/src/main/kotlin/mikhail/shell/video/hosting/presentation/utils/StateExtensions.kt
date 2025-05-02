package mikhail.shell.video.hosting.presentation.utils

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable

fun LazyGridState.reachedBottom(buffer: Int = 1): Boolean {
    val lastVisibleItemIndex = this.layoutInfo.visibleItemsInfo.lastOrNull()?.index?: return true
    val lastItemIndexInBuffer = this.layoutInfo.totalItemsCount - 1 - buffer
    return lastVisibleItemIndex >= lastItemIndexInBuffer
}

fun LazyListState.reachedBottom(buffer: Int = 1): Boolean {
    val lastVisibleItemIndex = this.layoutInfo.visibleItemsInfo.lastOrNull()?.index?: return true
    val lastItemIndexInBuffer = this.layoutInfo.totalItemsCount - 1 - buffer
    return lastVisibleItemIndex >= lastItemIndexInBuffer
}
@Composable
fun rememberOrientationState(): State<Int> {
    return rememberSaveable { mutableIntStateOf(0) }
}