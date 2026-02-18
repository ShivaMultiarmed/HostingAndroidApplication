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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import mikhail.shell.video.hosting.R

@Composable
fun PipContainer(
    modifier: Modifier = Modifier,
    initialOffset: DpOffset = DpOffset.Zero,
    onOffsetChanged: (x: Dp, y: Dp) -> Unit,
    content: @Composable BoxScope.() -> Unit
) {
    var xOffset by rememberSaveable (saver = dpSaver) { mutableStateOf(initialOffset.x) }
    var yOffset by rememberSaveable (saver = dpSaver) { mutableStateOf(initialOffset.y) }
    var pipWidth by rememberSaveable (saver = dpSaver) { mutableStateOf(0.dp) }
    var pipHeight by rememberSaveable (saver = dpSaver) { mutableStateOf(0.dp) }
    var parentWidth by remember { mutableStateOf(0.dp) }
    var parentHeight by remember { mutableStateOf(0.dp) }
    LaunchedEffect (parentWidth, parentHeight, pipWidth, pipHeight) {
        if (parentWidth > 0.dp && parentHeight > 0.dp && pipWidth > 0.dp && pipHeight > 0.dp) {
            xOffset = xOffset.coerceIn(0.dp, parentWidth - pipWidth)
            yOffset = yOffset.coerceIn(0.dp, parentHeight - pipHeight)
            onOffsetChanged(xOffset, yOffset)
        }
    }
    val density = LocalDensity.current
    Box(
        modifier = modifier
            .offset(
                x = xOffset,
                y = yOffset
            ).pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    xOffset += dragAmount.x.toDp()
                    xOffset = xOffset.coerceIn(0.dp, parentWidth - pipWidth)
                    yOffset += dragAmount.y.toDp()
                    yOffset = yOffset.coerceIn(0.dp, parentHeight - pipHeight)
                    onOffsetChanged(xOffset, yOffset)
                }
            }.onGloballyPositioned {
                with (density) {
                    pipWidth = it.size.width.toDp()
                    pipHeight =  it.size.height.toDp()
                    it.parentLayoutCoordinates?.size?.width?.let {
                        parentWidth = it.toDp()
                    }
                    it.parentLayoutCoordinates?.size?.height?.let {
                        parentHeight = it.toDp()
                    }
                }
            }
            .clip(RoundedCornerShape(10.dp)),
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
            contentDescription = stringResource(R.string.pip_container_close_hint)
        )
    }
}