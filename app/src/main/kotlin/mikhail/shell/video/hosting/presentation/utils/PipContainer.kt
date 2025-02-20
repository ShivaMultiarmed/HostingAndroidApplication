package mikhail.shell.video.hosting.presentation.utils

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

@Composable
fun PipContainer(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    var xOffset by remember { mutableFloatStateOf(0f) }
    var yOffset by remember { mutableFloatStateOf(0f) }
    var pipSize by remember { mutableStateOf(IntSize.Zero) }
    val screenWidthPx = with(LocalDensity.current) {
        LocalConfiguration.current.screenWidthDp.dp.toPx()
    }
    val screenHeightPx = with(LocalDensity.current) {
        LocalConfiguration.current.screenHeightDp.dp.toPx()
    }
    Box(
        modifier = modifier
            .offset{
                IntOffset(xOffset.roundToInt(), yOffset.roundToInt())
            }.pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    xOffset += dragAmount.x
                    xOffset = xOffset.coerceIn(0f, screenWidthPx - pipSize.width)
                    yOffset += dragAmount.y
                    yOffset = yOffset.coerceIn(0f, screenHeightPx - pipSize.height)
                }
            }.onGloballyPositioned {
                pipSize = it.size
            }.clip(RoundedCornerShape(10.dp)),
    ) {
        content()
    }
}