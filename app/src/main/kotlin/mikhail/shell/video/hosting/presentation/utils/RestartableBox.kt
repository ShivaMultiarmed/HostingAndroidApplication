package mikhail.shell.video.hosting.presentation.utils

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
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
import androidx.compose.ui.unit.coerceAtMost
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.times
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val duration = 400
private val reloadIndicatorSize = 24.dp
private val reloadThumbSize = 36.dp
private val shadowWidth = 5.dp
private val visibleDiameter = (reloadThumbSize + shadowWidth * 2)
private val idlePosition = - 1.5f * visibleDiameter
private val activationZoneSize = 46.dp

@Composable
fun RestartableBox(
    modifier: Modifier = Modifier,
    isStarting: Boolean,
    canStart: Boolean = true,
    onStart: () -> Unit,
    content: @Composable BoxScope.() -> Unit
) {
    val density = LocalDensity.current
    val resistance = 1.4f

    var boxHeight by remember {
        mutableStateOf(0.dp)
    }
    val endActivationPosition = remember (boxHeight) {
        (2 * visibleDiameter).coerceAtMost(boxHeight - visibleDiameter)
    }
    val activationZone = (endActivationPosition - activationZoneSize)..endActivationPosition


    var offsetY by rememberSaveable(saver = dpSaver) {
        mutableStateOf(idlePosition)
    }

    val animatedOffsetY = remember {
        Animatable(
            initialValue = offsetY,
            typeConverter = Dp.VectorConverter
        )
    }
    val updatedOffsetY by rememberUpdatedState(offsetY)
    val canStartUpdated by rememberUpdatedState(canStart)
    var isStartingCurrent by rememberSaveable {
        mutableStateOf(isStarting)
    }
    val isStartingUpdated by rememberUpdatedState(isStartingCurrent)
    Box(
        modifier = modifier
            .clipToBounds()
            .onGloballyPositioned { coordinates ->
                boxHeight = with(density) {
                    coordinates.size.height.toDp()
                }
            }
            .pointerInput(Unit) {
                coroutineScope {
                    awaitPointerEventScope {
                        while (true) {
                            val event = awaitPointerEvent(PointerEventPass.Main)
                            val change = event.changes.firstOrNull() ?: continue
                            when {
                                !change.previousPressed && change.pressed -> { // Pointer down
                                }

                                change.pressed && change.positionChange() != Offset.Zero -> { // Dragging
                                    if (!isStartingUpdated && canStartUpdated) {
                                        offsetY =
                                            (updatedOffsetY + (change.positionChange().y * resistance).toDp()).coerceIn(
                                                idlePosition,
                                                activationZone.endInclusive
                                            )
                                        if (!(updatedOffsetY == idlePosition && change.positionChange().y < 0)) {
                                            change.consume()
                                        }
                                    }
                                }

                                change.previousPressed && !change.pressed -> { // Pointer up or Canceled
                                    launch {
                                        animatedOffsetY.snapTo(updatedOffsetY)
                                        if (updatedOffsetY in activationZone) {
                                            if (canStartUpdated) {
                                                animatedOffsetY.animateTo(activationZone.start, tween(duration))
                                                onStart()
                                            }
                                        } else {
                                            animatedOffsetY.animateTo(idlePosition, tween(duration))
                                        }
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
            modifier = Modifier.offset(y = offsetY),
            isActive = offsetY in activationZone,
            isStarting = isStarting
        )
    }
    LaunchedEffect(Unit) {
        snapshotFlow { animatedOffsetY.value }.collect {
            offsetY = it
        }
    }
    LaunchedEffect(isStarting) {
        if (!isStarting) {
            delay(duration.toLong())
            isStartingCurrent = isStarting
            animatedOffsetY.snapTo(offsetY)
            animatedOffsetY.animateTo(idlePosition, tween(duration))
        }
    }
}

@Composable
private fun RestartThumb(
    modifier: Modifier = Modifier,
    isActive: Boolean,
    isStarting: Boolean
) {
    var boxHeight by remember {
        mutableStateOf(0.dp)
    }
    val endActivationPosition = remember (boxHeight) {
        (2 * visibleDiameter).coerceAtMost(boxHeight - visibleDiameter)
    }
    val activationZone = (endActivationPosition - activationZoneSize)..endActivationPosition
    val density = LocalDensity.current
    var isStartingCurrent by rememberSaveable {
        mutableStateOf(isStarting)
    }
    LaunchedEffect(isStarting) {
        if (!isStarting) {
            delay(duration.toLong())
        }
        isStartingCurrent = isStarting
    }
    var angle by rememberSaveable {
        mutableFloatStateOf(0f)
    }
    val updatedAngle by rememberUpdatedState(angle)
    Box(
        modifier = modifier
            .size(visibleDiameter)
            .background(Color.Transparent)
            .shadow(
                elevation = shadowWidth,
                shape = CircleShape
            )
            .onGloballyPositioned { coordinates ->
                with(density) {
                    val parentHeight = coordinates.parentLayoutCoordinates?.size?.height?.toDp()
                    if (parentHeight != null) {
                        boxHeight = parentHeight
                    }
                    val verticalOffset = coordinates.positionInParent().y.toDp()
                    angle = ((verticalOffset - idlePosition) / (activationZone.start - idlePosition)).coerceAtMost(1f) * 360
                }
            }
            .size(reloadThumbSize)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .size(reloadIndicatorSize)
            .clip(CircleShape)
            .background(Color.Transparent),
        contentAlignment = Alignment.Center
    ) {
        if (isStartingCurrent) {
            CircularProgressIndicator(
                modifier = Modifier.size(reloadIndicatorSize),
                color = MaterialTheme.colorScheme.primary
            )
        } else {
            CircularProgressIndicator(
                modifier = Modifier
                    .size(reloadIndicatorSize)
                    .rotate(angle),
                progress = { updatedAngle / 360 },
                color = when (isActive) {
                    true -> MaterialTheme.colorScheme.primary
                    false -> MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                }
            )
        }
    }
}
