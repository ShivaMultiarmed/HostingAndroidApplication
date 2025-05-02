package mikhail.shell.video.hosting.presentation.utils

import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp

@Composable
fun PipContainer(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    var xOffset by remember { mutableIntStateOf(0) }
    var yOffset by remember { mutableIntStateOf(0) }
    var pipSize by remember { mutableStateOf(IntSize.Zero) }
    var parentWidth by remember { mutableIntStateOf(0) }
    var parentHeight by remember { mutableIntStateOf(0) }
    Box(
        modifier = modifier
            .offset{
                IntOffset(xOffset, yOffset)
            }.pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    xOffset += dragAmount.x.toInt()
                    xOffset = xOffset.coerceIn(0, parentWidth - pipSize.width)
                    yOffset += dragAmount.y.toInt()
                    yOffset = yOffset.coerceIn(0, parentHeight - pipSize.height)
                }
            }.onGloballyPositioned {
                pipSize = it.size
                it.parentLayoutCoordinates?.size?.width?.let {
                    parentWidth = it
                }
                it.parentLayoutCoordinates?.size?.height?.let {
                    parentHeight = it
                }
            }.clip(RoundedCornerShape(10.dp)),
        content = content
    )
}

@Composable
fun PipTopBar(
    modifier: Modifier = Modifier,
    onClose: () -> Unit
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.End
    ) {
        Icon(
            modifier = Modifier
                .size(27.dp)
                .clickable(onClick = onClose),
            imageVector = Icons.Rounded.Close,
            tint = Color.White,
            contentDescription = "Закрыть видео"
        )
    }
}