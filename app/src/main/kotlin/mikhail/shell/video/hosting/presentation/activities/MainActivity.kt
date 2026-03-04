package mikhail.shell.video.hosting.presentation.activities

import android.app.ComponentCaller
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioManager
import android.media.session.MediaSession
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
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.StateFlow
import mikhail.shell.video.hosting.domain.providers.UserDetails
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
import mikhail.shell.video.hosting.presentation.player.PlayerState
import mikhail.shell.video.hosting.presentation.player.rememberPlayerState
import mikhail.shell.video.hosting.presentation.video.MiniPlayer
import mikhail.shell.video.hosting.receivers.MediaBroadcastReceiver
import mikhail.shell.video.hosting.receivers.MediaHandler
import mikhail.shell.video.hosting.ui.theme.DarkColorScheme
import mikhail.shell.video.hosting.ui.theme.UiPreferences
import mikhail.shell.video.hosting.ui.theme.VideoHostingTheme
import javax.inject.Inject

@UnstableApi
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
    lateinit var mediaReceiver: MediaBroadcastReceiver

    @Inject
    lateinit var mediaHandler: MediaHandler
    lateinit var mediaSession: MediaSession

    lateinit var rootBackStack: MutableList<Route>
    lateinit var currentTabBackStack: MutableState<MutableList<Route>>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setPrimaryContent()
        setMediaHandlers()
        setDataFlows()
    }

    private fun setPrimaryContent() {
        setContent {
            val userData by userDetails.collectAsStateWithLifecycle()
            val uiPreferences by uiPreferences.collectAsStateWithLifecycle()
            VideoHostingTheme(
                uiPreferences = uiPreferences
            ) {
                val playerState = rememberSaveable {
                    mutableStateOf(PlayerState())
                }
                val miniPlayerDimensionsState = rememberSaveable {
                    mutableStateOf(null as MiniPlayerDimensions?)
                }
                CompositionLocalProvider(
                    LocalPlayerState provides playerState,
                    LocalMiniPlayerDimensionsState provides miniPlayerDimensionsState
                ) {
                    val activity = LocalActivity.current!!
                    val view = LocalView.current
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
                    var playerState by player.rememberPlayerState()
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
                            && currentRootRoute !is Route.Video) {
                            playerState = playerState.copy(
                                hidden = false
                            )
                        }
                    }
                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        bottomBar = {
                            AnimatedVisibility (
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
                            if (!playerState.hidden && playerState.prepared) {
                                MiniPlayer(
                                    modifier = Modifier,
                                    playerProvider = { player },
                                    onFullScreen = {
                                        rootBackStack.add(Route.Video(it))
                                    }
                                )
                            }
                        }
                    }
                }
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
            rootBackStack.add(Route.Video(videoId))
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
    }

    override fun onNewIntent(intent: Intent, caller: ComponentCaller) {
        super.onNewIntent(intent, caller)
        handleDeepLink(intent)
    }

    override fun onStop() {
        if (!isChangingConfigurations) {
            player.pause()
        }
        super.onStop()
    }

    override fun onDestroy() {
        unregisterReceiver(mediaReceiver)
        mediaSession.release()
        super.onDestroy()
    }
}