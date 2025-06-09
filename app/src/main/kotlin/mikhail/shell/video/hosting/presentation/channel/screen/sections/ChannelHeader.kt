package mikhail.shell.video.hosting.presentation.channel.screen.sections

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import coil.compose.AsyncImage
import coil.compose.AsyncImagePainter
import coil.compose.AsyncImagePainter.State.Success
import coil.request.ImageRequest
import mikhail.shell.video.hosting.R
import mikhail.shell.video.hosting.domain.models.ChannelWithUser
import mikhail.shell.video.hosting.domain.models.SubscriptionState
import mikhail.shell.video.hosting.domain.models.SubscriptionState.NOT_SUBSCRIBED
import mikhail.shell.video.hosting.domain.models.SubscriptionState.SUBSCRIBED
import mikhail.shell.video.hosting.domain.utils.isNotBlank
import mikhail.shell.video.hosting.presentation.utils.ContextMenu
import mikhail.shell.video.hosting.presentation.utils.Dialog
import mikhail.shell.video.hosting.presentation.utils.MenuItem
import mikhail.shell.video.hosting.presentation.utils.PrimaryButton
import mikhail.shell.video.hosting.presentation.utils.toFullSubscribers
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun ChannelHeader(
    modifier: Modifier = Modifier,
    channel: ChannelWithUser,
    onSubscription: (SubscriptionState) -> Unit,
    onEdit: (channelId: Long) -> Unit = {},
    onRemove: (channelId: Long) -> Unit = {},
    owns: Boolean = false
) {
    val context = LocalContext.current
    val windowSizeClass = calculateWindowSizeClass(context as Activity)
    var hasCover by rememberSaveable { mutableStateOf<Boolean?>(null) }
    if (windowSizeClass.widthSizeClass == WindowWidthSizeClass.Compact) {
        ChannelHeaderCompact(
            modifier = modifier,
            hasCover = hasCover,
            coverUrlAssignment = { hasCover = it },
            channel = channel,
            onSubscription = onSubscription,
            onEdit = onEdit,
            onRemove = onRemove,
            owns = owns
        )
    } else if (windowSizeClass.heightSizeClass == WindowHeightSizeClass.Compact) {
        ChannelHeaderMedium(
            modifier = modifier,
            channel = channel,
            onSubscription = onSubscription,
            onEdit = onEdit,
            onRemove = onRemove,
            owns = owns
        )
    } else {
        ChannelHeaderExpanded(
            modifier = modifier,
            hasCover,
            { hasCover = it },
            channel,
            onSubscription,
            onEdit,
            onRemove,
            owns
        )
    }
}

@Composable
fun ChannelHeaderCompact(
    modifier: Modifier = Modifier,
    hasCover: Boolean?,
    coverUrlAssignment: (Boolean) -> Unit,
    channel: ChannelWithUser,
    onSubscription: (SubscriptionState) -> Unit,
    onEdit: (channelId: Long) -> Unit = {},
    onRemove: (channelId: Long) -> Unit = {},
    owns: Boolean = false
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        verticalArrangement = Arrangement.Top,
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    modifier = Modifier.padding(start = 10.dp)
                ) {
                    ChannelTitle(title = channel.title)
                    ChannelAlias(alias = channel.alias)
                    SubscriberNumberText(subscribers = channel.subscribers)
                }
                if (owns) {
                    ChannelActionsButton(
                        channelId = channel.channelId!!,
                        onEdit = onEdit,
                        onRemove = onRemove
                    )
                }
            }
        }
        SubscriptionButton(state = channel.subscription, onSubscription = onSubscription)
    }
}

@Composable
fun ChannelHeaderMedium(
    modifier: Modifier = Modifier,
    channel: ChannelWithUser,
    onSubscription: (SubscriptionState) -> Unit,
    onEdit: (channelId: Long) -> Unit = {},
    onRemove: (channelId: Long) -> Unit = {},
    owns: Boolean = false
) {
    ConstraintLayout(
        modifier = modifier.fillMaxWidth()
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
            Row {
                SubscriberNumberText(subscribers = channel.subscribers)
                if (owns) {
                    ChannelActionsButton(
                        channelId = channel.channelId!!,
                        onEdit = onEdit,
                        onRemove = onRemove
                    )
                }
            }
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
fun ChannelHeaderExpanded(
    modifier: Modifier = Modifier,
    hasCover: Boolean?,
    coverUrlAssignment: (Boolean) -> Unit,
    channel: ChannelWithUser,
    onSubscription: (SubscriptionState) -> Unit,
    onEdit: (channelId: Long) -> Unit = {},
    onRemove: (channelId: Long) -> Unit = {},
    owns: Boolean = false
) {
    ConstraintLayout(
        modifier = modifier.fillMaxWidth()
    ) {
        val coverRef = createRef()
        ChannelCover(
            modifier = Modifier.then(
                if (hasCover == true) {
                    Modifier
                        .fillMaxWidth()
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
                    top.linkTo(coverRef.bottom, (-65).dp)
                } else {
                    top.linkTo(parent.top)
                }
                start.linkTo(parent.start)
                end.linkTo(parent.end)
            },
            avatarUrl = channel.avatarUrl
        )
        val annotationRef = createRef()
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .constrainAs(annotationRef) {
                    top.linkTo(avatarRef.bottom, 16.dp)
                },
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ChannelTitle(title = channel.title)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                ChannelAlias(alias = channel.alias)
                SubscriptionButton(
                    modifier = Modifier,
                    state = channel.subscription,
                    onSubscription = onSubscription
                )
                Row {
                    SubscriberNumberText(subscribers = channel.subscribers)
                    if (owns) {
                        ChannelActionsButton(
                            channelId = channel.channelId!!,
                            onEdit = onEdit,
                            onRemove = onRemove
                        )
                    }
                }
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
    val context = LocalContext.current
    val widthDp = LocalConfiguration.current.screenWidthDp
    val widthPx = with(LocalDensity.current) { widthDp.dp.toPx().roundToInt() }
    val heightPx = with(LocalDensity.current) { 100.dp.toPx().roundToInt() }
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
                    Modifier
                }
            )
    ) {
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(coverUrl)
                .size(widthPx, heightPx)
                .build(),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.matchParentSize(),
            onState = {
                when(it) {
                    is Success -> coverUrlAssignment(true)
                    is AsyncImagePainter.State.Error -> coverUrlAssignment(false)
                    else -> Unit
                }
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
        contentDescription = stringResource(R.string.channel_avatar_description),
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
        text = if (state == SUBSCRIBED) stringResource(R.string.unsubscribe)
        else stringResource(R.string.subscribe),
        isActivated = state == SUBSCRIBED,
        onClick = {
            val subscriptionState =
                if (state == SUBSCRIBED) NOT_SUBSCRIBED else SUBSCRIBED
            onSubscription(subscriptionState)
        }
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

@Composable
fun ChannelActionsButton(
    channelId: Long,
    onEdit: (channelId: Long) -> Unit,
    onRemove: (channelId: Long) -> Unit
) {
    var actionDialogVisible by rememberSaveable { mutableStateOf(false) }
    var removeDialogVisible by rememberSaveable { mutableStateOf(false) }
    Box {
        IconButton(
            onClick = {
                actionDialogVisible = true
            }
        ) {
            Icon(
                imageVector = Icons.Rounded.MoreVert,
                contentDescription = stringResource(R.string.channel_actions_description)
            )
        }
        ContextMenu(
            isExpanded = actionDialogVisible,
            menuItems = listOf(
                MenuItem(
                    title = stringResource(R.string.channel_edit_btn),
                     onClick = {
                        onEdit(channelId)
                     }
                ),
                MenuItem(
                    title = stringResource(R.string.channel_delete_btn),
                    onClick = {
                        removeDialogVisible = true
                    }
                )
            ),
            onDismiss = {
                actionDialogVisible = false
            }
        )
        if (removeDialogVisible) {
            Dialog(
                onSubmit = {
                    onRemove(channelId)
                },
                onDismiss = {
                    removeDialogVisible = false
                },
                dialogTitle = stringResource(R.string.channel_deleting_title),
                dialogDescription = stringResource(R.string.channel_deleting_description)
            )
        }
    }
}