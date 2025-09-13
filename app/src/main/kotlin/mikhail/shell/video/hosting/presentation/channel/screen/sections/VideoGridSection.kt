package mikhail.shell.video.hosting.presentation.channel.screen.sections

import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import mikhail.shell.video.hosting.R
import mikhail.shell.video.hosting.presentation.utils.EmptyResultComponent
import mikhail.shell.video.hosting.presentation.utils.PageableBox
import mikhail.shell.video.hosting.presentation.utils.RestartableBox
import mikhail.shell.video.hosting.presentation.video.VideoSnippet
import mikhail.shell.video.hosting.presentation.video.models.VideoUi

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
internal fun VideoGridSection(
    modifier: Modifier = Modifier,
    isStarting: Boolean,
    videos: List<VideoUi>,
    onVideoClick: (videoId: Long) -> Unit,
    onReachedBottom: () -> Unit,
    onRestart: () -> Unit,
    onReload: () -> Unit,
    hasMore: Boolean
) {
    val windowSize = calculateWindowSizeClass(LocalActivity.current!!)
    val isWidthCompact = windowSize.widthSizeClass == WindowWidthSizeClass.Compact
    RestartableBox(
        modifier = modifier,
        onLaunch = onRestart,
        isStarting = isStarting,
    ) {
        PageableBox(
            modifier = Modifier.fillMaxSize(),
            itemComponent = {
                VideoSnippet(
                    modifier = Modifier.then(
                        if (isWidthCompact) {
                            Modifier
                        } else {
                            Modifier.clip(RoundedCornerShape(15.dp))
                        }
                    ),
                    video = it,
                    onClick = onVideoClick
                )
            },
            emptyComponent = {
                EmptyResultComponent(
                    modifier = Modifier.fillMaxSize(),
                    message = stringResource(R.string.no_videos_yet)
                )
            },
            items = videos,
            hasMore = hasMore,
            onReachedBottom = onReachedBottom,
            onReload = onReload
        )
    }
}