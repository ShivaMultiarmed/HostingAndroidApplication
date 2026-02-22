package mikhail.shell.video.hosting.presentation.utils

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.SaverScope
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

fun LazyGridState.reachedBottom(buffer: Int = 1): Boolean {
    val lastVisibleItemIndex = layoutInfo.visibleItemsInfo.lastOrNull()?.index?: return true
    val lastItemIndexInBuffer = layoutInfo.totalItemsCount - 1 - buffer
    return lastVisibleItemIndex >= lastItemIndexInBuffer
}

fun LazyListState.reachedBottom(buffer: Int = 1): Boolean {
    val lastVisibleItemIndex = layoutInfo.visibleItemsInfo.lastOrNull()?.index?: return true
    val lastItemIndexInBuffer = layoutInfo.totalItemsCount - 1 - buffer
    return lastVisibleItemIndex >= lastItemIndexInBuffer
}

val dpSaver = object : Saver<MutableState<Dp>, Float> {
    override fun SaverScope.save(value: MutableState<Dp>): Float {
        return value.value.value
    }
    override fun restore(value: Float): MutableState<Dp> {
        return mutableStateOf(value.dp)
    }
}