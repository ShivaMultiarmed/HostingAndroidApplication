package mikhail.shell.video.hosting.presentation.activities

import android.content.IntentFilter
import android.media.AudioManager
import android.media.session.MediaSession
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSavedStateNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import androidx.navigation3.ui.rememberSceneSetupNavEntryDecorator
import dagger.hilt.android.AndroidEntryPoint
import mikhail.shell.video.hosting.domain.providers.UserDetailsProvider
import mikhail.shell.video.hosting.presentation.exoplayer.LocalPlayerState
import mikhail.shell.video.hosting.presentation.exoplayer.PlayerState
import mikhail.shell.video.hosting.presentation.exoplayer.PlayerStateSaver
import mikhail.shell.video.hosting.presentation.exoplayer.isPlayerPrepared
import mikhail.shell.video.hosting.presentation.navigation.authentication.authenticationGraph
import mikhail.shell.video.hosting.presentation.navigation.common.BottomNavBar
import mikhail.shell.video.hosting.presentation.navigation.common.Route
import mikhail.shell.video.hosting.presentation.navigation.user.subscriptionsGraph
import mikhail.shell.video.hosting.presentation.navigation.user.userGraph
import mikhail.shell.video.hosting.presentation.navigation.video.recommendationsGraph
import mikhail.shell.video.hosting.presentation.navigation.video.searchGraph
import mikhail.shell.video.hosting.presentation.navigation.video.videoGraph
import mikhail.shell.video.hosting.presentation.utils.BackStackSaver
import mikhail.shell.video.hosting.presentation.video.MiniPlayer
import mikhail.shell.video.hosting.receivers.MediaBroadcastReceiver
import mikhail.shell.video.hosting.receivers.MediaHandler
import mikhail.shell.video.hosting.ui.theme.DarkColorScheme
import mikhail.shell.video.hosting.ui.theme.VideoHostingTheme
import javax.inject.Inject

@UnstableApi
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var userDetailsProvider: UserDetailsProvider

    @Inject
    lateinit var player: Player
    lateinit var mediaReceiver: MediaBroadcastReceiver

    @Inject
    lateinit var mediaHandler: MediaHandler
    lateinit var mediaSession: MediaSession

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setPrimaryContent()
        setMediaHandlers()
    }

    private fun setPrimaryContent() {
        setContent {
            VideoHostingTheme {
                val playerState = rememberSaveable(saver = PlayerStateSaver) { mutableStateOf(PlayerState()) }
                CompositionLocalProvider(
                    LocalPlayerState provides playerState
                ) {
                    val activity = LocalActivity.current!!
                    val view = LocalView.current
                    val rootBackStack = rememberSaveable(
                        saver = BackStackSaver
                    ) {
                        mutableStateListOf((if (userDetailsProvider.getUserId() != 0L) Route.Recommendations else Route.Authentication))
                    }
                    val currentRoute = rootBackStack.lastOrNull()
                    val recommendationsBackStack = rememberSaveable(
                        saver = BackStackSaver
                    ) {
                        mutableStateListOf(Route.Recommendations.View)
                    }
                    val subscriptionsBackStack = rememberSaveable(
                        saver = BackStackSaver
                    ) {
                        mutableStateListOf(Route.Subscriptions.View)
                    }
                    val searchBackStack = rememberSaveable(
                        saver = BackStackSaver
                    ) {
                        mutableStateListOf(Route.Search.View)
                    }
                    val userBackStack = rememberSaveable(
                        saver = BackStackSaver
                    ) {
                        mutableStateListOf(Route.User.Profile(userDetailsProvider.getUserId()))
                    }
                    val orientation = LocalConfiguration.current.orientation
                    val statusBarIconsColor = MaterialTheme.colorScheme.onSurface
                    LaunchedEffect(currentRoute) {
                        WindowCompat.getInsetsController(activity.window, view).isAppearanceLightStatusBars = when {
                            currentRoute is Route.Video.View -> false
                            else -> statusBarIconsColor != DarkColorScheme.onSurface
                        }
                    }
                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        bottomBar = {
                            if (currentRoute != null && currentRoute != Route.Authentication && currentRoute !is Route.Video && !LocalPlayerState.current.value.fullScreen) {
                                BottomNavBar(
                                    onClick = { navItem ->
                                        if (!rootBackStack.contains(navItem.route)) {
                                            rootBackStack.add(navItem.route)
                                        } else {
                                            val routeToSwitch = rootBackStack.find { it == navItem.route }!!
                                            rootBackStack.remove(routeToSwitch)
                                            rootBackStack.add(routeToSwitch)
                                        }
                                    },
                                    userId = userDetailsProvider.getUserId()
                                )
                            }
                        }
                    ) { padding ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    if (currentRoute is Route.Video) {
                                        Color.Black
                                    } else {
                                        MaterialTheme.colorScheme.surface
                                    }
                                )
                                .padding(padding)
                                .consumeWindowInsets(padding)
                        ) {
                            NavDisplay(
                                modifier = Modifier.fillMaxSize(),
                                backStack = rootBackStack,
                                entryDecorators = listOf(
                                    rememberSceneSetupNavEntryDecorator(),
                                    rememberSavedStateNavEntryDecorator(),
                                    rememberViewModelStoreNavEntryDecorator()
                                ),
                                entryProvider = entryProvider {
                                    authenticationGraph(
                                        rootBackStack = rootBackStack,
                                        userDetailsProvider = userDetailsProvider
                                    )
                                    recommendationsGraph(
                                        rootBackStack = rootBackStack,
                                        recommendationsBackStack = recommendationsBackStack,
                                        userDetailsProvider = userDetailsProvider
                                    )
                                    subscriptionsGraph(
                                        rootBackStack = rootBackStack,
                                        subscriptionsBackStack = subscriptionsBackStack,
                                        userDetailsProvider = userDetailsProvider
                                    )
                                    searchGraph(
                                        rootBackStack = rootBackStack,
                                        searchBackStack = searchBackStack,
                                        userDetailsProvider = userDetailsProvider
                                    )
                                    userGraph(
                                        rootBackStack = rootBackStack,
                                        player = player,
                                        userBackStack = userBackStack,
                                        userDetailsProvider = userDetailsProvider
                                    )
                                    videoGraph(
                                        rootBackStack = rootBackStack,
                                        player = player,
                                        userDetailsProvider = userDetailsProvider,
                                        currentBackStack = when (currentRoute) {
                                            Route.Recommendations -> recommendationsBackStack
                                            Route.Subscriptions -> subscriptionsBackStack
                                            Route.Search -> searchBackStack
                                            is Route.User -> userBackStack
                                            else -> recommendationsBackStack
                                        }
                                    )
                                }
                            )
                            if (currentRoute !is Route.Video && isPlayerPrepared(player)) {
                                MiniPlayer(
                                    player = player,
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

    private fun setMediaHandlers() {
        mediaSession = MediaSession(this, "PlayerMediaSession")
        mediaSession.setCallback(mediaHandler)
        mediaSession.isActive = true

        mediaReceiver = MediaBroadcastReceiver()
        registerReceiver(mediaReceiver, IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY))
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