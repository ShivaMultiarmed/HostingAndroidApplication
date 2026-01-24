package mikhail.shell.video.hosting.presentation.channel.screen.sections

import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import mikhail.shell.video.hosting.R
import mikhail.shell.video.hosting.presentation.utils.EmptyComponent
import mikhail.shell.video.hosting.presentation.utils.PageableBox
import mikhail.shell.video.hosting.presentation.utils.PageableBoxState
import mikhail.shell.video.hosting.presentation.utils.RestartableBox
import mikhail.shell.video.hosting.presentation.utils.isAtStart
import mikhail.shell.video.hosting.presentation.video.VideoSnippet
import mikhail.shell.video.hosting.presentation.video.models.VideoUi

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
internal fun VideoGridSection(
    modifier: Modifier = Modifier,
    state: PageableBoxState<VideoUi>,
    isStarting: Boolean,
    onVideoClick: (videoId: Long) -> Unit,
    onReachedBottom: () -> Unit,
    onRestart: () -> Unit,
    onReload: () -> Unit
) {
    val windowSize = calculateWindowSizeClass(LocalActivity.current!!)
    val isWidthCompact = windowSize.widthSizeClass == WindowWidthSizeClass.Compact
    val isAtStart by remember {
        derivedStateOf {
            state.gridState.isAtStart()
        }
    }
    RestartableBox(
        modifier = modifier,
        onStart = onRestart,
        isStarting = isStarting,
        canStart = isAtStart
    ) {
        PageableBox(
            modifier = Modifier.fillMaxSize(),
            state = state,
            itemComponent = {
                VideoSnippet(
                    modifier = Modifier.then(
                        when {
                            isWidthCompact -> Modifier
                            else -> Modifier.clip(RoundedCornerShape(15.dp))
                        }
                    ),
                    video = it,
                    onClick = onVideoClick
                )
            },
            emptyComponent = {
                EmptyComponent(
                    modifier = Modifier.fillMaxSize(),
                    message = stringResource(R.string.no_videos_yet)
                )
            },
            onReachedEnd = onReachedBottom,
            onReload = onReload
        )
    }
}