package mikhail.shell.video.hosting.presentation.utils

import android.content.res.Configuration
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.core.util.Consumer

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
    val activity = LocalActivity.current as ComponentActivity
    val initialOrientation = LocalConfiguration.current.orientation
    val state = rememberSaveable { mutableIntStateOf(initialOrientation) }
    DisposableEffect(Unit) {
        val listener = Consumer<Configuration> { conf -> state.intValue = conf.orientation }
        activity.addOnConfigurationChangedListener(listener)
        onDispose {
            activity.removeOnConfigurationChangedListener(listener)
        }
    }
    return state
}