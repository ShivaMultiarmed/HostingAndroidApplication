package mikhail.shell.video.hosting.presentation.utils

import android.view.MotionEvent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun PipContainer(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    var xOffset by remember { mutableFloatStateOf(0f) }
    var yOffset by remember { mutableFloatStateOf(0f) }
    var pipSize by remember { mutableStateOf(IntSize.Zero) }
    val initialPosition = Offset.Zero
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current
    var initialTouchOffset by remember { mutableStateOf(Offset.Zero) }
    val screenHeightPx = with(density) {
        configuration.screenHeightDp.dp.toPx()
    }
    val screenWidthPx = with (density) {
        configuration.screenWidthDp.dp.toPx()
    }
    LaunchedEffect(Unit) {
        yOffset = initialPosition.y.coerceIn(0f, screenHeightPx - pipSize.height)
        xOffset = initialPosition.x.coerceIn(0f, screenWidthPx - pipSize.width)
    }
    Box(
        modifier = modifier
            .offset{
                IntOffset(yOffset.roundToInt(), xOffset.roundToInt())
            }.pointerInteropFilter { event ->
                when(event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        initialTouchOffset = Offset(
                            event.rawX - xOffset,
                            event.rawY - yOffset
                        )
                        true
                    }
                    MotionEvent.ACTION_MOVE -> {
                        val newX = event.rawX - initialTouchOffset.x
                        val newY = event.rawY - initialTouchOffset.y
                        xOffset = newX.coerceIn(0f, screenWidthPx - pipSize.width)
                        yOffset = newY.coerceIn(0f, screenHeightPx - pipSize.height)
                        true
                    }
                    else -> false
                }
            }.onGloballyPositioned { coordinates ->
                pipSize = coordinates.size
            }.width(300.dp)
            .height(250.dp)
            .clip(RoundedCornerShape(10.dp)),
    ) {
        content()
    }
}