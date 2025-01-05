package mikhail.shell.video.hosting.presentation.channel.screen

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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import mikhail.shell.video.hosting.domain.models.SubscriptionState
import mikhail.shell.video.hosting.domain.models.SubscriptionState.NOT_SUBSCRIBED
import mikhail.shell.video.hosting.domain.models.SubscriptionState.SUBSCRIBED
import mikhail.shell.video.hosting.domain.models.Video
import mikhail.shell.video.hosting.domain.utils.isBlank
import mikhail.shell.video.hosting.presentation.utils.ErrorComponent
import mikhail.shell.video.hosting.presentation.utils.LoadingComponent
import mikhail.shell.video.hosting.presentation.utils.PrimaryButton
import mikhail.shell.video.hosting.presentation.utils.toFullSubscribers
import mikhail.shell.video.hosting.presentation.utils.toViews
import mikhail.shell.video.hosting.presentation.video.screen.toPresentation


@Composable
fun ChannelScreen(
    state: ChannelScreenState,
    onChannelRefresh: () -> Unit,
    onVideosRefresh: () -> Unit,
    onSubscription: (SubscriptionState) -> Unit,
    onVideoClick: (Long) -> Unit
) {
    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
            .verticalScroll(scrollState)
    ) {
        if (state.channel != null) {
            val channel = state.channel
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
                        fontSize = 24.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    val alias = if (!channel.alias.isBlank()) channel.alias else channel.channelId
                    Text(
                        text = "@$alias",
                        fontSize = 13.sp,
                        modifier = Modifier.padding(top = 10.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = channel.subscribers.toFullSubscribers(),
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            PrimaryButton(
                text = if (channel.subscription == SUBSCRIBED) "Отписаться" else "Подписаться",
                onClick = {
                    val subscriptionState = if (channel.subscription == SUBSCRIBED) NOT_SUBSCRIBED else SUBSCRIBED
                    onSubscription(subscriptionState)
                }
            )
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = channel.description,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else if (state.isChannelLoading) {
            LoadingComponent(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )
        } else {
            ErrorComponent(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                onRetry = {
                    onChannelRefresh()
                }
            )
        }
        if (state.videos != null) {
            LazyColumn(
                modifier = Modifier
                    .padding(top = 10.dp)
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
        } else if (state.isChannelLoading) {
            LoadingComponent(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(3f)
            )
        } else {
            ErrorComponent(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(3f),
                onRetry = {
                    onVideosRefresh()
                }
            )
        }
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
        modifier = modifier
            .fillMaxWidth()
            .clickable {
                onClick(video.videoId!!)
            }
            .padding(vertical = 10.dp)
    ) {
        AsyncImage(
            model = video.coverUrl,
            contentDescription = video.title,
            modifier = Modifier
                .fillMaxWidth(0.45f)
                .aspectRatio(16f / 9)
                .clip(RoundedCornerShape(6.dp))
                .background(MaterialTheme.colorScheme.secondaryContainer),
            contentScale = ContentScale.Crop
        )
        Column(
            modifier = Modifier
                .padding(start = 10.dp)
                .weight(1f)
        ) {
            Text(
                modifier = Modifier.padding(top = 7.dp),
                text = video.title,
                maxLines = 2,
                fontSize = 14.sp,
                lineHeight = 16.sp,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                modifier = Modifier.padding(top = 7.dp),
                maxLines = 2,
                fontSize = 12.sp,
                lineHeight = 14.sp,
                text = video.views.toViews() + " - " + video.dateTime.toPresentation(),
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }

}