package mikhail.shell.video.hosting.presentation.utils

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.drag
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
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

@Composable
fun RestartableBox(
    modifier: Modifier = Modifier,
    isStarting: Boolean,
    canStart: Boolean = true,
    onStart: () -> Unit,
    content: @Composable () -> Unit
) {
    val resistance = 0.15f
    val density = LocalDensity.current.density
    var height by rememberSaveable { mutableStateOf(topPosition) }
    val animatedHeight by animateDpAsState(height, tween(200))
    var isDragged by rememberSaveable { mutableStateOf(false) }
    val isStartingUpdated by rememberUpdatedState(isStarting)
    Box(
        modifier = modifier
            .clipToBounds()
            .then(
                if (canStart && !isStarting) {
                    Modifier.pointerInput(Unit) {
                        awaitPointerEventScope {
                            while (true) {
                                val down = awaitFirstDown(pass = PointerEventPass.Initial)
                                isDragged = true
                                drag(down.id) {
                                    if (height < bottomPosition) {
                                        height =
                                            (height + (it.positionChange().y * density * resistance).dp).coerceIn(
                                                topPosition,
                                                bottomPosition
                                            )
                                        if (!(height == topPosition && it.positionChange().y < 0)) {
                                            it.consume()
                                        }
                                    }
                                }
                                isDragged = false
                                if (height == bottomPosition) {
                                    onStart()
                                } else if (height < bottomPosition) {
                                    height = topPosition
                                }
                            }
                        }
                    }
                } else {
                    Modifier
                }
            ),
        contentAlignment = Alignment.TopCenter
    ) {
        content()
        RestartThumb(
            isStarting = isStartingUpdated,
            height = if (isDragged || isStartingUpdated) height else animatedHeight
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
    isStarting: Boolean,
    height: Dp
) {
    Box(
        modifier = modifier
            .offset(y = height)
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
            .background(Color.Transparent),
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
                    .rotate((height - topPosition)/(bottomPosition - topPosition) * 360),
                progress = { 0.25f },
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}