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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.edit
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.ui.PlayerView
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import mikhail.shell.video.hosting.data.DSWithTokenFactory
import mikhail.shell.video.hosting.domain.providers.UserDetailsProvider
import mikhail.shell.video.hosting.presentation.Route
import mikhail.shell.video.hosting.presentation.channel.ChannelScreen
import mikhail.shell.video.hosting.presentation.channel.ChannelScreenViewModel
import mikhail.shell.video.hosting.presentation.signin.password.SignInScreen
import mikhail.shell.video.hosting.presentation.signin.password.SignInWithPasswordViewModel
import mikhail.shell.video.hosting.presentation.signup.password.SignUpScreen
import mikhail.shell.video.hosting.presentation.signup.password.SignUpWithPasswordViewModel
import mikhail.shell.video.hosting.presentation.video.page.VideoScreen
import mikhail.shell.video.hosting.presentation.video.page.VideoScreenViewModel
import mikhail.shell.video.hosting.ui.theme.VideoHostingTheme
import javax.inject.Inject

@UnstableApi
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var userDetailsProvider: UserDetailsProvider
    @Inject
    lateinit var dsFactory: DefaultMediaSourceFactory

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
                        composable<Route.SignUp> {
                            val viewModel = hiltViewModel<SignUpWithPasswordViewModel>()
                            val state by viewModel.state.collectAsStateWithLifecycle()
                            val sharedPref =
                                LocalContext.current.applicationContext.getSharedPreferences(
                                    "user_details",
                                    MODE_PRIVATE
                                )
                            val coroutineScope = rememberCoroutineScope()
                            SignUpScreen(
                                state = state,
                                onSubmit =  {
                                    viewModel.signUp(it)
                                },
                                onSuccess = {
                                    coroutineScope.launch {
                                        navController.navigate(Route.Channel(1))
                                    }
                                }
                            )
                        }
                        composable<Route.SignIn> {
                            val viewModel = hiltViewModel<SignInWithPasswordViewModel>()
                            val state by viewModel.state.collectAsStateWithLifecycle()
                            val sharedPref =
                                LocalContext.current.applicationContext.getSharedPreferences(
                                    "user_details",
                                    MODE_PRIVATE
                                )
                            SignInScreen(
                                state = state,
                                onSubmit = { email, password ->
                                    viewModel.signIn(email, password)
                                },
                                onSuccess = {
                                    if (state.authModel != null) {
                                        sharedPref.edit {
                                            putLong("userId", state.authModel!!.userId)
                                            putString("token", state.authModel!!.token)
                                            commit()
                                        }
                                    }
                                    navController.navigate(Route.Channel(1))
                                },
                                onSigningUp = {
                                    navController.navigate(Route.SignUp)
                                }
                            )
                        }
                        composable<Route.Channel> {
                            val channelRouteInfo = it.toRoute<Route.Channel>()
                            val userId = userDetailsProvider.getUserId()
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
                            val videoId = videoRouteInfo.videoId
                            val context = LocalContext.current
                            val videoScreenViewModel =
                                hiltViewModel<VideoScreenViewModel, VideoScreenViewModel.Factory> { factory ->
                                    val userId = userDetailsProvider.getUserId()
                                    val player = ExoPlayer.Builder(context)
                                        .setMediaSourceFactory(dsFactory)
                                        .build()
                                    factory.create(player, userId, videoId)
                                }
                            val state by videoScreenViewModel.state.collectAsStateWithLifecycle()
                            VideoScreen(
                                state = state,
                                player = videoScreenViewModel.player,
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
