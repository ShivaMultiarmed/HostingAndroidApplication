package mikhail.shell.video.hosting

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Outline
import android.graphics.PixelFormat
import android.media.AudioManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.MotionEvent
import android.view.View
import android.view.ViewOutlineProvider
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.eventFlow
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
import mikhail.shell.video.hosting.presentation.utils.LifecycleOwnerHolder
import mikhail.shell.video.hosting.presentation.utils.PipRow
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
    private var shouldMiniPlay = false
    private var shouldPipPlay = false
    private lateinit var windowManager: WindowManager
    private var pipRoot: View? = null
    private lateinit var pipLifecycleOwner: LifecycleOwnerHolder
    private lateinit var audioReceiver: AudioBroadcastReceiver
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setPrimaryContent()
        windowManager = getSystemService(WindowManager::class.java)
        pipLifecycleOwner = LifecycleOwnerHolder().apply { updateState(Lifecycle.State.CREATED) }
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
                        shouldMiniPlay = shouldMiniPlay()
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
                    if (shouldMiniPlay && isPrepared) {
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
        onPipClose()
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
                isPrepared = this@MainActivity.isPrepared
                player.removeListener(playerListener)
            }
        }
        return isPrepared
    }

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        shouldPipPlay = true
    }

    override fun onStop() {
        if (isPrepared && shouldPipPlay) {
            val pipEnabled = Settings.canDrawOverlays(this)
            if (pipEnabled) {
                if (pipRoot == null) {
                    pipLifecycleOwner.updateState(Lifecycle.State.RESUMED)
                    pipRoot = createPipLayout(this)
                    window.setFlags(
                        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                    )
                    windowManager.addView(
                        pipRoot,
                        createPipLayoutParameters()
                    )
                    pipRoot?.setOnTouchListener { view, event ->
                        view.performClick()
                        when(event.action) {
                            MotionEvent.ACTION_MOVE -> {
                                view.x += event.rawX
                                view.y += event.rawY
                                true
                            }
                            else -> false
                        }
                    }
                }
            }
        }
        super.onStop()
    }

    private fun createPipLayoutParameters(): WindowManager.LayoutParams {
        val density = resources.displayMetrics.density
        return WindowManager.LayoutParams(
            (200 * density).toInt(),
            WindowManager.LayoutParams.WRAP_CONTENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                WindowManager.LayoutParams.TYPE_PHONE
            },
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        )
    }

    private fun createPipLayout(context: Context): View {

        return ComposeView(context).apply {
            setViewTreeSavedStateRegistryOwner(this@MainActivity)
            setViewTreeLifecycleOwner(pipLifecycleOwner)
            background = getDrawable(R.drawable.rounder_shape)
            clipChildren = true
            clipToPadding = true
            clipToOutline = true
            val density = resources.displayMetrics.density
            outlineProvider = object : ViewOutlineProvider() {
                override fun getOutline(view: View, outline: Outline) {
                    outline.setRoundRect(0, 0, view.width, view.height, 10 * density)
                }
            }
            setContent {
                CompositionLocalProvider(
                    LocalLifecycleOwner provides pipLifecycleOwner
                ) {
                    Box(
                        modifier = Modifier
                            .width(200.dp)
                            .clip(RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        PlayerComponent(
                            modifier = Modifier,
                            player = player
                        )
                        PipRow(
                            onOpenUp = {
                                startActivity(
                                    Intent(
                                        this@MainActivity,
                                        MainActivity::class.java
                                    ).apply {
                                        val videoId = player.currentMediaItem
                                            ?.localConfiguration?.uri?.toString()!!
                                            .split("/").dropLast(1).last().toLong()
                                        putExtra("videoId", videoId)
                                    }
                                )
                                onPipClose()
                            },
                            onClose = {
                                player.pause()
                                onPipClose()
                            }
                        )
                    }
                }
            }
        }
    }

    private fun onPipClose() {
        if (pipRoot != null) {
            pipLifecycleOwner.updateState(Lifecycle.State.STARTED)
            windowManager.removeView(pipRoot)
            pipRoot = null
        }
        shouldPipPlay = false
    }

    override fun onDestroy() {
        unregisterReceiver(audioReceiver)
        super.onDestroy()
    }
}
