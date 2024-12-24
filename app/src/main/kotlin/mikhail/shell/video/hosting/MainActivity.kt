package mikhail.shell.video.hosting

import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.OptIn
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.edit
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.ui.PlayerView
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import dagger.hilt.android.AndroidEntryPoint
import mikhail.shell.video.hosting.data.DSWithTokenFactory
import mikhail.shell.video.hosting.presentation.Route
import mikhail.shell.video.hosting.presentation.channel.ChannelScreen
import mikhail.shell.video.hosting.presentation.channel.ChannelScreenViewModel
import mikhail.shell.video.hosting.presentation.signin.password.SignInScreen
import mikhail.shell.video.hosting.presentation.signin.password.SignInWithPasswordViewModel
import mikhail.shell.video.hosting.presentation.video.page.VideoScreen
import mikhail.shell.video.hosting.presentation.video.page.VideoScreenViewModel
import mikhail.shell.video.hosting.ui.theme.VideoHostingTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @OptIn(UnstableApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            VideoHostingTheme {
                // VideoScreenComposable()
                val navController = rememberNavController()
                Scaffold(
                    modifier = Modifier.fillMaxSize()
                ) { padding ->
                    NavHost(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding),
                        navController = navController,
                        startDestination = Route.SignIn
                    ) {
                        composable<Route.SignIn> {
                            val viewModel = hiltViewModel<SignInWithPasswordViewModel>()
                            val state by viewModel.state.collectAsStateWithLifecycle()
                            val sharedPref = LocalContext.current.getSharedPreferences(
                                "auth_details",
                                MODE_PRIVATE
                            )

                            SignInScreen(
                                state = state,
                                onSubmit = { email, password ->
                                    viewModel.signIn(email, password)
                                },
                                onSuccess = {
                                    navController.navigate(Route.Channel(1))
                                    if (state.authModel != null) {
                                        sharedPref.edit {
                                            putLong("userId", state.authModel!!.userId)
                                            putString("token", state.authModel!!.token)
                                        }
                                    }
                                }
                            )
                        }
                        composable<Route.Channel> {
                            val channelRouteInfo = it.toRoute<Route.Channel>()
                            val userId = 1L
                            val channelId = channelRouteInfo.channelId
                            val viewModel =
                                hiltViewModel<ChannelScreenViewModel, ChannelScreenViewModel.Factory> {
                                    it.create(channelId, userId)
                                }
                            val state by viewModel.state.collectAsStateWithLifecycle()
                            ChannelScreen(
                                state = state,
                                onRefresh = {
                                    viewModel.loadChannelInfo()
                                    viewModel.loadVideosPart()
                                },
                                onSubscription = {

                                },
                                onVideoClick = {
                                    navController.navigate(Route.Video(it))
                                }
                            )
                        }
                        composable<Route.Video> {
                            val videoRouteInfo = it.toRoute<Route.Video>()
                            val userId = 1L
                            val videoId = videoRouteInfo.videoId
                            val context = LocalContext.current
                            val player = ExoPlayer.Builder(context)
                                .setMediaSourceFactory(DefaultMediaSourceFactory(DSWithTokenFactory("eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIiwiaWF0IjoxNzM1MDEzNDQyLCJleHAiOjE3MzU4Nzc0NDJ9.atuqn9hAgf-MWKskGr4nh-Zk_grmXtPIP6svCxJ-Doc")))
                                .build()
                            val videoScreenViewModel =
                                hiltViewModel<VideoScreenViewModel, VideoScreenViewModel.Factory> { factory ->
                                    factory.create(player, userId, videoId)
                                }
                            val state by videoScreenViewModel.state.collectAsStateWithLifecycle()
                            VideoScreen(
                                state = state,
                                exoPlayerConnection = { context ->
                                    PlayerView(context).also {
                                        it.layoutParams = ViewGroup.LayoutParams(
                                            ViewGroup.LayoutParams.MATCH_PARENT,
                                            ViewGroup.LayoutParams.WRAP_CONTENT
                                        )
                                        it.player = videoScreenViewModel.player
                                        videoScreenViewModel.player.addListener(object :
                                            Player.Listener {
                                            override fun onPlaybackStateChanged(playbackState: Int) {
                                                when (playbackState) {
                                                    Player.STATE_IDLE -> Log.d(
                                                        "ExoPlayer",
                                                        "Player is idle"
                                                    )

                                                    Player.STATE_BUFFERING -> Log.d(
                                                        "ExoPlayer",
                                                        "Buffering"
                                                    )

                                                    Player.STATE_READY -> Log.d(
                                                        "ExoPlayer",
                                                        "Ready to play"
                                                    )

                                                    Player.STATE_ENDED -> Log.d(
                                                        "ExoPlayer",
                                                        "Playback ended"
                                                    )
                                                }
                                            }

                                            override fun onPlayerError(error: PlaybackException) {
                                                Log.e(
                                                    "ExoPlayer",
                                                    "Playback error: ${error.message}"
                                                )
                                            }
                                        })
                                    }
                                },
                                onRefresh = {
                                    videoScreenViewModel.loadVideo()
                                },
                                onRate = {
                                    videoScreenViewModel.rate(it)
                                },
                                onSubscribe = {

                                },
                                onChannelLinkClick = {
                                    navController.navigate(Route.Channel(it))
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
