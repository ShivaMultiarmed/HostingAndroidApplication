package mikhail.shell.video.hosting.presentation.activities

import android.content.IntentFilter
import android.content.res.Configuration
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import mikhail.shell.video.hosting.domain.providers.UserDetailsProvider
import mikhail.shell.video.hosting.presentation.exoplayer.isPlayerPrepared
import mikhail.shell.video.hosting.presentation.navigation.authentication.authenticationGraph
import mikhail.shell.video.hosting.presentation.navigation.channel.channelGraph
import mikhail.shell.video.hosting.presentation.navigation.common.BottomNavBar
import mikhail.shell.video.hosting.presentation.navigation.common.Route
import mikhail.shell.video.hosting.presentation.navigation.user.userGraph
import mikhail.shell.video.hosting.presentation.navigation.video.videoGraph
import mikhail.shell.video.hosting.presentation.video.MiniPlayer
import mikhail.shell.video.hosting.presentation.video.shouldShowMiniPlayer
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
                val activity = LocalActivity.current!!
                val view = LocalView.current
                val navController = rememberNavController()
                this.navController = navController
                val backStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = backStackEntry?.destination?.route
                var isVideoFullScreened by rememberSaveable { mutableStateOf(false) }
                val orientation = LocalConfiguration.current.orientation
                val statusBarIconsColor = MaterialTheme.colorScheme.onSurface
                LaunchedEffect (currentRoute) {
                    if (Route.Video.View::class.qualifiedName?.let { currentRoute?.contains(it) } == true) {
                        WindowCompat.getInsetsController(activity.window, view).isAppearanceLightStatusBars = false
                    } else {
                        WindowCompat.getInsetsController(activity.window, view).isAppearanceLightStatusBars = (statusBarIconsColor != DarkColorScheme.onSurface)
                    }
                }
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        if (backStackEntry != null && currentRoute !in listOf(
                                Route.Authentication.SignIn::class.qualifiedName,
                                Route.Authentication.SignUp::class.qualifiedName,
                                Route.Video::class.qualifiedName
                            ) && !(orientation == Configuration.ORIENTATION_LANDSCAPE
                                    && Route.Video.View::class.qualifiedName?.let { currentRoute?.contains(it) } != false
                                    || isVideoFullScreened)
                        ) {
                            val userId = userDetailsProvider.getUserId()
                            BottomNavBar(
                                onClick = {
                                    navController.navigate(it.route) {
                                        val destinationToPopUpTo =
                                            navController.currentDestination?.id
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
                            .background(
                                if (Route.Video.View::class.qualifiedName?.let { currentRoute?.contains(it) } == true) {
                                    Color.Black
                                } else {
                                    MaterialTheme.colorScheme.surface
                                }
                            )
                            .padding(padding)
                            .consumeWindowInsets(padding)
                    ) {
                        NavHost(
                            modifier = Modifier
                                .fillMaxSize(),
                            navController = navController,
                            startDestination = if (userDetailsProvider.getUserId() != 0L) Route.Video else Route.Authentication
                        ) {
                            authenticationGraph(navController)
                            videoGraph(
                                navController,
                                player,
                                userDetailsProvider
                            ) { isVideoFullScreened = it }
                            channelGraph(navController, userDetailsProvider)
                            userGraph(navController, userDetailsProvider, player)
                        }
                        if (shouldShowMiniPlayer(navController) && isPlayerPrepared(player)) {
                            MiniPlayer(
                                player = player,
                                onFullScreen = {
                                    navController.navigate(Route.Video.View(it))
                                }
                            )
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