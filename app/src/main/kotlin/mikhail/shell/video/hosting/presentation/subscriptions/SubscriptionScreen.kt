package mikhail.shell.video.hosting.presentation.subscriptions

import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import mikhail.shell.video.hosting.R
import mikhail.shell.video.hosting.presentation.user.screen.ChannelSnippet
import mikhail.shell.video.hosting.presentation.utils.ErrorComponent
import mikhail.shell.video.hosting.presentation.utils.LoadingComponent
import mikhail.shell.video.hosting.presentation.utils.TopBar

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun SubscriptionsScreen(
    modifier: Modifier = Modifier,
    state: SubscriptionsScreenState,
    onRefresh: () -> Unit,
    onChannelClick: (Long) -> Unit
) {
    val windowSize = calculateWindowSizeClass(LocalActivity.current!!)
    val isWidthCompact = windowSize.widthSizeClass == WindowWidthSizeClass.Compact
    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface),
        topBar = {
            TopBar(
                title = stringResource(R.string.subscriptions_title)
            )
        }
    ) { padding ->
        if (state.channels != null) {
            LazyVerticalGrid(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .then(
                        if (isWidthCompact) {
                            Modifier
                        } else {
                            Modifier
                                .padding(top = 10.dp)
                                .padding(horizontal = 10.dp)
                        }
                    ),
                columns = GridCells.Adaptive(300.dp),
                horizontalArrangement = Arrangement.spacedBy(if (isWidthCompact) 0.dp else 10.dp),
                verticalArrangement = Arrangement.spacedBy(if (isWidthCompact) 0.dp else 10.dp)
            ) {
                items(state.channels) {
                    ChannelSnippet(
                        modifier = Modifier
                            .then(
                                if (windowSize.widthSizeClass == WindowWidthSizeClass.Compact) {
                                    Modifier
                                } else {
                                    Modifier
                                        .clip(RoundedCornerShape(15.dp))
                                }
                            ),
                        channel = it,
                        onClick = onChannelClick
                    )
                }
            }
        } else if (state.isLoading) {
            LoadingComponent(
                modifier = modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surface)
            )
        } else if (state.error != null) {
            ErrorComponent(
                modifier = modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surface),
                onRetry = onRefresh
            )
        }
    }
}