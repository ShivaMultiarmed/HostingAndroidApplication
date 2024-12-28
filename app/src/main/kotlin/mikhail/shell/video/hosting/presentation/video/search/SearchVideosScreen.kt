package mikhail.shell.video.hosting.presentation.video.search

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import mikhail.shell.video.hosting.domain.models.VideoWithChannel
import mikhail.shell.video.hosting.presentation.utils.InputField
import mikhail.shell.video.hosting.presentation.utils.PrimaryButton
import mikhail.shell.video.hosting.presentation.utils.toViews
import mikhail.shell.video.hosting.presentation.video.screen.toPresentation

@Composable
fun SearchVideosScreen(
    modifier: Modifier = Modifier,
    state: SearchVideosScreenState,
    onSubmit: (String) -> Unit,
    onScrollToBottom: (Long, Int) -> Unit,
    onVideoClick: (Long) -> Unit
) {
    Scaffold (
        modifier = modifier.fillMaxSize(),
        topBar = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                var query by remember {
                    mutableStateOf("")
                }
                InputField(
                    value = query,
                    onValueChange = {
                        query = it
                    }
                )
                PrimaryButton(
                    onClick = {
                        onSubmit(query)
                    }
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Search,
                        tint = Color.White,
                        contentDescription = "Поиск"
                    )
                }
            }
        }
    ) {
        if (state.videos != null) {
            LazyColumn (
                modifier = Modifier.padding(it)
                    .fillMaxSize()
            ) {
                items(state.videos) {
                    VideoWithChannelSnippet(
                        videoWithChannel = it,
                        onClick = onVideoClick
                    )
                }
            }
        }
    }
}

@Composable
fun VideoWithChannelSnippet(
    videoWithChannel: VideoWithChannel,
    onClick: (Long) -> Unit
) {
    val video = videoWithChannel.video
    val channel = videoWithChannel.channel
    Column (
        modifier = Modifier.fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .clickable {
                onClick(video.videoId!!)
            }
    ) {
        AsyncImage(
            modifier = Modifier.fillMaxWidth()
                .aspectRatio(16f / 9),
            model = video.coverUrl,
            contentDescription = video.title,
            contentScale = ContentScale.Crop
        )
        Row (
            modifier = Modifier.fillMaxWidth()
                .height(100.dp)
        ) {
            AsyncImage(
                modifier = Modifier.size(48.dp)
                    .clip(CircleShape),
                model = channel.avatarUrl,
                contentDescription = channel.title,
                contentScale = ContentScale.Crop
            )
            Column (
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = video.title,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 18.sp
                )
                Row {
                    Text(
                        text = channel.title,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 18.sp
                    )
                    Text(
                        text = video.views.toViews(),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 18.sp
                    )
                    Text(
                        text = video.dateTime.toPresentation(),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 18.sp
                    )
                }
            }
        }
    }
}


@Composable
@Preview
fun SearchVideosScreenPreview() {
    SearchVideosScreen(
        state = SearchVideosScreenState(),
        onSubmit = {},
        onScrollToBottom = {a, b ->

        },
        onVideoClick = {}
    )
}