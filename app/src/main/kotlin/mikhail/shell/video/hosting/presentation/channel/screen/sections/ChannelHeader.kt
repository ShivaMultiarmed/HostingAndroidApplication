package mikhail.shell.video.hosting.presentation.channel.screen.sections

import android.app.Activity
import android.content.res.Configuration.ORIENTATION_PORTRAIT
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowHeightSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import coil.compose.AsyncImage
import mikhail.shell.video.hosting.domain.models.ChannelWithUser
import mikhail.shell.video.hosting.domain.models.SubscriptionState
import mikhail.shell.video.hosting.domain.models.SubscriptionState.NOT_SUBSCRIBED
import mikhail.shell.video.hosting.domain.models.SubscriptionState.SUBSCRIBED
import mikhail.shell.video.hosting.domain.utils.isNotBlank
import mikhail.shell.video.hosting.presentation.utils.ErrorComponent
import mikhail.shell.video.hosting.presentation.utils.LoadingComponent
import mikhail.shell.video.hosting.presentation.utils.PrimaryButton
import mikhail.shell.video.hosting.presentation.utils.toFullSubscribers

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun ColumnScope.ChannelHeader(
    channel: ChannelWithUser?,
    loading: Boolean,
    onSubscription: (SubscriptionState) -> Unit,
    onChannelRefresh: () -> Unit
) {
    val context = LocalContext.current
    val windowSizeClass = calculateWindowSizeClass(context as Activity)
    if (channel != null) {
        var hasCover by rememberSaveable { mutableStateOf<Boolean?>(null) }
        if (windowSizeClass.widthSizeClass == WindowWidthSizeClass.Compact) {
            ChannelHeaderShrinked(
                hasCover,
                { hasCover = it },
                channel,
                onSubscription
            )
        } else if (windowSizeClass.heightSizeClass == WindowHeightSizeClass.Compact) {
            ChannelHeaderWide(
                channel,
                onSubscription
            )
        } else {
            ChannelHeaderExpanded(
                hasCover,
                { hasCover = it },
                channel,
                onSubscription
            )
        }
    } else if (loading) {
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
            onRetry = onChannelRefresh
        )
    }
}

@Composable
fun ColumnScope.ChannelHeaderShrinked(
    hasCover: Boolean?,
    coverUrlAssignment: (Boolean) -> Unit,
    channel: ChannelWithUser,
    onSubscription: (SubscriptionState) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.Top
    ) {
        ChannelCover(
            hasCover = hasCover,
            coverUrlAssignment = coverUrlAssignment,
            coverUrl = channel.coverUrl
        )
        Row(
            Modifier
                .fillMaxWidth()
                .padding(top = 13.dp, bottom = 7.dp)
        ) {
            ChannelAvatar(
                modifier = Modifier,
                avatarUrl = channel.avatarUrl
            )
            Column(
                modifier = Modifier.padding(start = 10.dp)
            ) {
                ChannelTitle(title = channel.title)
                ChannelAlias(alias = channel.alias)
                SubscriberNumberText(subscribers = channel.subscribers)
            }
        }
        SubscriptionButton(state = channel.subscription, onSubscription = onSubscription)
    }
}

@Composable
fun ColumnScope.ChannelHeaderWide(
    channel: ChannelWithUser,
    onSubscription: (SubscriptionState) -> Unit
) {
    ConstraintLayout(
        modifier = Modifier.fillMaxWidth()
    ) {
        val avatarRef = createRef()
        ChannelAvatar(
            modifier = Modifier.constrainAs(avatarRef) {
                start.linkTo(parent.start)
                top.linkTo(parent.top)
                bottom.linkTo(parent.bottom)
            },
            avatarUrl = channel.avatarUrl
        )
        val briefRef = createRef()
        Column(
            modifier = Modifier.constrainAs(briefRef) {
                top.linkTo(parent.top)
                bottom.linkTo(parent.bottom)
                start.linkTo(avatarRef.end, 10.dp)
            }
        ) {
            ChannelTitle(title = channel.title)
            ChannelAlias(alias = channel.alias)
            SubscriberNumberText(subscribers = channel.subscribers)
        }
        val subButton = createRef()
        SubscriptionButton(
            modifier = Modifier.constrainAs(subButton) {
                end.linkTo(parent.end)
                top.linkTo(parent.top)
                bottom.linkTo(parent.bottom)
            },
            state = channel.subscription,
            onSubscription = onSubscription
        )
    }
}

@Composable
fun ColumnScope.ChannelHeaderExpanded(
    hasCover: Boolean?,
    coverUrlAssignment: (Boolean) -> Unit,
    channel: ChannelWithUser,
    onSubscription: (SubscriptionState) -> Unit
) {
    ConstraintLayout(
        modifier = Modifier.fillMaxWidth()
    ) {
        val coverRef = createRef()
        ChannelCover(
            modifier = Modifier.then(
                if (hasCover == true) {
                    Modifier.fillMaxWidth()
                        .constrainAs(coverRef) {
                            top.linkTo(parent.top)
                            start.linkTo(parent.start)
                            end.linkTo(parent.end)
                        }
                } else Modifier
            ),
            hasCover = hasCover,
            coverUrlAssignment = coverUrlAssignment,
            coverUrl = channel.coverUrl,
        )
        val avatarRef = createRef()
        ChannelAvatar(
            modifier = Modifier.constrainAs(avatarRef) {
                if (hasCover == true) {
                    top.linkTo(coverRef.bottom, -65.dp)
                } else {
                    top.linkTo(parent.top)
                }
                start.linkTo(parent.start)
                end.linkTo(parent.end)
            },
            avatarUrl = channel.avatarUrl
        )
        val annotationRef = createRef()
        Column (
            modifier = Modifier
                .fillMaxWidth()
                .constrainAs(annotationRef) {
                    top.linkTo(avatarRef.bottom, 16.dp)
                },
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ChannelTitle(title = channel.title)
            Row (
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                ChannelAlias(alias = channel.alias)
                SubscriptionButton(
                    modifier = Modifier,
                    state = channel.subscription,
                    onSubscription = onSubscription
                )
                SubscriberNumberText(subscribers = channel.subscribers)
            }
        }
    }
}

@Composable
fun ChannelCover(
    modifier: Modifier = Modifier,
    hasCover: Boolean?,
    coverUrlAssignment: (Boolean) -> Unit,
    coverUrl: String?
) {
    Box(
        modifier = modifier
            .then(
                if (hasCover == true) {
                    Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                } else {
                    Modifier.size(0.dp)
                }
            )

    ) {
        AsyncImage(
            model = coverUrl,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
            onSuccess = {
                coverUrlAssignment(true)
            },
            onError = {
                coverUrlAssignment(false)
            }
        )
    }
}

@Composable
fun ChannelAvatar(
    modifier: Modifier = Modifier,
    avatarUrl: String?
) {
    AsyncImage(
        model = avatarUrl,
        contentDescription = "Аватар канала",
        contentScale = ContentScale.Crop,
        modifier = modifier
            .size(80.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant)
    )
}


@Composable
fun ChannelDescription(
    modifier: Modifier = Modifier,
    description: String
) {
    Text(
        modifier = modifier.fillMaxWidth(),
        text = description,
        fontSize = 13.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
fun SubscriptionButton(
    modifier: Modifier = Modifier.fillMaxWidth(),
    state: SubscriptionState,
    onSubscription: (SubscriptionState) -> Unit
) {
    PrimaryButton(
        modifier = modifier,
        text = if (state == SUBSCRIBED) "Отписаться" else "Подписаться",
        onClick = {
            val subscriptionState =
                if (state == SUBSCRIBED) NOT_SUBSCRIBED else SUBSCRIBED
            onSubscription(subscriptionState)
        },
        isActivated = state == SUBSCRIBED
    )
}

@Composable
fun SubscriberNumberText(
    modifier: Modifier = Modifier,
    subscribers: Long
) {
    Text(
        modifier = modifier,
        text = subscribers.toFullSubscribers(),
        fontSize = 13.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
fun ChannelTitle(
    modifier: Modifier = Modifier,
    title: String
) {
    Text(
        modifier = modifier,
        text = title,
        fontSize = 24.sp,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
fun ChannelAlias(
    modifier: Modifier = Modifier,
    alias: String?
) {
    if (alias.isNotBlank()) {
        Text(
            text = "@${alias}",
            fontSize = 13.sp,
            modifier = modifier.padding(top = 10.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}