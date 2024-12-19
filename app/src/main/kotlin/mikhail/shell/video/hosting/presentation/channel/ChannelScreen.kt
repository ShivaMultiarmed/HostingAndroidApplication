package mikhail.shell.video.hosting.presentation.channel

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import mikhail.shell.video.hosting.domain.models.ChannelInfo
import mikhail.shell.video.hosting.domain.models.ExtendedChannelInfo
import mikhail.shell.video.hosting.presentation.utils.ErrorComponent
import mikhail.shell.video.hosting.presentation.utils.LoadingComponent

@Composable
fun ChannelScreen(
    state: ChannelScreenState,
    onRefresh: () -> Unit,
    onSubscription: () -> Unit
) {
    if (state.info != null) {
        val scrollState = rememberScrollState()
        val channel = state.info.info
        Column(
            modifier = Modifier.fillMaxSize()
                .verticalScroll(scrollState)
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp)
        ) {
            Box(
                modifier = Modifier.fillMaxWidth()
                    .height(120.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.secondaryContainer)
            ) {
                AsyncImage(
                    model = "http://192.168.1.107:9999/api/v1/channels/${channel.channelId}/cover",
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
            Row (
                modifier = Modifier.fillMaxWidth()
                    .padding(vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage(
                    model = "http://192.168.1.107:9999/api/v1/channels/${channel.channelId}/avatar",
                    contentDescription = "Ссылка на канал",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                )
                Column (
                    modifier = Modifier.weight(1f)
                        .padding(start = 10.dp),
                    verticalArrangement = Arrangement.Top
                ) {
                    Text(
                        text = channel.title,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "@${channel.alias}",
                        fontSize = 13.sp,
                        modifier = Modifier.padding(top = 10.dp)
                    )
                    Text(
                        text = "${channel.subscribers} подписчиков",
                        fontSize = 14.sp
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = channel.description,
                    fontSize = 14.sp
                )
            }
        }
    }else if (state.isLoading) {
        LoadingComponent(
            modifier = Modifier.fillMaxSize()
        )
    } else {
        ErrorComponent(
            modifier = Modifier.fillMaxSize(),
            onRetry = {
                onRefresh()
            }
        )
    }
}

@Composable
@Preview
fun ChannelScreenPreview() {
    ChannelScreen(
        state = ChannelScreenState(
            info = ExtendedChannelInfo(
                info = ChannelInfo(
                    1, 1, "Some title", "sometitle", "Lorem ipsum is simply dummy text", 100
                ),
                subscription = true
            ),
            isLoading = false,
            error = null
        ),
        onRefresh = {},
        onSubscription = {}
    )
}
