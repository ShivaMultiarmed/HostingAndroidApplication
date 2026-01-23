package mikhail.shell.video.hosting.presentation.utils

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.SaverScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.times

private val reloadIndicatorSize = 20.dp
private val reloadThumbSize = 1.5 * reloadIndicatorSize
private val shadowBaseDiameter = 1.2 * reloadThumbSize
private val shadowWidth = 5.dp
private val topPosition = -(shadowBaseDiameter + shadowWidth)
private val bottomPosition = 0.7f * (shadowBaseDiameter + shadowWidth)

private val dpSaver = object : Saver<MutableState<Dp>, Float> {
    override fun SaverScope.save(value: MutableState<Dp>): Float {
        return value.value.value
    }

    override fun restore(value: Float): MutableState<Dp> {
        return mutableStateOf(value.dp)
    }
}

@Composable
fun RestartableBox(
    modifier: Modifier = Modifier,
    isStarting: Boolean,
    canStart: Boolean = true,
    onStart: () -> Unit,
    content: @Composable () -> Unit
) {
    val TAG = "RestartableBox"
    val resistance = 0.15f
    val density = LocalDensity.current.density
    var height by rememberSaveable(saver = dpSaver) { mutableStateOf(topPosition) }
    val animatedHeight by animateDpAsState(height, tween(200))
    var isPressed by rememberSaveable { mutableStateOf(false) }
    var isDragged by rememberSaveable { mutableStateOf(false) }
    val canStartUpdated by rememberUpdatedState(canStart)
    val isStartingUpdated by rememberUpdatedState(isStarting)
    Box(
        modifier = modifier
            .clipToBounds()
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent(pass = PointerEventPass.Initial)
                        event.changes.firstOrNull()?.let { change ->
                            when {
                                !change.previousPressed && change.pressed -> { // Pointer down
                                    isPressed = true
                                    isDragged = false
                                }
                                change.pressed && change.positionChange() != Offset.Zero -> { // Dragging
                                    isDragged = true
                                    if (!isStartingUpdated && canStartUpdated) {
                                        height =
                                            (height + (change.positionChange().y * density * resistance).dp).coerceIn(
                                                topPosition,
                                                bottomPosition
                                            )
                                        if (!(height == topPosition && change.positionChange().y < 0)) {
                                            change.consume()
                                        }
                                    }
                                }
                                change.previousPressed && !change.pressed -> { // Pointer up or Cancelled
                                    isDragged = false
                                    isPressed = false
                                    if (height == bottomPosition) {
                                        if (canStartUpdated) {
                                            onStart()
                                        }
                                    } else if (height < bottomPosition) {
                                        height = topPosition
                                    }
                                }
                            }
                        }
                    }
                }
            },
        contentAlignment = Alignment.TopCenter
    ) {
        content()
        RestartThumb(
            modifier = Modifier.offset(
                y = when {
                    isDragged || isPressed || isStartingUpdated -> height
                    else -> animatedHeight
                }
            ),
            isStarting = isStartingUpdated
        )
    }
    LaunchedEffect(isStarting) {
        if (!isStarting) {
            height = topPosition
        }
    }
}

@Composable
private fun RestartThumb(
    modifier: Modifier = Modifier,
    isStarting: Boolean
) {
    var angle by rememberSaveable {
        mutableFloatStateOf(0f)
    }
    Box(
        modifier = modifier
            .size(shadowBaseDiameter)
            .background(Color.Transparent)
            .shadow(
                elevation = shadowWidth,
                shape = CircleShape
            )
            .size(reloadThumbSize)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .size(reloadIndicatorSize)
            .clip(CircleShape)
            .background(Color.Transparent)
            .onGloballyPositioned {
                val verticalOffset = it.positionInParent().y.dp
                angle = (verticalOffset - topPosition) / (bottomPosition - topPosition) * 360
            },
        contentAlignment = Alignment.Center
    ) {
        if (isStarting) {
            CircularProgressIndicator(
                modifier = Modifier.size(reloadIndicatorSize),
                color = MaterialTheme.colorScheme.primary
            )
        } else {
            CircularProgressIndicator(
                modifier = Modifier
                    .size(reloadIndicatorSize)
                    .rotate(angle),
                progress = { angle / 360 },
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
