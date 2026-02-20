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
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.SaverScope
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
private val reloadThumbSize = 1.5f * reloadIndicatorSize
private val shadowBaseDiameter = 1.2f * reloadThumbSize
private val shadowWidth = 5.dp
private val topPosition = -(shadowBaseDiameter + shadowWidth) * 1.5f
private val bottomPosition = 1.0f * (shadowBaseDiameter + shadowWidth)
private val activationZone = bottomPosition..(0.6f * (bottomPosition - topPosition))

val dpSaver = object : Saver<MutableState<Dp>, Float> {
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
    content: @Composable BoxScope.() -> Unit
) {
    val resistance = 0.15f
    val density = LocalDensity.current.density
    var height by rememberSaveable(saver = dpSaver) {
        mutableStateOf(topPosition)
    }
    val animatedHeight = remember {
        Animatable(
            initialValue = height,
            typeConverter = Dp.VectorConverter
        )
    }
    val updatedHeight by rememberUpdatedState(height)
    val canStartUpdated by rememberUpdatedState(canStart)
    var isStartingCurrent by rememberSaveable {
        mutableStateOf(isStarting)
    }
    val isStartingUpdated by rememberUpdatedState(isStartingCurrent)
    Box(
        modifier = modifier
            .clipToBounds()
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
                                        height =
                                            (updatedHeight + (change.positionChange().y * density * resistance).dp).coerceIn(
                                                topPosition,
                                                activationZone.endInclusive
                                            )
                                        if (!(updatedHeight == topPosition && change.positionChange().y < 0)) {
                                            change.consume()
                                        }
                                    }
                                }

                                change.previousPressed && !change.pressed -> { // Pointer up or Canceled
                                    launch {
                                        animatedHeight.snapTo(updatedHeight)
                                        if (updatedHeight in activationZone) {
                                            if (canStartUpdated) {
                                                animatedHeight.animateTo(bottomPosition, tween(duration))
                                                onStart()
                                            }
                                        } else {
                                            animatedHeight.animateTo(topPosition, tween(duration))
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
            modifier = Modifier.offset(y = height),
            isActive = height in activationZone,
            isStarting = isStarting
        )
    }
    LaunchedEffect(Unit) {
        snapshotFlow { animatedHeight.value }.collect {
            height = it
        }
    }
    LaunchedEffect(isStarting) {
        if (!isStarting) {
            delay(duration.toLong())
            isStartingCurrent = isStarting
            animatedHeight.snapTo(height)
            animatedHeight.animateTo(topPosition, tween(duration))
        }
    }
}

@Composable
private fun RestartThumb(
    modifier: Modifier = Modifier,
    isActive: Boolean,
    isStarting: Boolean
) {
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
                val verticalOffset = with(density) {
                    it.positionInParent().y.toDp()
                }
                angle = (verticalOffset.coerceAtMost(activationZone.start) - topPosition) / (activationZone.start - topPosition) * 360
            },
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
                progress = { angle / 360 },
                color = when (isActive) {
                    true -> MaterialTheme.colorScheme.primary
                    false -> MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                }
            )
        }
    }
}
