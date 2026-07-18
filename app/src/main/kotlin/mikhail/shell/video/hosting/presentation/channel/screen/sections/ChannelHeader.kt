package mikhail.shell.video.hosting.presentation.channel.screen.sections

import android.app.Activity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import coil.compose.AsyncImagePainter
import mikhail.shell.video.hosting.R
import mikhail.shell.video.hosting.domain.models.ImageSize
import mikhail.shell.video.hosting.domain.models.Subscription
import mikhail.shell.video.hosting.domain.models.Subscription.NOT_SUBSCRIBED
import mikhail.shell.video.hosting.domain.models.Subscription.SUBSCRIBED
import mikhail.shell.video.hosting.presentation.channel.models.ChannelForUserUi
import mikhail.shell.video.hosting.presentation.channel.screen.ChannelScreenAction
import mikhail.shell.video.hosting.presentation.utils.ContextMenu
import mikhail.shell.video.hosting.presentation.utils.Description
import mikhail.shell.video.hosting.presentation.utils.Dialog
import mikhail.shell.video.hosting.presentation.utils.MenuItem
import mikhail.shell.video.hosting.presentation.utils.MoreButton
import mikhail.shell.video.hosting.presentation.utils.PrimaryToggleButton
import mikhail.shell.video.hosting.presentation.utils.RestartableBox
import mikhail.shell.video.hosting.presentation.utils.exists
import mikhail.shell.video.hosting.presentation.utils.rememberAsyncImagePainter
import mikhail.shell.video.hosting.presentation.utils.toFullSubscribers
import mikhail.shell.video.hosting.presentation.channel.screen.ChannelScreenAction as ScreenAction

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
internal fun ChannelHeader(
    modifier: Modifier = Modifier,
    channel: ChannelForUserUi,
    isStarting: Boolean,
    onAction: (ScreenAction) -> Unit,
    owns: Boolean,
    onShowLogo: () -> Unit
) {
    val context = LocalContext.current
    val windowSizeClass = calculateWindowSizeClass(context as Activity)
    RestartableBox(
        modifier = modifier.padding(10.dp),
        isStarting = isStarting,
        onStart = {
            onAction(ScreenAction.RestartChannel)
        }
    ) {
        when {
            windowSizeClass.widthSizeClass == WindowWidthSizeClass.Compact -> {
                ChannelHeaderCompact(
                    modifier = Modifier,
                    channel = channel,
                    onAction = onAction,
                    owns = owns,
                    onShowLogo = onShowLogo
                )
            }
            windowSizeClass.heightSizeClass == WindowHeightSizeClass.Compact -> {
                ChannelHeaderMedium(
                    modifier = Modifier,
                    channel = channel,
                    onAction = onAction,
                    owns = owns,
                    onShowLogo = onShowLogo
                )
            }
            else -> {
                ChannelHeaderExpanded(
                    modifier = Modifier,
                    channel = channel,
                    onAction = onAction,
                    owns = owns,
                    onShowLogo = onShowLogo
                )
            }
        }
    }
}

@Composable
private fun ChannelHeaderCompact(
    modifier: Modifier = Modifier,
    channel: ChannelForUserUi,
    onAction: (ChannelScreenAction) -> Unit,
    owns: Boolean = false,
    onShowLogo: () -> Unit = {}
) {
    val header = rememberAsyncImagePainter(channel.header[ImageSize.MEDIUM])
    val logo = rememberAsyncImagePainter(channel.logo[ImageSize.MEDIUM])
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        verticalArrangement = Arrangement.Top,
    ) {
        if (header.exists() == true) {
            ChannelHeaderImage(
                modifier = Modifier.height(100.dp),
                header = header
            )
        }
        var isDescriptionVisible by rememberSaveable {
            mutableStateOf(false)
        }
        Row(
            Modifier
                .fillMaxWidth()
                .padding(
                    top = 13.dp,
                    bottom = 7.dp
                )
        ) {
            ChannelLogo(
                modifier = Modifier.size(80.dp),
                logo = logo,
                onShowLogo = onShowLogo
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    modifier = Modifier.padding(start = 10.dp)
                ) {
                    ChannelTitle(title = channel.title)
                    if (channel.alias != null) {
                        ChannelAlias(alias = channel.alias)
                    }
                    SubscriberNumberText(subscribers = channel.subscribers)
                }
                Row (
                    modifier = Modifier.wrapContentSize(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End
                ) {
                    if (owns) {
                        ChannelActionsButton(
                            channelId = channel.channelId,
                            onEdit = {
                                onAction(ChannelScreenAction.Edit)
                            },
                            onRemove = {
                                onAction(ChannelScreenAction.Remove)
                            }
                        )
                    }
                    if (channel.description != null) {
                        MoreButton(
                            isVisible = isDescriptionVisible,
                            onClick = {
                                isDescriptionVisible = !isDescriptionVisible
                            }
                        )
                    }
                }
            }
        }
        if (channel.description != null) {
            Description(
                modifier = Modifier.fillMaxWidth(),
                isVisible = isDescriptionVisible,
                description = channel.description
            )
        }
        SubscriptionButton(
            modifier = Modifier.fillMaxWidth(),
            state = channel.subscription,
            onSubscription = {
                onAction(ChannelScreenAction.Subscribe(it))
            }
        )
    }
}

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
private fun ChannelHeaderMedium(
    modifier: Modifier = Modifier,
    channel: ChannelForUserUi,
    onAction: (ChannelScreenAction) -> Unit,
    owns: Boolean = false,
    onShowLogo: () -> Unit = {}
) {
    val logo = rememberAsyncImagePainter(channel.logo[ImageSize.MEDIUM])
    val header = rememberAsyncImagePainter(channel.header[ImageSize.MEDIUM])
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        if (header.exists() == true) {
            ChannelHeaderImage(
                modifier = Modifier.height(40.dp),
                header = header
            )
        }
        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
        )
        var isDescriptionVisible by rememberSaveable {
            mutableStateOf(false)
        }
        ConstraintLayout(
            modifier = Modifier.fillMaxWidth()
        ) {
            val logoRef = createRef()
            ChannelLogo(
                modifier = Modifier
                    .size(64.dp)
                    .constrainAs(logoRef) {
                        start.linkTo(parent.start)
                        top.linkTo(parent.top)
                        bottom.linkTo(parent.bottom)
                    },
                logo = logo,
                onShowLogo = onShowLogo
            )
            val briefRef = createRef()
            Column(
                modifier = Modifier.constrainAs(briefRef) {
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                    start.linkTo(logoRef.end, 10.dp)
                }
            ) {
                ChannelTitle(title = channel.title)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (channel.alias != null) {
                        ChannelAlias(alias = channel.alias)
                    }
                    SubscriberNumberText(subscribers = channel.subscribers)
                }
            }
            val subButton = createRef()
            val moreButton = createRef()
            if (owns) {
                val actionsButton = createRef()
                ChannelActionsButton(
                    modifier = Modifier.constrainAs(actionsButton) {
                        if (channel.description != null) {
                            end.linkTo(moreButton.start, margin = 16.dp)
                        } else {
                            end.linkTo(subButton.start, margin = 16.dp)
                        }
                        top.linkTo(parent.top)
                        bottom.linkTo(parent.bottom)
                    },
                    channelId = channel.channelId,
                    onEdit = {
                        onAction(ChannelScreenAction.Edit)
                    },
                    onRemove = {
                        onAction(ChannelScreenAction.Remove)
                    }
                )
            }
            if (channel.description != null) {
                MoreButton(
                    modifier = Modifier.constrainAs(moreButton) {
                        end.linkTo(subButton.start, margin = 16.dp)
                        top.linkTo(parent.top)
                        bottom.linkTo(parent.bottom)
                    },
                    isVisible = isDescriptionVisible,
                    onClick = {
                        isDescriptionVisible = !isDescriptionVisible
                    }
                )
            }
            SubscriptionButton(
                modifier = Modifier.constrainAs(subButton) {
                    end.linkTo(parent.end)
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                },
                state = channel.subscription,
                onSubscription = {
                    onAction(ChannelScreenAction.Subscribe(it))
                }
            )
        }
        if (channel.description != null) {
            Description(
                modifier = Modifier.fillMaxWidth(),
                isVisible = isDescriptionVisible,
                description = channel.description
            )
        }
    }
}

@Composable
private fun ChannelHeaderExpanded(
    modifier: Modifier = Modifier,
    channel: ChannelForUserUi,
    onAction: (ChannelScreenAction) -> Unit,
    owns: Boolean = false,
    onShowLogo: () -> Unit
) {
    val header = rememberAsyncImagePainter(channel.header[ImageSize.LARGE])
    val headerExists = header.exists()
    val logo = rememberAsyncImagePainter(channel.logo[ImageSize.LARGE])
    ConstraintLayout(
        modifier = modifier.fillMaxWidth()
    ) {
        val headerRef = createRef()
        if (headerExists == true) {
            ChannelHeaderImage(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .constrainAs(headerRef) {
                        top.linkTo(parent.top)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                    },
                header = header
            )
        }
        val logoRef = createRef()
        val logoExists = logo.exists()
        ChannelLogo(
            modifier = Modifier
                .size(80.dp)
                .constrainAs(logoRef) {
                    if (logoExists == true) {
                        top.linkTo(headerRef.bottom, -65.dp)
                    } else {
                        top.linkTo(parent.top)
                    }
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                },
            logo = logo,
            onShowLogo = onShowLogo
        )
        val annotationRef = createRef()
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .constrainAs(annotationRef) {
                    top.linkTo(logoRef.bottom, 16.dp)
                },
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ChannelTitle(title = channel.title)
            var isDescriptionVisible by rememberSaveable {
                mutableStateOf(false)
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (channel.alias != null) {
                    ChannelAlias(alias = channel.alias)
                }
                SubscriptionButton(
                    state = channel.subscription,
                    onSubscription = {
                        onAction(ChannelScreenAction.Subscribe(it))
                    }
                )
                SubscriberNumberText(subscribers = channel.subscribers)
                if (owns) {
                    ChannelActionsButton(
                        channelId = channel.channelId,
                        onEdit = {
                            onAction(ChannelScreenAction.Edit)
                        },
                        onRemove = {
                            onAction(ChannelScreenAction.Remove)
                        }
                    )
                }
                if (channel.description != null) {
                    MoreButton(
                        isVisible = isDescriptionVisible,
                        onClick = {
                            isDescriptionVisible = !isDescriptionVisible
                        }
                    )
                }
            }
            if (channel.description != null) {
                Description(
                    modifier = Modifier.fillMaxWidth(),
                    isVisible = isDescriptionVisible,
                    description = channel.description
                )
            }
        }
    }
}

@Composable
private fun ChannelHeaderImage(
    modifier: Modifier = Modifier,
    header: AsyncImagePainter
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Image(
            modifier = Modifier.matchParentSize(),
            painter = header,
            contentScale = ContentScale.Crop,
            contentDescription = null
        )
    }
}

@Composable
private fun ChannelLogo(
    modifier: Modifier = Modifier,
    logo: AsyncImagePainter,
    onShowLogo: (() -> Unit)? = null
) {
    Image(
        modifier = modifier
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable(
                enabled = logo.exists() == true && onShowLogo != null,
                onClick = onShowLogo ?: {}
            ),
        painter = logo,
        contentDescription = stringResource(R.string.channel_logo_description),
        contentScale = ContentScale.Crop
    )
}

@Composable
fun SubscriptionButton(
    modifier: Modifier = Modifier,
    state: Subscription,
    onSubscription: (Subscription) -> Unit
) {
    PrimaryToggleButton(
        modifier = modifier,
        toggledOffText = stringResource(R.string.subscribe_button),
        toggledOnText = stringResource(R.string.unsubscribe_button),
        toggled = state == SUBSCRIBED,
        onClick = {
            onSubscription(if (state == SUBSCRIBED) NOT_SUBSCRIBED else SUBSCRIBED)
        }
    )
}

@Composable
private fun SubscriberNumberText(
    modifier: Modifier = Modifier,
    subscribers: Long
) {
    val context = LocalContext.current
    Text(
        modifier = modifier,
        text = subscribers.toFullSubscribers(context),
        fontSize = 13.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
private fun ChannelTitle(
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
private fun ChannelAlias(
    modifier: Modifier = Modifier,
    alias: String
) {
    Text(
        text = "@${alias}",
        fontSize = 13.sp,
        modifier = modifier.padding(top = 10.dp),
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
private fun ChannelActionsButton(
    modifier: Modifier = Modifier,
    channelId: Long,
    onEdit: (channelId: Long) -> Unit,
    onRemove: (channelId: Long) -> Unit
) {
    var actionDialogVisible by rememberSaveable {
        mutableStateOf(false)
    }
    var removeDialogVisible by rememberSaveable {
        mutableStateOf(false)
    }
    Box(
        modifier = modifier
    ) {
        IconButton(
            modifier = Modifier.size(21.dp),
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
                    title = stringResource(R.string.channel_edit_button),
                    onClick = {
                        onEdit(channelId)
                    }
                ),
                MenuItem(
                    title = stringResource(R.string.channel_delete_button),
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
                dialogTitle = stringResource(R.string.channel_delete_warning_title),
                dialogDescription = stringResource(R.string.channel_delete_warning_message)
            )
        }
    }
}