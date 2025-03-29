package mikhail.shell.video.hosting.presentation.subscriptions

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import mikhail.shell.video.hosting.presentation.user.screen.ChannelSnippet
import mikhail.shell.video.hosting.presentation.utils.ErrorComponent
import mikhail.shell.video.hosting.presentation.utils.LoadingComponent
import mikhail.shell.video.hosting.presentation.utils.TopBar

@Composable
fun SubscriptionsScreen(
    modifier: Modifier = Modifier,
    state: SubscriptionsScreenState,
    onRefresh: () -> Unit,
    onChannelClick: (Long) -> Unit
) {
    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface),
        topBar = {
            TopBar(
                title = "Подписки"
            )
        }
    ) { padding ->
        if (state.channels != null) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                items(state.channels) {
                    ChannelSnippet(
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