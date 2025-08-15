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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.times
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
    val reloadThumbSize = 25
    val resistance = 0.2f
    val initialPosition = -reloadThumbSize
    val maxPosition = 2 * reloadThumbSize
    val density = LocalDensity.current.density
    var height by rememberSaveable { mutableIntStateOf(initialPosition) }
    val animatedHeight by animateDpAsState(height.dp, tween(200))
    var isDragged by rememberSaveable { mutableStateOf(false) }
    Box(
        modifier = modifier
            .pointerInput(Unit) {
                detectDragGestures(
                    onDrag = { change, offset ->
                        if (!isLoading) {
                            change.consume()
                            if (height < maxPosition) {
                                height =
                                    (height + (offset.y * density * resistance).toInt()).coerceIn(
                                        initialPosition,
                                        maxPosition
                                    )
                            }
                        }
                    },
                    onDragStart = {
                        isDragged = true
                    },
                    onDragCancel = {
                        height = initialPosition
                        isDragged = false
                    },
                    onDragEnd = {
                        if (height == maxPosition && !isLoading) {
                            onLaunch()
                        }
                        if (isLoading || height < maxPosition) {
                            height = initialPosition
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
                .size(reloadThumbSize.dp)
                .offset(
                    y = if (isDragged) height.dp else animatedHeight
                )
                .clip(CircleShape)
                .background(Color.White)
                .size(1.2 * reloadThumbSize.dp)
                .background(Color.Transparent)
                .shadow(
                    elevation = 25.dp,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                modifier = Modifier
                    .matchParentSize()
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
            height = initialPosition
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
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .background(Color.Blue)
                )
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