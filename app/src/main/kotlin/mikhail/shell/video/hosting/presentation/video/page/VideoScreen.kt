package mikhail.shell.video.hosting.presentation.video.page

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Repeat
import androidx.compose.material.icons.outlined.ThumbDown
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.ThumbDown
import androidx.compose.material.icons.rounded.ThumbUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.paint
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.fastCbrt
import mikhail.shell.video.hosting.domain.models.ExtendedVideoInfo
import mikhail.shell.video.hosting.domain.models.VideoInfo
import mikhail.shell.video.hosting.presentation.utils.ErrorComponent
import mikhail.shell.video.hosting.presentation.utils.LoadingComponent
import mikhail.shell.video.hosting.ui.theme.VideoHostingTheme
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Composable
fun VideoScreen(
    state: VideoScreenState,
    onRefresh: () -> Unit,
    onRate: (Boolean) -> Unit,
    onSubscribe: (Boolean) -> Unit
) {
    if (state.extendedVideoInfo != null) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9)
                    .background(MaterialTheme.colorScheme.onBackground)
            ) {

            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.onBackground)
                    .clip(
                        RoundedCornerShape(
                            topStart = 10.dp,
                            topEnd = 10.dp
                        )
                    )
                    .background(MaterialTheme.colorScheme.background)
                    .padding(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    Text(
                        text = state.extendedVideoInfo?.videoInfo?.title!!,
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
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = state.extendedVideoInfo?.videoInfo?.views.toString() + " просмотров",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = state.extendedVideoInfo?.videoInfo?.dateTime!!.toPresentation(),
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 16.sp
                    )
                    Text(
                        text = "Ещё",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    //Image()
                    Text(
                        text = "Канал №" + state.extendedVideoInfo?.videoInfo?.channelId.toString(),
                        fontSize = 16.sp
                    )
                    Text(
                        text = "150 тыс.",
                        fontSize = 13.sp,
                        modifier = Modifier.weight(1f)
                    )
                    Button(
                        contentPadding = PaddingValues(4.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        modifier = Modifier.height(38.dp),
                        onClick = {

                        },
                    ) {
                        Text(
                            text = "Подписаться",
                            fontSize = 13.sp
                        )
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    val likeVector =
                        when(state.extendedVideoInfo.liking){
                            true -> Icons.Rounded.ThumbUp
                            false, null -> Icons.Outlined.ThumbUp
                        }

                    VideoButton(
                        icon = likeVector,
                        text = state.extendedVideoInfo.videoInfo.likes.toString(),
                        onClick = {
                            onRate(true)
                        }
                    )
                    val dislikeVector =
                        when(state.extendedVideoInfo.liking) {
                            false -> Icons.Rounded.ThumbDown
                            true, null -> Icons.Outlined.ThumbDown
                        }
                    VideoButton(
                        icon = dislikeVector,
                        text = state.extendedVideoInfo.videoInfo.dislikes.toString(),
                        onClick = {
                            onRate(false)
                        }
                    )
                    VideoButton(
                        icon = Icons.Outlined.Repeat,
                        text = "Поделиться",
                        onClick = {

                        }
                    )
                    VideoButton(
                        icon = Icons.Outlined.Download,
                        text = "Скачать",
                        onClick = {

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

@Composable
@Preview(
    //name = "Dark Mode Preview",
    //uiMode = Configuration.UI_MODE_NIGHT_YES,
    uiMode = Configuration.UI_MODE_NIGHT_NO,
    showBackground = true
)
fun VideoScreenPreview() {
    VideoScreen(
        state = VideoScreenState(
            ExtendedVideoInfo(
                videoInfo = VideoInfo(
                    1,
                    1,
                    "Какой-то заголовок видео",
                    LocalDateTime.of(
                        2024,
                        12,
                        9,
                        10,
                        9
                    ),
                    views = 100,
                    likes = 23,
                    dislikes = 14
                ),
                liking = true
            )
        ),
        onRefresh = {},
        onRate = {},
        onSubscribe = {}
    )
}

@Composable
fun VideoButton(
    icon: ImageVector,
    text: String,
    onClick: () -> Unit
) {
    Button(
        contentPadding = PaddingValues(0.dp),
        onClick = onClick,
        modifier = Modifier.height(28.dp),
        colors = ButtonDefaults.buttonColors(
            contentColor = MaterialTheme.colorScheme.onSurface,
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        shape = CircleShape
    ) {
        Row(
            modifier = Modifier,
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = text,
                fontSize = 12.sp
            )
        }
    }
}

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