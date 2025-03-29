package mikhail.shell.video.hosting.presentation.activities

import android.content.IntentFilter
import android.media.AudioManager
import android.media.session.MediaSession
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import mikhail.shell.video.hosting.domain.providers.UserDetailsProvider
import mikhail.shell.video.hosting.presentation.navigation.BottomNavBar
import mikhail.shell.video.hosting.presentation.navigation.Route
import mikhail.shell.video.hosting.presentation.navigation.authentication.authenticationGraph
import mikhail.shell.video.hosting.presentation.navigation.channel.channelGraph
import mikhail.shell.video.hosting.presentation.navigation.searchRoute
import mikhail.shell.video.hosting.presentation.navigation.user.userGraph
import mikhail.shell.video.hosting.presentation.navigation.video.videoGraph
import mikhail.shell.video.hosting.presentation.video.MiniPlayer
import mikhail.shell.video.hosting.receivers.MediaBroadcastReceiver
import mikhail.shell.video.hosting.receivers.MediaHandler
import mikhail.shell.video.hosting.ui.theme.VideoHostingTheme
import javax.inject.Inject

@UnstableApi
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var userDetailsProvider: UserDetailsProvider
    @Inject
    lateinit var player: Player
    private lateinit var navController: NavController
    private lateinit var mediaReceiver: MediaBroadcastReceiver
    @Inject
    lateinit var mediaHandler: MediaHandler
    private lateinit var mediaSession: MediaSession
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setPrimaryContent()
        setMediaHandlers()
    }
    private fun setPrimaryContent() {
        setContent {
            VideoHostingTheme {
                val navController = rememberNavController()
                this.navController = navController
                val backStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = backStackEntry?.destination?.route
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        if (backStackEntry != null && currentRoute !in listOf(
                                Route.Authentication.SignIn::class.qualifiedName,
                                Route.Authentication.SignUp::class.qualifiedName,
                                Route.Video::class.qualifiedName
                            )
                        ) {
                            val userId = userDetailsProvider.getUserId()
                            BottomNavBar(
                                onClick = {
                                    navController.navigate(it.route) {
                                        val destinationToPopUpTo = navController.currentDestination?.id
                                            ?: navController.graph.findStartDestination().id
                                        popUpTo(destinationToPopUpTo) {
                                            saveState = true
                                            inclusive = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                userId = userId
                            )
                        }
                    }
                ) { padding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding)
                    ) {
                        NavHost(
                            modifier = Modifier
                                .fillMaxSize(),
                            navController = navController,
                            startDestination = if (userDetailsProvider.getUserId() == 0L) Route.Authentication else getHomeDestination()
                        ) {
                            authenticationGraph(navController)
                            videoGraph(navController, player, userDetailsProvider)
                            channelGraph(navController, userDetailsProvider)
                            userGraph(navController,userDetailsProvider)
                            searchRoute(navController)
                        }
                    }
                    if (shouldMiniPlay() && isPlayerPrepared()) {
                        MiniPlayer(
                            player = player,
                            onOpenUp = {
                                navController.navigate(Route.Video.View(it))
                            }
                        )
                    }
                }
            }
        }
    }

    private fun getHomeDestination(): Route {
        intent.extras?.let {
            if (it.getLong("videoId") != 0L) {
                return Route.Video.View(it.getLong("videoId"))
            }
            it.clear()
        }
        return Route.Search
    }

    private fun setMediaHandlers() {
        mediaSession = MediaSession(this, "PlayerMediaSession")
        mediaSession.setCallback(mediaHandler)
        mediaSession.isActive = true

        mediaReceiver = MediaBroadcastReceiver()
        registerReceiver(mediaReceiver, IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY))
    }

    @Composable
    private fun shouldMiniPlay(): Boolean {
        val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
        return Route.Video::class.qualifiedName!! !in currentRoute.toString()
                && currentRoute != null
    }

    @Composable
    private fun isPlayerPrepared(): Boolean {
        val initialValue = player.currentMediaItem != null
        var isPrepared by rememberSaveable { mutableStateOf(initialValue) }
        DisposableEffect(Unit) {
            val playerListener = object : Player.Listener {
                override fun onMediaItemTransition(
                    mediaItem: MediaItem?,
                    reason: Int
                ) {
                    val uri = mediaItem?.localConfiguration?.uri?.toString()
                    isPrepared = uri != null
                }
            }
            player.addListener(playerListener)
            onDispose {
                isPrepared = player.currentMediaItem != null
                player.removeListener(playerListener)
            }
        }
        return isPrepared
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
