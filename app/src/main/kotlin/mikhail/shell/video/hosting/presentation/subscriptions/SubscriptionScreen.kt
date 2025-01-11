package mikhail.shell.video.hosting.presentation.subscriptions

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import mikhail.shell.video.hosting.presentation.profile.ChannelSnippet
import mikhail.shell.video.hosting.presentation.utils.ErrorComponent
import mikhail.shell.video.hosting.presentation.utils.LoadingComponent

@Composable
fun SubscriptionsScreen(
    modifier: Modifier = Modifier,
    state: SubscriptionsScreenState,
    onRefresh: () -> Unit,
    onChannelClick: (Long) -> Unit
) {
    if (state.channels != null) {
        Box(
            modifier = modifier.fillMaxSize()
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                items(state.channels) {
                    ChannelSnippet(
                        channel = it,
                        onClick = onChannelClick
                    )
                }
            }
        }
    } else if (state.isLoading) {
        LoadingComponent(
            modifier = modifier.fillMaxSize()
        )
    } else if (state.error != null) {
        ErrorComponent(
            modifier = modifier.fillMaxSize(),
            onRetry = onRefresh
        )
    }
}