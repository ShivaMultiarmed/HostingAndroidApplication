package mikhail.shell.video.hosting.presentation.utils

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import mikhail.shell.video.hosting.ui.theme.VideoHostingTheme
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun ReloadableBox(
    modifier: Modifier = Modifier,
    onLaunch: () -> Unit,
    isLoading: Boolean,
    content: @Composable () -> Unit
) {
    val reloadIndicatorSize = 30
    val reloadThumbSize = 1.5f * reloadIndicatorSize
    val shadowBaseDiameter = 1.2f * reloadThumbSize
    val shadowWidth = 5
    val resistance = 0.2f
    val topPosition = -(shadowBaseDiameter + shadowWidth)
    val bottomPosition = 0.7f * (shadowBaseDiameter + shadowWidth)
    val density = LocalDensity.current.density
    var height by rememberSaveable { mutableFloatStateOf(topPosition) }
    val animatedHeight by animateDpAsState(height.dp, tween(200))
    var isDragged by rememberSaveable { mutableStateOf(false) }
    Box(
        modifier = modifier
            .clipToBounds()
            .pointerInput(Unit) {
                detectDragGestures(
                    onDrag = { change, offset ->
                        if (!isLoading) {
                            change.consume()
                            if (height < bottomPosition) {
                                height =
                                    (height + (offset.y * density * resistance).toInt()).coerceIn(
                                        topPosition,
                                        bottomPosition
                                    )
                            }
                        }
                    },
                    onDragStart = {
                        isDragged = true
                    },
                    onDragCancel = {
                        height = topPosition
                        isDragged = false
                    },
                    onDragEnd = {
                        if (height == bottomPosition && !isLoading) {
                            onLaunch()
                        }
                        if (isLoading || height < bottomPosition) {
                            height = topPosition
                        }
                        isDragged = false
                    }
                )
            },
        contentAlignment = Alignment.TopCenter
    ) {
        content()
        Box(
            modifier = Modifier
                .offset(
                    y = if (isDragged) height.dp else animatedHeight
                )
                .size(shadowBaseDiameter.dp)
                .background(Color.Transparent)
                .shadow(
                    elevation = shadowWidth.dp,
                    shape = CircleShape
                )
                .size(reloadThumbSize.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.secondary)
                .size(reloadIndicatorSize.dp)
                .clip(CircleShape)
                .background(Color.Transparent)
            ,
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                modifier = Modifier
                    .size(reloadIndicatorSize.dp)
                    .rotate(
                        if (isLoading) {
                            val infiniteRotation = rememberInfiniteTransition()
                            infiniteRotation.animateFloat(
                                initialValue = 0f,
                                targetValue = 360f,
                                infiniteRepeatable(
                                    animation = tween(800),
                                    repeatMode = RepeatMode.Restart
                                )
                            ).value
                        } else 0f
                    ),
                progress = { 0.25f },
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
    LaunchedEffect(isLoading) {
        if (!isLoading) {
            height = topPosition
        }
    }
}

@Composable
@Preview
fun ReloadableBoxPreview() {
    VideoHostingTheme {
        var isLoading by remember { mutableStateOf(false) }
        val coroutineScope = rememberCoroutineScope()
        Scaffold { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                ReloadableBox(
                    modifier = Modifier
                        .fillMaxSize(),
                    onLaunch = {
                        coroutineScope.launch {
                            isLoading = true
                            delay(3000.milliseconds)
                            isLoading = false
                        }
                    },
                    isLoading = isLoading
                ) {

                }
            }
        }
    }
}