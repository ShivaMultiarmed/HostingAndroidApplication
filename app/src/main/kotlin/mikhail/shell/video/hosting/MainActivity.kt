package mikhail.shell.video.hosting

import android.content.Context
import android.content.res.Configuration
import android.graphics.PixelFormat
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.view.View
import android.view.WindowManager
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
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import dagger.hilt.android.AndroidEntryPoint
import mikhail.shell.video.hosting.domain.providers.UserDetailsProvider
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
import mikhail.shell.video.hosting.presentation.utils.LifecycleOwnerHolder
import mikhail.shell.video.hosting.presentation.utils.PlayerComponent
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
    private var isPrepared = false
    private var shouldPlay = false
    private lateinit var windowManager: WindowManager
    private var pipRoot: View? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setPrimaryContent()
        windowManager = getSystemService(WindowManager::class.java)
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
                        shouldPlay = shouldPlay()
                        isPrepared = isPlayerPrepared()
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
                    if (shouldPlay && isPrepared) {
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

    override fun onRestart() {
        super.onRestart()
        if (pipRoot != null) {
            windowManager.removeView(pipRoot)
            pipRoot = null
        }
    }

    @Composable
    private fun shouldPlay(): Boolean {
        val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
        return Route.Video::class.qualifiedName!! !in currentRoute.toString()
                && currentRoute != null
    }

    @Composable
    private fun isPlayerPrepared(): Boolean {
        var isPrepared by rememberSaveable { mutableStateOf(false) }
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
                player.removeListener(playerListener)
            }
        }
        return isPrepared
    }

    override fun onStop() {
        if (isPrepared) {
            val pipEnabled = Settings.canDrawOverlays(this)
            if (pipEnabled) {
                if (pipRoot == null) {
                    pipRoot = createPipLayout(this)
                    window.setFlags(
                        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                    )
                    windowManager.addView(
                        pipRoot, WindowManager.LayoutParams(
                            WindowManager.LayoutParams.WRAP_CONTENT,
                            WindowManager.LayoutParams.WRAP_CONTENT,
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                            } else {
                                WindowManager.LayoutParams.TYPE_PHONE
                            },
                            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                            PixelFormat.TRANSLUCENT
                        ).also {
                            it.gravity = Gravity.TOP or Gravity.START
                            it.x = 100
                            it.y = 100
                        }
                    )
                }
            }
        }
        super.onStop()
    }

    private fun createPipLayout(context: Context): View {
        val lifecycleOwner = LifecycleOwnerHolder()
        return ComposeView(context).apply {
            setViewTreeSavedStateRegistryOwner(this@MainActivity)
            setViewTreeLifecycleOwner(lifecycleOwner)
            setContent {
//                CompositionLocalProvider(
//                    LocalLifecycleOwner provides lifecycleOwner
//                ) {
                    PlayerComponent(
                        player = player,
                        useHostLifecycle = false
                    )
//                }
            }
        }.also {
            lifecycleOwner.updateState(Lifecycle.State.RESUMED)
        }
    }

    override fun onPictureInPictureModeChanged(
        isInPictureInPictureMode: Boolean,
        newConfig: Configuration
    ) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig)
    }
}
