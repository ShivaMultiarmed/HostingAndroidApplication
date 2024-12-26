package mikhail.shell.video.hosting.presentation.profile

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusModifier
import androidx.compose.ui.input.pointer.motionEventSpy
import androidx.compose.ui.unit.dp
import mikhail.shell.video.hosting.presentation.utils.ErrorComponent
import mikhail.shell.video.hosting.presentation.utils.LoadingComponent

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier,
    state: ProfileScreenState,
    onGoToChannel: (Long) -> Unit,
    onPublishVideo: () -> Unit,
    onCreateChannel: () -> Unit,
    onRefresh: () -> Unit
) {
    if (state.channels != null) {
        val scrollState = rememberScrollState()
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                //.verticalScroll(scrollState)
        ) {
            stickyHeader {
                Button(
                    onClick = onCreateChannel
                ) {
                    Text("Создать канал")
                }
                Button(
                    onClick = onPublishVideo
                ) {
                    Text("Опубликовать видео")
                }
                Spacer(
                    modifier = Modifier.fillMaxSize()
                        .height(200.dp)
                )
            }
            items(state.channels) {
                Button(
                    onClick = {
                        onGoToChannel(it.channelId!!)
                    }
                ) {
                    Text(
                        text = it.title
                    )
                }
            }

        }
    } else if (state.channelError != null || state.userError != null) {
        ErrorComponent(
            modifier = modifier.fillMaxSize(),
            onRetry = onRefresh
        )
    } else {
        LoadingComponent(
            modifier = modifier.fillMaxSize()
        )
    }
}