package mikhail.shell.video.hosting

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import mikhail.shell.video.hosting.domain.providers.UserDetailsProvider
import mikhail.shell.video.hosting.domain.services.PlayerService
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
import mikhail.shell.video.hosting.presentation.utils.PipContainer
import mikhail.shell.video.hosting.presentation.utils.PlayerComponent
import mikhail.shell.video.hosting.ui.theme.VideoHostingTheme
import javax.inject.Inject

@UnstableApi
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var userDetailsProvider: UserDetailsProvider
    private var playerService: PlayerService? = null
    @Inject
    lateinit var player: Player
    private var isBound: Boolean = false
    private lateinit var navController: NavController
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            VideoHostingTheme {
                val navController = rememberNavController()
                this.navController = navController
                val backStackEntry by navController.currentBackStackEntryAsState()
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        if (backStackEntry != null && backStackEntry?.destination?.route !in
                            listOf(
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
                        var isPrepared by rememberSaveable { mutableStateOf(false) }
                        player.addListener(
                            object : Player.Listener {
                                override fun onMediaItemTransition(
                                    mediaItem: MediaItem?,
                                    reason: Int
                                ) {
                                    val uri = mediaItem?.localConfiguration?.uri?.toString()
                                    isPrepared = uri != null
                                }
                            }
                        )
                        if (isPrepared && Route.Video::class.qualifiedName!! !in backStackEntry?.destination?.route.toString()) {
                            PipContainer {
                                PlayerComponent(
                                    modifier = Modifier
                                        .width(300.dp)
                                        .wrapContentHeight(),
                                    player = player
                                )
                            }
                        }
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

    private val playerServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, serviceBinder: IBinder?) {
            isBound = true
            playerService = (serviceBinder as PlayerService.PlayerBinder).getService()
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            isBound = false
            playerService = null
        }
    }

    override fun onStart() {
        super.onStart()
        bindService(
            Intent(this, PlayerService::class.java),
            playerServiceConnection,
            Context.BIND_AUTO_CREATE
        )
    }

    override fun onStop() {
        if (isBound) {
            isBound = false
            unbindService(playerServiceConnection)
        }
        super.onStop()
    }
}
