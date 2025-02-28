package mikhail.shell.video.hosting.presentation.video.screen

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import androidx.annotation.OptIn
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Repeat
import androidx.compose.material.icons.outlined.ThumbDown
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.ThumbDown
import androidx.compose.material.icons.rounded.ThumbUp
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.times
import androidx.core.app.ActivityCompat
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import coil.compose.AsyncImage
import mikhail.shell.video.hosting.domain.models.LikingState
import mikhail.shell.video.hosting.domain.models.LikingState.DISLIKED
import mikhail.shell.video.hosting.domain.models.LikingState.LIKED
import mikhail.shell.video.hosting.domain.models.LikingState.NONE
import mikhail.shell.video.hosting.domain.models.SubscriptionState
import mikhail.shell.video.hosting.domain.models.SubscriptionState.NOT_SUBSCRIBED
import mikhail.shell.video.hosting.domain.models.SubscriptionState.SUBSCRIBED
import mikhail.shell.video.hosting.domain.services.VideoDownloadingService
import mikhail.shell.video.hosting.presentation.utils.ActionButton
import mikhail.shell.video.hosting.presentation.utils.ContextMenu
import mikhail.shell.video.hosting.presentation.utils.Dialog
import mikhail.shell.video.hosting.presentation.utils.EditButton
import mikhail.shell.video.hosting.presentation.utils.ErrorComponent
import mikhail.shell.video.hosting.presentation.utils.LoadingComponent
import mikhail.shell.video.hosting.presentation.utils.MenuItem
import mikhail.shell.video.hosting.presentation.utils.PlayerComponent
import mikhail.shell.video.hosting.presentation.utils.PrimaryButton
import mikhail.shell.video.hosting.presentation.utils.toSubscribers
import mikhail.shell.video.hosting.presentation.utils.toViews
import mikhail.shell.video.hosting.ui.theme.Black
import java.time.Duration
import java.time.LocalDateTime

@OptIn(UnstableApi::class)
@Composable
fun VideoScreen(
    userId: Long,
    state: VideoScreenState,
    onRefresh: () -> Unit,
    onRate: (LikingState) -> Unit,
    onSubscribe: (SubscriptionState) -> Unit,
    player: Player,
    onChannelLinkClick: (Long) -> Unit,
    onView: () -> Unit,
    onDelete: () -> Unit,
    onUpdate: (Long) -> Unit,
) {
    LaunchedEffect(state.isViewed) {
        if (state.isViewed) {
            onView()
        }
    }
    val context = LocalContext.current
    if (state.videoDetails != null) {
        var videoInfoExpanded by rememberSaveable { mutableStateOf(false) }
        val idealVideoWidth = LocalConfiguration.current.screenWidthDp.dp
        val idealMinVideoHeight = 9f / 16 * idealVideoWidth
        val idealMaxVideoHeight = 16f / 9 * idealVideoWidth
        val scrollState = rememberScrollState()
        val animatedHeight by animateDpAsState(
            targetValue = if (videoInfoExpanded) idealMinVideoHeight else 0.dp,
            animationSpec = tween(durationMillis = 250),
            label = "videoAnimation"
        )
        val video = state.videoDetails.video
        val channel = state.videoDetails.channel
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.onBackground)
            ) {
                PlayerComponent(
                    modifier = Modifier
                        .fillMaxWidth()
                        .then(
                            if ((videoInfoExpanded && animatedHeight < idealMaxVideoHeight
                                        || !videoInfoExpanded && animatedHeight >= idealMinVideoHeight)
                                && (player.playerError != null || player.isLoading)
                            )
                                Modifier.height(animatedHeight)
                            else
                                Modifier.wrapContentHeight()
                        ),
                    player = player
                )
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Black)
                    .clip(
                        RoundedCornerShape(
                            topStart = 10.dp,
                            topEnd = 10.dp
                        )
                    )
                    .background(MaterialTheme.colorScheme.background)
                    .padding(12.dp)
                    .verticalScroll(scrollState)
                    .draggable(
                        orientation = Orientation.Vertical,
                        state = rememberDraggableState {
                            if (it < -50f) {
                                videoInfoExpanded = true
                            } else if (it > 50f) {
                                videoInfoExpanded = false
                            }
                        }
                    )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    Text(
                        text = video.title,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = 20.sp,
                        maxLines = 2,
                        lineHeight = 22.sp
                    )
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.background)
                        .padding(vertical = 7.dp),
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = video.views.toViews(),
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = video.dateTime.toPresentation(),
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 16.sp
                    )
                    Text(
                        text = "Ещё",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (channel.ownerId == userId) {
                        var isDeletingDialogOpen by remember { mutableStateOf(false) }
                        var isAdvancedDialogOpen by remember { mutableStateOf(false) }
                        Box {
                            EditButton(
                                modifier = Modifier.size(22.dp),
                                imageVector = Icons.Rounded.MoreVert,
                                onClick = {
                                    isAdvancedDialogOpen = true
                                }
                            )
                            if (isAdvancedDialogOpen) {
                                ContextMenu(
                                    modifier = Modifier,
                                    isExpanded = true,
                                    menuItems = listOf(
                                        MenuItem(
                                            title = "Редактировать",
                                            onClick = {
                                                onUpdate(video.videoId!!)
                                            }
                                        ),
                                        MenuItem(
                                            title = "Удалить",
                                            onClick = {
                                                isDeletingDialogOpen = true
                                            }
                                        )
                                    ),
                                    onDismiss = {
                                        isAdvancedDialogOpen = false
                                    }
                                )
                            }
                        }
                        if (isDeletingDialogOpen) {
                            Dialog(
                                onSubmit = onDelete,
                                onDismiss = {
                                    isDeletingDialogOpen = false
                                },
                                dialogTitle = "Удалить видео",
                                dialogDescription = "Вы уверены, что хотите продолжить?"
                            )
                        }
                    }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .clickable { onChannelLinkClick(channel.channelId!!) }
                            .weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AsyncImage(
                            model = channel.avatarUrl,
                            contentDescription = "Ссылка на канал",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.secondaryContainer)
                        )
                        Text(
                            text = channel.title,
                            modifier = Modifier
                                .weight(1f)
                                .padding(start = 13.dp),
                            fontSize = 15.sp,
                            lineHeight = 17.sp,
                            overflow = TextOverflow.Ellipsis,
                            maxLines = 1
                        )
                    }
                    Text(
                        text = channel.subscribers.toSubscribers() + " \uD83D\uDC64",
                        fontSize = 13.sp,
                        modifier = Modifier.padding(end = 5.dp)
                    )
                    val subscriptionText = if (channel.subscription == SUBSCRIBED)
                        "Отписаться"
                    else "Подписаться"
                    PrimaryButton(
                        text = subscriptionText,
                        onClick = {
                            val subscriptionState = if (channel.subscription == SUBSCRIBED)
                                NOT_SUBSCRIBED
                            else SUBSCRIBED
                            onSubscribe(subscriptionState)
                        },
                        isActivated = channel.subscription == SUBSCRIBED
                    )
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    val likeVector =
                        when (video.liking) {
                            LIKED -> Icons.Rounded.ThumbUp
                            else -> Icons.Outlined.ThumbUp
                        }

                    ActionButton(
                        icon = likeVector,
                        text = video.likes.toString(),
                        onClick = {
                            if (video.liking != LIKED)
                                onRate(LIKED)
                            else
                                onRate(NONE)
                        }
                    )
                    val dislikeVector =
                        when (video.liking) {
                            DISLIKED -> Icons.Rounded.ThumbDown
                            else -> Icons.Outlined.ThumbDown
                        }
                    ActionButton(
                        icon = dislikeVector,
                        text = video.dislikes.toString(),
                        onClick = {
                            if (video.liking != DISLIKED)
                                onRate(DISLIKED)
                            else
                                onRate(NONE)
                        }
                    )
                    ActionButton(
                        icon = Icons.Outlined.Repeat,
                        text = "Поделиться",
                        onClick = {

                        }
                    )
                    ActionButton(
                        icon = Icons.Outlined.Download,
                        text = "Скачать",
                        onClick = {
                            Intent(context, VideoDownloadingService::class.java).also {
                                it.action = "mikhail.shell.video.hosting.ACTION_LAUNCH_DOWNLOADING"
                                it.putExtra("videoId", state.videoDetails.video.videoId!!)
                                context.startService(it)
                            }
                        }
                    )
                }
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.surfaceContainer)
                        .padding(10.dp)
                ) {
                    Text(
                        text = "Комментарии"
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .padding(top = 10.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                                .padding(vertical = 3.dp, horizontal = 10.dp)
                        ) {
                            Text(
                                text = "Введите комментарий",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
        LaunchedEffect(Unit) {
            if (ActivityCompat.checkSelfPermission(context, "android.permission.SYSTEM_ALERT_WINDOW") == PackageManager.PERMISSION_DENIED) {
                ActivityCompat.requestPermissions(
                    context as Activity,
                    arrayOf("android.permission.SYSTEM_ALERT_WINDOW"),
                    0
                )
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
//@Preview(
//    //name = "Dark Mode Preview",
//    //uiMode = Configuration.UI_MODE_NIGHT_YES,
//    uiMode = Configuration.UI_MODE_NIGHT_NO,
//    showBackground = true
//)
//fun VideoScreenPreview() {
//    VideoScreen(
//        state = VideoScreenState(
//
//            ExtendedVideoInfo(
//                videoInfo = VideoInfo(
//                    1,
//                    1,
//                    "Какой-то заголовок видео",
//                    LocalDateTime.of(
//                        2024,
//                        12,
//                        9,
//                        10,
//                        9
//                    ),
//                    views = 100,
//                    likes = 23,
//                    dislikes = 14
//                ),
//                liking = true
//            )
//        ),
//        exoPlayerConnection = { context ->
//            PlayerView(context)
//        },
//        onRefresh = {},
//        onRate = {},
//        onSubscribe = {}
//    )
//}

fun LocalDateTime.toPresentation(): String {
    val now = LocalDateTime.now()
    return if (now.minusMinutes(10) < this)
        "Только что"
    else if (now.minusMinutes(60) < this) {
        val diff = Duration.between(this, now).toMinutes().toInt()
        when (diff % 10) {
            1 -> "$diff минуту назад назад"
            in 2..4 -> "$diff минуты назад"
            else -> "$diff минут назад"
        }
    } else if (now.minusHours(24) < this) {
        val diff = Duration.between(this, now).toHours().toInt()
        when (diff % 10) {
            1 -> "$diff час назад"
            in 2..4 -> "$diff часа назад"
            else -> "$diff часов назад"
        }
    } else if (now.minusDays(30) < this) {
        val diff = Duration.between(this, now).toDays().toInt()

        when (diff % 10) {
            1 -> if (diff != 1) "$diff день назад" else "Вчера"
            in 2..4 -> "$diff дня назад"
            else -> "$diff дней назад"
        }
    } else if (now.minusMonths(12) < this) {
        val diff = Duration.between(this, now).toDays().div(30).toInt()
        when (diff % 10) {
            1 -> "$diff месяц назад"
            in 2..4 -> "$diff месяца назад"
            else -> "$diff месяцев назад"
        }
    } else {
        val diff = Duration.between(this, now).toDays().div(30).div(12).toInt()
        when (diff % 10) {
            1 -> "$diff год назад"
            in 2..4 -> "$diff года назад"
            else -> "$diff лет назад"
        }
    }
}