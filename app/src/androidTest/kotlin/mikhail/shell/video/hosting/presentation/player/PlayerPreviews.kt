@file:OptIn(ExperimentalMaterial3WindowSizeClassApi::class)

package mikhail.shell.video.hosting.presentation.player

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import kotlinx.coroutines.delay
import mikhail.shell.video.hosting.ui.theme.VideoHostingTheme
import mikhail.shell.video.hosting.ui.theme.White

@Composable
@Preview(widthDp = 400, heightDp = 700)
private fun PlayerControlsPreview() {
    VideoHostingTheme {
        var isPlaying by rememberSaveable {
            mutableStateOf(false)
        }
        var position by rememberSaveable {
            mutableLongStateOf(0)
        }
        val duration = 100_000L
        LaunchedEffect(isPlaying) {
            if (isPlaying) {
                while (position < duration) {
                    delay(1000)
                    position += 1000
                }
            }
        }
        PlayerControls(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9),
            isPlaying = isPlaying,
            onPlay = {
                isPlaying = true
            },
            onPause = {
                isPlaying = false
            },
            onSeekForward = {
                position += 1000
            },
            onSeekBack = {
                position -= 1000
            },
            position = position,
            duration = duration,
            onSeek = {
                position = it
            },
            onFullscreen = {}
        )
    }
}

@Preview
@Composable
private fun StadiumShapePreview() {
    VideoHostingTheme {
        ConstraintLayout(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9)
                .background(Color.Blue)
        ) {
            val (seekBack, seekForward) = createRefs()
            Box(
                modifier = Modifier
                    .constrainAs(seekBack) {
                        start.linkTo(parent.start)
                    }
                    .fillMaxWidth(0.4f)
                    .fillMaxHeight()
                    .clip(createStadiumShape(Direction.Ltr))
                    .background(White)
            )
            Box(
                modifier = Modifier
                    .constrainAs(seekForward) {
                        end.linkTo(parent.end)
                    }
                    .fillMaxWidth(0.4f)
                    .fillMaxHeight()
                    .clip(createStadiumShape(Direction.Rtl))
                    .background(White)
            )
        }
    }
}

@Composable
@Preview
fun ShimmerEffectPreview() {
    Box(
        modifier = Modifier
            .size(300.dp)
            .shimmer(
                direction = Direction.Ltr,
                baseColor = Color(255f, 255f, 255f, 0.15f),
                accentColor = Color(255f, 255f, 255f, 0.5f),
            )
    )
}