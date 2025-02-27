package mikhail.shell.video.hosting

import android.content.IntentFilter
import android.media.AudioManager
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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.eventFlow
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import mikhail.shell.video.hosting.domain.providers.UserDetailsProvider
import mikhail.shell.video.hosting.domain.services.AudioBroadcastReceiver
import mikhail.shell.video.hosting.presentation.navigation.BottomNavBar
import mikhail.shell.video.hosting.presentation.navigation.Route
import mikhail.shell.video.hosting.presentation.navigation.channelRoute
import mikhail.shell.video.hosting.presentation.navigation.createChannelRoute
import mikhail.shell.video.hosting.presentation.navigation.profileRoute
import mikhail.shell.video.hosting.presentation.navigation.searchRoute
import mikhail.shell.video.hosting.presentation.navigation.signInRoute
import mikhail.shell.video.hosting.presentation.navigation.signUpRoute
import mikhail.shell.video.hosting.presentation.navigation.subscriptionsRoute
import mikhail.shell.video.hosting.presentation.navigation.uploadVideoRoute
import mikhail.shell.video.hosting.presentation.navigation.videoEditRoute
import mikhail.shell.video.hosting.presentation.navigation.videoRoute
import mikhail.shell.video.hosting.presentation.video.MiniPlayer
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
    private lateinit var audioReceiver: AudioBroadcastReceiver
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setPrimaryContent()
        audioReceiver = AudioBroadcastReceiver()
        registerReceiver(audioReceiver, IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY))
    }

    private fun setPrimaryContent() {
        setContent {
            VideoHostingTheme {
                val navController = rememberNavController()
                this.navController = navController
                val backStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = backStackEntry?.destination?.route
                val lifecycleOwner = LocalLifecycleOwner.current
                val lifecycleEvent by lifecycleOwner.lifecycle.eventFlow.collectAsStateWithLifecycle(Lifecycle.Event.ON_CREATE)
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        if (backStackEntry != null && currentRoute !in listOf(
                                Route.SignIn::class.qualifiedName, Route.SignUp::class.qualifiedName
                            )
                        ) {
                            BottomNavBar(
                                onClick = {
                                    navController.navigate(it.route)
                                }
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
                            startDestination = if (userDetailsProvider.getUserId() == 0L) Route.SignIn else getHomeDestination()
                        ) {
                            signUpRoute(navController)
                            signInRoute(navController)
                            channelRoute(navController, userDetailsProvider)
                            videoRoute(navController, player, userDetailsProvider)
                            searchRoute(navController)
                            createChannelRoute(navController, userDetailsProvider)
                            uploadVideoRoute(navController, userDetailsProvider)
                            profileRoute(navController, userDetailsProvider)
                            subscriptionsRoute(navController, userDetailsProvider)
                            videoEditRoute(navController, userDetailsProvider)
                        }
                    }
                    if (shouldMiniPlay() && isPlayerPrepared()) {
                        MiniPlayer(
                            player = player,
                            onOpenUp = {
                                navController.navigate(Route.Video(it))
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
                return Route.Video(it.getLong("videoId"))
            }
            it.clear()
        }
        return Route.Search
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
        unregisterReceiver(audioReceiver)
        super.onDestroy()
    }
}
