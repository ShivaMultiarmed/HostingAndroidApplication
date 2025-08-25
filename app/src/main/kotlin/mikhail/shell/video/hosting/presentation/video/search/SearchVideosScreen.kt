package mikhail.shell.video.hosting.presentation.video.search

import androidx.activity.compose.LocalActivity
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import coil.compose.AsyncImage
import mikhail.shell.video.hosting.R
import mikhail.shell.video.hosting.presentation.utils.EmptyResultComponent
import mikhail.shell.video.hosting.presentation.utils.ErrorComponent
import mikhail.shell.video.hosting.presentation.utils.InputField
import mikhail.shell.video.hosting.presentation.utils.LoadingComponent
import mikhail.shell.video.hosting.presentation.utils.PrimaryProgressButton
import mikhail.shell.video.hosting.presentation.utils.StandardComplexErrorHandler
import mikhail.shell.video.hosting.presentation.utils.borderBottom
import mikhail.shell.video.hosting.presentation.utils.reachedBottom
import mikhail.shell.video.hosting.presentation.utils.toViews
import mikhail.shell.video.hosting.presentation.video.models.VideoWithChannelUi
import mikhail.shell.video.hosting.presentation.video.screen.toPresentation

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun SearchVideosScreen(
    state: SearchVideosScreenState,
    onEvent: (SearchScreenUiEvent) -> Unit
) {
    val windowSize = calculateWindowSizeClass(LocalActivity.current!!)
    val isWidthCompact = windowSize.widthSizeClass == WindowWidthSizeClass.Compact
    val snackBarHostState = remember { SnackbarHostState() }
    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface),
        topBar = {
            ConstraintLayout(
                modifier = Modifier
                    .fillMaxWidth()
                    .borderBottom(
                        strokeWidth = 3,
                        color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.4f)
                    )
            ) {
                val button = createRef()
                val errorMsg = state.queryError?.let { "" }
                InputField(
                    modifier = Modifier.fillMaxWidth(),
                    value = state.query,
                    onValueChange = {
                        onEvent(SearchScreenUiEvent.QueryChanged(it))
                    },
                    errorMsg = errorMsg,
                    placeholder = stringResource(R.string.video_search_label),
                    icon = Icons.Rounded.Search
                )
                PrimaryProgressButton(
                    modifier = Modifier.constrainAs(button) {
                        end.linkTo(parent.end, 10.dp)
                        top.linkTo(parent.top)
                        bottom.linkTo(parent.bottom)
                    },
                    enabled = state.query.isNotEmpty(),
                    onClick = {
                        onEvent(SearchScreenUiEvent.Submit)
                    },
                    icon = Icons.AutoMirrored.Rounded.Send
                )
            }
        },
        snackbarHost = {
            SnackbarHost(hostState = snackBarHostState)
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            val lazyGridState = rememberLazyGridState()
            val reachedBottom by remember { derivedStateOf { lazyGridState.reachedBottom(buffer = 4) } }
            if (state.videos != null) {
                if (state.videos.isNotEmpty()) {
                    LazyVerticalGrid(
                        modifier = Modifier
                            .fillMaxSize()
                            .then(
                                if (isWidthCompact) {
                                    Modifier
                                } else {
                                    Modifier
                                        .padding(horizontal = 10.dp)
                                        .padding(top = 10.dp)
                                }
                            ),
                        columns = GridCells.Adaptive(300.dp),
                        horizontalArrangement = Arrangement.spacedBy(if (isWidthCompact) 0.dp else 10.dp),
                        verticalArrangement = Arrangement.spacedBy(if (isWidthCompact) 0.dp else 10.dp),
                        state = lazyGridState
                    ) {
                        items(state.videos) {
                            VideoWithChannelSnippet(
                                modifier = Modifier
                                    .then(
                                        if (isWidthCompact) {
                                            Modifier
                                        } else {
                                            Modifier
                                                .clip(RoundedCornerShape(15.dp))
                                        }
                                    ),
                                videoWithChannel = it,
                                onClick = {
                                    onEvent(SearchScreenUiEvent.ClickedVideo(it))
                                }
                            )
                        }
                        item (
                            span = {
                                GridItemSpan(maxLineSpan)
                            }
                        ) {
                            if (state.isLoading) {
                                LoadingComponent(modifier = Modifier.fillMaxSize())
                            } else if (state.error != null) {
                                ErrorComponent(
                                    modifier = Modifier.fillMaxSize(),
                                    onRetry = {
                                        onEvent(SearchScreenUiEvent.Reload)
                                    }
                                )
                            }
                        }
                    }
                    LaunchedEffect(reachedBottom) {
                        if (reachedBottom && state.hasMore) {
                            onEvent(SearchScreenUiEvent.BottomReached)
                        }
                    }
                } else {
                    EmptyResultComponent(
                        modifier = Modifier
                            .padding(padding)
                            .fillMaxSize(),
                        message = stringResource(R.string.video_found_nothing)
                    )
                }
            } else if (state.isLoading) {
                LoadingComponent(modifier = Modifier.fillMaxSize())
            } else if (state.error != null) {
                ErrorComponent(
                    modifier = Modifier.fillMaxSize(),
                    onRetry = {
                        onEvent(SearchScreenUiEvent.Reload)
                    }
                )
            }
            StandardComplexErrorHandler(
                error = state.error,
                snackBarHostState = snackBarHostState
            )
        }
    }
}

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun VideoWithChannelSnippet(
    modifier: Modifier = Modifier,
    videoWithChannel: VideoWithChannelUi,
    onClick: (Long) -> Unit
) {
    val context = LocalContext.current
    val windowSizeClass = calculateWindowSizeClass(LocalActivity.current!!)
    val isWidthCompact = windowSizeClass.widthSizeClass == WindowWidthSizeClass.Compact
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .clickable {
                onClick(videoWithChannel.videoId)
            }
            .then(
                if (isWidthCompact) {
                    Modifier
                } else {
                    Modifier.padding(10.dp)
                }
            )
    ) {
        AsyncImage(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9)
                .then(
                    if (isWidthCompact)
                        Modifier
                    else
                        Modifier
                            .clip(RoundedCornerShape(10.dp))
                )
                .background(MaterialTheme.colorScheme.secondaryContainer),
            model = videoWithChannel.videoCoverUrl,
            contentDescription = videoWithChannel.videoTitle,
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
                model = videoWithChannel.channelAvatarUrl,
                contentDescription = videoWithChannel.channelTitle,
                contentScale = ContentScale.Crop
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 10.dp)
            ) {
                Text(
                    text = videoWithChannel.videoTitle,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 14.sp,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 2
                )
                val modId = "view_icon"
                val videoLabel = remember {
                    buildAnnotatedString {
                        append(videoWithChannel.channelTitle)
                        append(" - ")
                        append(videoWithChannel.views.toViews())
                        append(" ")
                        appendInlineContent(modId, "[view_icon]")
                        append(" - ")
                        append(videoWithChannel.dateTime.toPresentation(context))
                    }
                }
                val inlineContent = mapOf(
                    modId to InlineTextContent(
                        placeholder = Placeholder(
                            width = 13.sp,
                            height = 13.sp,
                            placeholderVerticalAlign = PlaceholderVerticalAlign.Center
                        )
                    ) {
                        Icon(
                            modifier = Modifier.fillMaxSize(),
                            imageVector = Icons.Rounded.Visibility,
                            tint = MaterialTheme.colorScheme.onSurface,
                            contentDescription = videoWithChannel.views.toString()
                        )
                    }
                )
                Text(
                    text = videoLabel,
                    inlineContent = inlineContent,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp,
                    lineHeight = 13.sp,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 2,
                )
            }
        }
    }
}