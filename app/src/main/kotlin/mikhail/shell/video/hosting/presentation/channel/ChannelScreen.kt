package mikhail.shell.video.hosting.presentation.channel

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun ChannelScreen(
    state: ChannelScreenState,
    onRefresh: () -> Unit,
    onSubscription: () -> Unit
) {
    if (state.info != null) {
        Text (
            text = state.info.info.title
        )
    }
}
