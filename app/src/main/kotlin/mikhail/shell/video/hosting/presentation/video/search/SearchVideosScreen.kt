package mikhail.shell.video.hosting.presentation.video.search

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Send
import androidx.compose.material.icons.rounded.Title
import androidx.compose.material.icons.rounded.VideoLibrary
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import coil.compose.AsyncImage
import mikhail.shell.video.hosting.domain.models.VideoWithChannel
import mikhail.shell.video.hosting.domain.utils.isNotBlank
import mikhail.shell.video.hosting.presentation.utils.EmptyResultComponent
import mikhail.shell.video.hosting.presentation.utils.ErrorComponent
import mikhail.shell.video.hosting.presentation.utils.InputField
import mikhail.shell.video.hosting.presentation.utils.LoadingComponent
import mikhail.shell.video.hosting.presentation.utils.PrimaryButton
import mikhail.shell.video.hosting.presentation.utils.borderBottom
import mikhail.shell.video.hosting.presentation.utils.toViews
import mikhail.shell.video.hosting.presentation.video.screen.toPresentation
import mikhail.shell.video.hosting.ui.theme.VideoHostingTheme

@Composable
fun SearchVideosScreen(
    modifier: Modifier = Modifier,
    state: SearchVideosScreenState,
    onSubmit: (String) -> Unit,
    onScrollToBottom: (Long, Int) -> Unit,
    onVideoClick: (Long) -> Unit
) {
    var query by rememberSaveable {
        mutableStateOf("")
    }
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            ConstraintLayout (
                modifier = Modifier
                    .borderBottom(
                        strokeWidth = 3,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                    .fillMaxWidth()
            ) {
                val (input, button) = createRefs()
                var errorMsg by rememberSaveable { mutableStateOf<String?>(null) }
                InputField(
                    modifier = Modifier.fillMaxWidth(),
                    value = query,
                    onValueChange = {
                        query = it
                    },
                    errorMsg = errorMsg,
                    placeholder = "Искать",
                    icon = Icons.Rounded.Search
                )
                if (query.isNotBlank()) {
                    PrimaryButton(
                        modifier = Modifier.constrainAs(button) {
                            end.linkTo(parent.end, 10.dp)
                            top.linkTo(parent.top)
                            bottom.linkTo(parent.bottom)
                        },
                        icon = Icons.Rounded.Send,
                        onClick = {
                            if (query.isNotEmpty()) {
                                errorMsg = null
                                onSubmit(query)
                            }
                        }
                    )
                }
            }
        }
    ) {
        if (state.videos != null) {
            if (state.videos.isNotEmpty()) {
                LazyColumn(
                    modifier = Modifier
                        .padding(it)
                        .fillMaxSize()
                ) {
                    items(state.videos) {
                        VideoWithChannelSnippet(
                            videoWithChannel = it,
                            onClick = onVideoClick
                        )
                    }
                }
            } else {
                EmptyResultComponent(
                    modifier = modifier.padding(it).fillMaxSize(),
                    message = "Ничего не найдено"
                )
            }
        } else if (state.error != null) {
            ErrorComponent(
                modifier = modifier.fillMaxSize(),
                onRetry = {
                    if (query.isNotEmpty())
                        onSubmit(query)
                }
            )
        } else if (state.isLoading) {
            LoadingComponent(
                modifier = modifier.fillMaxSize()
            )
        } else {
            Box(
                modifier = modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Введите запрос\n" +
                            "Чтобы найти все видео введите пробел",
                    textAlign = TextAlign.Center
                )
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
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .clickable {
                onClick(video.videoId!!)
            }.padding(top = 10.dp)
    ) {
        AsyncImage(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9)
                .background(MaterialTheme.colorScheme.secondaryContainer),
            model = video.coverUrl,
            contentDescription = video.title,
            contentScale = ContentScale.Crop
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .padding(10.dp)
        ) {
            AsyncImage(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape),
                model = channel.avatarUrl,
                contentDescription = channel.title,
                contentScale = ContentScale.Crop
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 10.dp)
            ) {
                Text(
                    text = video.title,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 14.sp,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 2
                )
                Text(
                    text = channel.title + " - " + video.views.toViews() + " - " + video.dateTime.toPresentation(),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp,
                    lineHeight = 13.sp,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 2
                )
            }
        }
    }
}


@Composable
@Preview
fun SearchVideosScreenPreview() {
    VideoHostingTheme {
        SearchVideosScreen(
            state = SearchVideosScreenState(),
            onSubmit = {},
            onScrollToBottom = { a, b ->

            },
            onVideoClick = {}
        )
    }
}