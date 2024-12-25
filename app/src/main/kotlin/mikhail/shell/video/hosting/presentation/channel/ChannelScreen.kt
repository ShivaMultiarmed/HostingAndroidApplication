package mikhail.shell.video.hosting.presentation.channel

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import mikhail.shell.video.hosting.domain.models.Video
import mikhail.shell.video.hosting.presentation.utils.ErrorComponent
import mikhail.shell.video.hosting.presentation.utils.LoadingComponent
import mikhail.shell.video.hosting.presentation.utils.toViews
import mikhail.shell.video.hosting.presentation.video.page.toPresentation


@Composable
fun ChannelScreen(
    state: ChannelScreenState,
    onRefresh: () -> Unit,
    onSubscription: () -> Unit,
    onVideoClick: (Long) -> Unit
) {
    if (state.channel != null) {
        val scrollState = rememberScrollState()
        val channel = state.channel
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp)
                .verticalScroll(scrollState)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.secondaryContainer)
            ) {
                AsyncImage(
                    model = channel.coverUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage(
                    model = channel.avatarUrl,
                    contentDescription = "Аватар канала",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                )
                Column(
                    modifier = Modifier
                        .weight(1f)
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
            LazyColumn (
                modifier = Modifier
                    .padding(vertical = 10.dp)
                    .weight(1f)
            ) {
                items(state.videos) {
                    VideoSnippet(
                        video = it,
                        onClick = {
                            onVideoClick(it)
                        }
                    )
                }
            }
        }
    } else if (state.isLoading) {
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

//@Composable
//@Preview
//fun ChannelScreenPreview() {
//    ChannelScreen(
//        state = ChannelScreenState(
//            info = ExtendedChannelInfo(
//                info = ChannelInfo(
//                    1, 1, "Some title", "sometitle", "Lorem ipsum is simply dummy text", 100
//                ),
//                subscription = true
//            ),
//            isLoading = false,
//            error = null
//        ),
//        onRefresh = {},
//        onSubscription = {},
//        onVideoClick = {}
//    )
//}


@Composable
fun VideoSnippet(
    modifier: Modifier = Modifier,
    video: Video,
    onClick: (Long) -> Unit
) {
    Row(
        modifier = modifier.fillMaxWidth()
            .clickable {
                onClick(video.videoId)
            }
    ) {
        AsyncImage(
            model = video.coverUrl,
            contentDescription = video.title,
            modifier = Modifier.fillMaxWidth(0.5f)
                .aspectRatio(16f / 9)
                .clip(RoundedCornerShape(6.dp))
                .background(MaterialTheme.colorScheme.secondaryContainer),
            contentScale = ContentScale.Crop
        )
        Column (
            modifier = Modifier
                .padding(start = 10.dp)
                .weight(1f)
        ) {
            Text(
                text = video.title,
                maxLines = 3,
                fontSize = 16.sp
            )
            Text(
                text = video.views.toViews() + " - " + video.dateTime.toPresentation()
            )
        }
    }

}