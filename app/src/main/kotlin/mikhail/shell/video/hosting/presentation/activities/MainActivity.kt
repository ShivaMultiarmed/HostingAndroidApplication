package mikhail.shell.video.hosting.presentation.activities

import android.app.ComponentCaller
import android.content.Intent
import android.content.IntentFilter
import android.content.res.Configuration
import android.media.AudioManager
import android.media.session.MediaSession
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSerializable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.zIndex
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.Player
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import mikhail.shell.video.hosting.domain.providers.UserDetails
import mikhail.shell.video.hosting.domain.receivers.PipPlayerReceiver
import mikhail.shell.video.hosting.domain.usecases.ui.ObserveUiPreferences
import mikhail.shell.video.hosting.domain.usecases.user.ObserveUserDetails
import mikhail.shell.video.hosting.presentation.navigation.authentication.authenticationGraph
import mikhail.shell.video.hosting.presentation.navigation.common.BottomNavBar
import mikhail.shell.video.hosting.presentation.navigation.common.RootAnimations
import mikhail.shell.video.hosting.presentation.navigation.common.Route
import mikhail.shell.video.hosting.presentation.navigation.common.defaultNavDecorators
import mikhail.shell.video.hosting.presentation.navigation.user.subscriptionsGraph
import mikhail.shell.video.hosting.presentation.navigation.user.userGraph
import mikhail.shell.video.hosting.presentation.navigation.video.PredictiveFadeOutSceneStrategy
import mikhail.shell.video.hosting.presentation.navigation.video.recommendationsGraph
import mikhail.shell.video.hosting.presentation.navigation.video.searchGraph
import mikhail.shell.video.hosting.presentation.navigation.video.videoGraph
import mikhail.shell.video.hosting.presentation.player.LocalMiniPlayerDimensionsState
import mikhail.shell.video.hosting.presentation.player.LocalPlayerState
import mikhail.shell.video.hosting.presentation.player.MiniPlayerDimensions
import mikhail.shell.video.hosting.presentation.player.PipPlayerUtils
import mikhail.shell.video.hosting.presentation.player.PlayerState
import mikhail.shell.video.hosting.presentation.player.VideoSurface
import mikhail.shell.video.hosting.presentation.player.rememberPlayerState
import mikhail.shell.video.hosting.presentation.utils.observe
import mikhail.shell.video.hosting.presentation.video.MiniPlayer
import mikhail.shell.video.hosting.receivers.MediaBroadcastReceiver
import mikhail.shell.video.hosting.receivers.MediaHandler
import mikhail.shell.video.hosting.ui.theme.DarkColorScheme
import mikhail.shell.video.hosting.ui.theme.UiPreferences
import mikhail.shell.video.hosting.ui.theme.VideoHostingTheme
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var observeUiPreferences: ObserveUiPreferences
    lateinit var uiPreferences: StateFlow<UiPreferences>

    @Inject
    lateinit var observeUserDetails: ObserveUserDetails
    lateinit var userDetails: StateFlow<UserDetails>

    @Inject
    lateinit var player: Player
    private val playbackStateFlow = MutableStateFlow(Player.STATE_IDLE)
    lateinit var mediaReceiver: MediaBroadcastReceiver

    @Inject
    lateinit var mediaHandler: MediaHandler
    lateinit var mediaSession: MediaSession

    lateinit var rootBackStack: MutableList<Route>
    lateinit var currentTabBackStack: MutableState<MutableList<Route>>

    private lateinit var pipPlayerReceiver: PipPlayerReceiver
    private lateinit var pipPlayerUtils: PipPlayerUtils
    private val isInPipModeStateFlow = MutableStateFlow(false)
    private lateinit var pipPlayerListener: Player.Listener

    private val pendingIntents = Channel<Intent>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent()
        setMediaHandlers()
        setDataFlows()
        if (savedInstanceState == null) {
            lifecycleScope.launch {
                pendingIntents.send(intent)
            }
        }
    }

    private fun setContent() {
        setContent {
            val userData by userDetails.collectAsStateWithLifecycle()
            val uiPreferences by uiPreferences.collectAsStateWithLifecycle()
            VideoHostingTheme(
                uiPreferences = uiPreferences
            ) {
                val isInPipMode by isInPipModeStateFlow.collectAsStateWithLifecycle()
                val providedPlayerState = rememberSaveable {
                    mutableStateOf(PlayerState())
                }
                val providedMiniPlayerDimensionsState = rememberSaveable {
                    mutableStateOf(null as MiniPlayerDimensions?)
                }
                CompositionLocalProvider(
                    LocalPlayerState provides providedPlayerState,
                    LocalMiniPlayerDimensionsState provides providedMiniPlayerDimensionsState
                ) {
                    val activity = LocalActivity.current!!
                    val view = LocalView.current
                    var playerState by player.rememberPlayerState()
                    rootBackStack = rememberSaveable {
                        mutableStateListOf(
                            when {
                                userData.userId == 0L -> Route.Authentication
                                else -> Route.Recommendations
                            }
                        )
                    }
                    val currentRootRoute = rootBackStack.lastOrNull()
                    val recommendationsBackStack = rememberSaveable {
                        mutableStateListOf<Route>(Route.Recommendations.View)
                    }
                    val subscriptionsBackStack = rememberSaveable {
                        mutableStateListOf<Route>(Route.Subscriptions.View)
                    }
                    val searchBackStack = rememberSaveable {
                        mutableStateListOf<Route>(Route.Search.View)
                    }
                    val userBackStack = rememberSaveable(userData.userId) {
                        mutableStateListOf<Route>(Route.User.Profile(userData.userId))
                    }
                    var currentTabRoute by rememberSerializable {
                        mutableStateOf<Route>(Route.Recommendations)
                    }
                    currentTabBackStack = rememberSaveable {
                        mutableStateOf(recommendationsBackStack)
                    }
                    val statusBarIconsColor = MaterialTheme.colorScheme.onSurface
                    LaunchedEffect(currentRootRoute is Route.Video) {
                        WindowCompat.getInsetsController(
                            activity.window,
                            view
                        ).isAppearanceLightStatusBars = when {
                            currentRootRoute is Route.Video -> false
                            else -> statusBarIconsColor != DarkColorScheme.onSurface
                        }
                    }
                    LaunchedEffect(currentRootRoute) {
                        playerState = playerState.copy(
                            hidden = currentRootRoute == Route.Authentication || currentRootRoute is Route.Video
                        )
                        if (currentRootRoute == Route.Authentication && player.isPlaying) {
                            player.pause()
                        }
                        val isTabRoute = currentRootRoute in setOf(
                            Route.Recommendations,
                            Route.Subscriptions,
                            Route.Search
                        ) || currentRootRoute is Route.User
                        if (isTabRoute && currentRootRoute != null) {
                            currentTabRoute = currentRootRoute
                        }
                    }
                    LaunchedEffect(currentTabRoute) {
                        currentTabBackStack.value = when (currentTabRoute) {
                            Route.Recommendations -> recommendationsBackStack
                            Route.Subscriptions -> subscriptionsBackStack
                            Route.Search -> searchBackStack
                            is Route.User -> userBackStack
                            else -> return@LaunchedEffect
                        }
                    }
                    LaunchedEffect(currentRootRoute) {
                        if (currentRootRoute != null
                            && currentRootRoute != Route.Authentication
                            && currentRootRoute !is Route.Video
                        ) {
                            playerState = playerState.copy(
                                hidden = false
                            )
                        }
                    }
                    Box {
                        Scaffold(
                            modifier = Modifier.fillMaxSize(),
                            bottomBar = {
                                AnimatedVisibility(
                                    visible = currentRootRoute != null
                                            && currentRootRoute != Route.Authentication
                                            && currentRootRoute !is Route.Video
                                            && !playerState.fullScreen,
                                    enter = slideInVertically { it },
                                    exit = slideOutVertically { it }
                                ) {
                                    BottomNavBar(
                                        selectedTabRoute = currentRootRoute!!,
                                        onClick = { navRoute ->
                                            if (!rootBackStack.contains(navRoute)) {
                                                rootBackStack.add(navRoute)
                                            } else {
                                                val routeToSwitch =
                                                    rootBackStack.find { it == navRoute }!!
                                                if (currentRootRoute == routeToSwitch) {
                                                    currentTabBackStack.value.subList(
                                                        1,
                                                        currentTabBackStack.value.size
                                                    ).clear()
                                                } else {
                                                    rootBackStack.remove(routeToSwitch)
                                                    rootBackStack.add(routeToSwitch)
                                                }
                                            }
                                        },
                                        profileUserId = userData.userId
                                    )
                                }
                            }
                        ) { padding ->
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        when (currentRootRoute) {
                                            is Route.Video -> Color.Black
                                            else -> MaterialTheme.colorScheme.surface
                                        }
                                    )
                                    .padding(padding)
                                    .consumeWindowInsets(padding)
                            ) {
                                NavDisplay(
                                    modifier = Modifier.fillMaxSize(),
                                    backStack = when {
                                        Route.Authentication in rootBackStack -> rootBackStack.subList(
                                            rootBackStack.lastIndex,
                                            rootBackStack.size
                                        )
                                        else -> rootBackStack
                                    },
                                    entryDecorators = defaultNavDecorators,
                                    transitionSpec = { RootAnimations.enteringAnimation },
                                    popTransitionSpec = { RootAnimations.leavingAnimation },
                                    predictivePopTransitionSpec = { RootAnimations.leavingAnimation },
                                    sceneStrategy = remember { PredictiveFadeOutSceneStrategy() },
                                    entryProvider = entryProvider {
                                        authenticationGraph(rootBackStack = rootBackStack)
                                        recommendationsGraph(
                                            rootBackStack = rootBackStack,
                                            recommendationsBackStack = recommendationsBackStack,
                                            currentTabBackStack = currentTabBackStack
                                        )
                                        subscriptionsGraph(
                                            rootBackStack = rootBackStack,
                                            subscriptionsBackStack = subscriptionsBackStack,
                                            currentTabBackStack = currentTabBackStack
                                        )
                                        searchGraph(
                                            rootBackStack = rootBackStack,
                                            searchBackStack = searchBackStack,
                                            currentTabBackStack = currentTabBackStack
                                        )
                                        userGraph(
                                            rootBackStack = rootBackStack,
                                            userBackStack = userBackStack,
                                            currentTabBackStack = currentTabBackStack
                                        )
                                        videoGraph(
                                            rootBackStack = rootBackStack,
                                            currentTabBackStack = currentTabBackStack
                                        )
                                    }
                                )
                                if (!playerState.hidden && playerState.prepared && !isInPipMode) {
                                    MiniPlayer(
                                        playerProvider = { player },
                                        onFullScreen = {
                                            rootBackStack.add(Route.Video(it))
                                        }
                                    )
                                }
                            }
                        }
                        if (isInPipMode) {
                            VideoSurface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .zIndex(1f),
                                playerProvider = { player }
                            )
                        }
                    }
                    LaunchedEffect(isInPipMode) {
                        playerState = playerState.copy(inPipMode = isInPipMode)
                    }
                }
                pendingIntents.receiveAsFlow().observe(::handleDeepLink)
            }
        }
    }

    private fun handleDeepLink(intent: Intent) {
        val BASE_URL = Regex.escape("https://trendy-app.ru")
        if (intent.data == null) {
            return
        }
        val uri = intent.data.toString()
        Regex("^($BASE_URL/videos/)(\\d{1,8})$").find(uri)?.let {
            val videoId = it.groups[2]?.value?.toLongOrNull() ?: return@let
            val currentRootRoute = rootBackStack.last()
            if (currentRootRoute is Route.Video && currentRootRoute.videoId != videoId) {
                rootBackStack.removeLastOrNull()
            }
            if (currentRootRoute !is Route.Video || currentRootRoute.videoId != videoId) {
                rootBackStack.add(Route.Video(videoId))
            }
            return
        }
    }

    private fun setDataFlows() {
        uiPreferences = observeUiPreferences()
        userDetails = observeUserDetails()
    }

    private fun setMediaHandlers() {
        mediaSession = MediaSession(this, "PlayerMediaSession")
        mediaSession.setCallback(mediaHandler)
        mediaSession.isActive = true

        mediaReceiver = MediaBroadcastReceiver()
        registerReceiver(mediaReceiver, IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY))
        pipPlayerReceiver = PipPlayerReceiver(player)
        val flags =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) RECEIVER_EXPORTED else 0
        val pipPlayerReceiverIntentFilter = IntentFilter().apply {
            PipPlayerReceiver.ACTIONS.forEach(::addAction)
        }
        registerReceiver(pipPlayerReceiver, pipPlayerReceiverIntentFilter, flags)

        pipPlayerUtils = PipPlayerUtils(this, player)
        pipPlayerListener = object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                super.onIsPlayingChanged(isPlaying)
                if (playbackStateFlow.value != Player.STATE_BUFFERING) {
                    val pipParams = pipPlayerUtils.createPipParams()
                    setPictureInPictureParams(pipParams)
                }
            }
            override fun onPlaybackStateChanged(playbackState: Int) {
                super.onPlaybackStateChanged(playbackState)
                playbackStateFlow.value = playbackState
            }
        }
        player.addListener(pipPlayerListener)
    }

    private fun removeMediaHandlers() {
        player.removeListener(pipPlayerListener)
        unregisterReceiver(mediaReceiver)
        unregisterReceiver(pipPlayerReceiver)
        mediaSession.release()
    }

    override fun onNewIntent(intent: Intent, caller: ComponentCaller) {
        super.onNewIntent(intent, caller)
        lifecycleScope.launch {
            pendingIntents.send(intent)
        }
    }

    override fun onDestroy() {
        removeMediaHandlers()
        super.onDestroy()
    }

    // MiniPlayer PiP-mode

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        if (player.currentMediaItem != null) {
            val pipParams = pipPlayerUtils.createPipParams()
            setPictureInPictureParams(pipParams)
            enterPictureInPictureMode(pipParams)
        }
    }

    override fun onPictureInPictureModeChanged(
        isInPictureInPictureMode: Boolean,
        newConfig: Configuration
    ) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig)
        isInPipModeStateFlow.update { isInPictureInPictureMode }
        if (isInPictureInPictureMode) {
            val pipParams = pipPlayerUtils.createPipParams()
            setPictureInPictureParams(pipParams)
        }
    }

    override fun onStop() {
        if (!isChangingConfigurations) {
            player.pause()
        }
        super.onStop()
    }
}