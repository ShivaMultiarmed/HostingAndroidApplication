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
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.MutableStateFlow
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
    private var player = MutableStateFlow<Player?>(null)
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
                        player.collectAsStateWithLifecycle().value?.let { player ->
                            NavHost(
                                modifier = Modifier
                                    .fillMaxSize(),
                                navController = navController,
                                startDestination = if (userDetailsProvider.getUserId() == 0L) Route.SignIn else getHomeDestination()
                            ) {
                                signUpRoute(navController)
                                signInRoute(navController)
                                channelRoute(navController, userDetailsProvider)
                                videoRoute(navController, player, userDetailsProvider) { url ->
                                    playerService?.setVideo(url)
                                }
                                searchRoute(navController)
                                createChannelRoute(navController, userDetailsProvider)
                                uploadVideoRoute(navController, userDetailsProvider)
                                profileRoute(navController, userDetailsProvider)
                                subscriptionsRoute(navController, userDetailsProvider)
                                videoEditRoute(navController, userDetailsProvider)
                            }
                            val playerPreparedState = playerService?.isPrepared?.collectAsStateWithLifecycle()
                            if (playerPreparedState?.value == true &&
                                Route.Video::class.qualifiedName!! !in backStackEntry?.destination?.route.toString()) {
                                PipContainer {
                                    PlayerComponent(
                                        modifier = Modifier.fillMaxSize(),
                                        player = player
                                    )
                                }
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
            player.value = playerService!!.player
        }
        override fun onServiceDisconnected(name: ComponentName?) {
            isBound = false
            playerService = null
            player.value = null
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
//    override fun onUserLeaveHint() {
//        super.onUserLeaveHint()
//        val currentRoute = navController.currentBackStackEntry?.destination?.route
//        if (currentRoute != null) {
//            if (Route.Video::class.qualifiedName!! in currentRoute) {
//                enterPictureInPictureMode(
//                    PictureInPictureParams.Builder()
//                        .setAspectRatio(Rational(16, 9))
//                        .setActions(
//                            listOf(
//                                RemoteAction(
//                                    Icon.createWithResource(
//                                        this,
//                                        R.drawable.baseline_play_arrow_24
//                                    ),
//                                    "Проиграть",
//                                    "Проиграть",
//                                    PendingIntent.getService(
//                                        this,
//                                        0,
//                                        Intent(
//                                            this,
//                                            PlayerService::class.java
//                                        ).also {
//                                            it.putExtra("action", "PLAY")
//                                        },
//                                        PendingIntent.FLAG_MUTABLE
//                                    )
//                                )
//                            )
//                        ).build()
//                )
//            }
//        }
//    }
}
