package mikhail.shell.video.hosting.presentation.channel.screen.sections

import android.app.Activity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
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
import mikhail.shell.video.hosting.domain.models.Subscription
import mikhail.shell.video.hosting.domain.models.Subscription.NOT_SUBSCRIBED
import mikhail.shell.video.hosting.domain.models.Subscription.SUBSCRIBED
import mikhail.shell.video.hosting.presentation.channel.models.ChannelForUserUi
import mikhail.shell.video.hosting.presentation.channel.screen.ChannelScreenUiEvent
import mikhail.shell.video.hosting.presentation.utils.ContextMenu
import mikhail.shell.video.hosting.presentation.utils.Dialog
import mikhail.shell.video.hosting.presentation.utils.MenuItem
import mikhail.shell.video.hosting.presentation.utils.PrimaryToggleButton
import mikhail.shell.video.hosting.presentation.utils.toFullSubscribers
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
internal fun ChannelHeader(
    modifier: Modifier = Modifier,
    channel: ChannelForUserUi,
    onEvent: (ChannelScreenUiEvent) -> Unit,
    owns: Boolean = false,
    onShowLogo: () -> Unit
) {
    val context = LocalContext.current
    val windowSizeClass = calculateWindowSizeClass(context as Activity)
    var hasHeader by rememberSaveable { mutableStateOf<Boolean?>(null) }
    if (windowSizeClass.widthSizeClass == WindowWidthSizeClass.Compact) {
        ChannelHeaderCompact(
            modifier = modifier,
            hasHeader = hasHeader,
            headerUrlAssignment = { hasHeader = it },
            channel = channel,
            onEvent = onEvent,
            owns = owns,
            onShowLogo = onShowLogo
        )
    } else if (windowSizeClass.heightSizeClass == WindowHeightSizeClass.Compact) {
        ChannelHeaderMedium(
            modifier = modifier,
            channel = channel,
            onEvent = onEvent,
            owns = owns,
            onShowLogo = onShowLogo
        )
    } else {
        ChannelHeaderExpanded(
            modifier = modifier,
            hasHeader = hasHeader,
            headerUrlAssignment = { hasHeader = it },
            channel = channel,
            onEvent = onEvent,
            owns = owns,
            onShowLogo = onShowLogo
        )
    }
}

@Composable
private fun ChannelHeaderCompact(
    modifier: Modifier = Modifier,
    hasHeader: Boolean?,
    headerUrlAssignment: (Boolean) -> Unit,
    channel: ChannelForUserUi,
    onEvent: (ChannelScreenUiEvent) -> Unit,
    owns: Boolean = false,
    onShowLogo: () -> Unit = {}
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        verticalArrangement = Arrangement.Top,
    ) {
        ChannelHeader(
            hasHeader = hasHeader,
            headerUrlAssignment = headerUrlAssignment,
            headerUrl = channel.headerUrl
        )
        Row(
            Modifier
                .fillMaxWidth()
                .padding(
                    top = 13.dp,
                    bottom = 7.dp
                )
        ) {
            ChannelLogo(
                modifier = Modifier,
                logo = channel.logo,
                onShowLogo = onShowLogo
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
                    ChannelAlias(alias = channel.alias?: channel.channelId.toString())
                    SubscriberNumberText(subscribers = channel.subscribers)
                }
                if (owns) {
                    ChannelActionsButton(
                        channelId = channel.channelId,
                        onEdit = {
                            onEvent(ChannelScreenUiEvent.Edit)
                        },
                        onRemove = {
                            onEvent(ChannelScreenUiEvent.Remove)
                        }
                    )
                }
            }
        }
        if (channel.description != null) {
            ChannelDescriptionSection(
                description = channel.description
            )
        }
        SubscriptionButton(
            modifier = Modifier.fillMaxWidth(),
            state = channel.subscription,
            onSubscription = {
                onEvent(ChannelScreenUiEvent.Subscribe(it))
            }
        )
    }
}

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
private fun ChannelHeaderMedium(
    modifier: Modifier = Modifier,
    channel: ChannelForUserUi,
    onEvent: (ChannelScreenUiEvent) -> Unit,
    owns: Boolean = false,
    onShowLogo: () -> Unit = {}
) {
    ConstraintLayout(
        modifier = modifier.fillMaxWidth()
    ) {
        val logoRef = createRef()
        ChannelLogo(
            modifier = Modifier.constrainAs(logoRef) {
                start.linkTo(parent.start)
                top.linkTo(parent.top)
                bottom.linkTo(parent.bottom)
            },
            logo = channel.logo,
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
            ChannelAlias(alias = channel.alias?: channel.channelId.toString())
            Row {
                SubscriberNumberText(subscribers = channel.subscribers)
                if (owns) {
                    ChannelActionsButton(
                        channelId = channel.channelId,
                        onEdit = {
                            onEvent(ChannelScreenUiEvent.Edit)
                        },
                        onRemove = {
                            onEvent(ChannelScreenUiEvent.Remove)
                        }
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
            onSubscription = {
                onEvent(ChannelScreenUiEvent.Subscribe(it))
            }
        )
    }
}

@Composable
private fun ChannelHeaderExpanded(
    modifier: Modifier = Modifier,
    hasHeader: Boolean?,
    headerUrlAssignment: (Boolean) -> Unit,
    channel: ChannelForUserUi,
    onEvent: (ChannelScreenUiEvent) -> Unit,
    owns: Boolean = false,
    onShowLogo: () -> Unit
) {
    ConstraintLayout(
        modifier = modifier.fillMaxWidth()
    ) {
        val headerRef = createRef()
        ChannelHeader(
            modifier = Modifier.then(
                if (hasHeader == true) {
                    Modifier
                        .fillMaxWidth()
                        .constrainAs(headerRef) {
                            top.linkTo(parent.top)
                            start.linkTo(parent.start)
                            end.linkTo(parent.end)
                        }
                } else Modifier
            ),
            hasHeader = hasHeader,
            headerUrlAssignment = headerUrlAssignment,
            headerUrl = channel.headerUrl,
        )
        val logoRef = createRef()
        ChannelLogo(
            modifier = Modifier.constrainAs(logoRef) {
                if (hasHeader == true) {
                    top.linkTo(headerRef.bottom, (-65).dp)
                } else {
                    top.linkTo(parent.top)
                }
                start.linkTo(parent.start)
                end.linkTo(parent.end)
            },
            logo = channel.logo,
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
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                ChannelAlias(alias = channel.alias?: channel.channelId.toString())
                SubscriptionButton(
                    state = channel.subscription,
                    onSubscription = {
                        onEvent(ChannelScreenUiEvent.Subscribe(it))
                    }
                )
                SubscriberNumberText(subscribers = channel.subscribers)
                if (owns) {
                    ChannelActionsButton(
                        channelId = channel.channelId,
                        onEdit = {
                            onEvent(ChannelScreenUiEvent.Edit)
                        },
                        onRemove = {
                            onEvent(ChannelScreenUiEvent.Remove)
                        }
                    )
                }
            }
            if (channel.description != null) {
                ChannelDescriptionSection(
                    description = channel.description
                )
            }
        }
    }
}

@Composable
private fun ChannelHeader(
    modifier: Modifier = Modifier,
    hasHeader: Boolean?,
    headerUrlAssignment: (Boolean) -> Unit,
    headerUrl: String?
) {
    val context = LocalContext.current
    val widthDp = LocalConfiguration.current.screenWidthDp
    val widthPx = with(LocalDensity.current) { widthDp.dp.toPx().roundToInt() }
    val heightPx = with(LocalDensity.current) { 100.dp.toPx().roundToInt() }
    Box(
        modifier = modifier
            .then(
                if (hasHeader == true) {
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
                .data(headerUrl)
                .size(widthPx, heightPx)
                .build(),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.matchParentSize(),
            onState = {
                when (it) {
                    is Success -> headerUrlAssignment(true)
                    is AsyncImagePainter.State.Error -> headerUrlAssignment(false)
                    else -> Unit
                }
            }
        )
    }
}

@Composable
fun ChannelLogo(
    modifier: Modifier = Modifier,
    logo: String?,
    onShowLogo: (() -> Unit)? = null
) {
    var logoExists by rememberSaveable { mutableStateOf(null as Boolean?) }
    AsyncImage(
        model = logo,
        contentDescription = stringResource(R.string.channel_logo_description),
        contentScale = ContentScale.Crop,
        onSuccess = {
            logoExists = true
        },
        onError = {
            logoExists = false
        },
        modifier = modifier
            .size(80.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable(
                enabled = logoExists == true && onShowLogo != null,
                onClick = onShowLogo ?: {}
            )
    )
}


@Composable
private fun ChannelDescription(
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
private fun ChannelDescriptionSection(
    description: String
) {
    if (description.isNotEmpty()) {
        var shouldShowDescription by rememberSaveable { mutableStateOf(false) }
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            IconButton(
                modifier = Modifier.size(18.dp),
                onClick = {
                    shouldShowDescription = !shouldShowDescription
                }
            ) {
                Icon(
                    imageVector = when (shouldShowDescription) {
                        true -> Icons.Rounded.KeyboardArrowUp
                        false -> Icons.Rounded.KeyboardArrowDown
                    },
                    contentDescription = stringResource(R.string.more_button)
                )
            }
        }
        AnimatedVisibility(
            visible = shouldShowDescription,
            enter = expandVertically (
                tween (durationMillis = 300)
            ),
            exit = shrinkVertically(
                tween(durationMillis = 300)
            )
        ) {
            ChannelDescription(
                modifier = Modifier.padding(bottom = 7.dp),
                description = description
            )
        }
    }
}

@Composable
private fun ChannelActionsButton(
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