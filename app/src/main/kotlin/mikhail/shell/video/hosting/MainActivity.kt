package mikhail.shell.video.hosting

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import mikhail.shell.video.hosting.domain.providers.UserDetailsProvider
import mikhail.shell.video.hosting.presentation.Route
import mikhail.shell.video.hosting.presentation.channel.create.CreateChannelScreen
import mikhail.shell.video.hosting.presentation.channel.create.CreateChannelViewModel
import mikhail.shell.video.hosting.presentation.channel.screen.ChannelScreen
import mikhail.shell.video.hosting.presentation.channel.screen.ChannelScreenViewModel
import mikhail.shell.video.hosting.presentation.signin.password.SignInScreen
import mikhail.shell.video.hosting.presentation.signin.password.SignInWithPasswordViewModel
import mikhail.shell.video.hosting.presentation.signup.password.SignUpScreen
import mikhail.shell.video.hosting.presentation.signup.password.SignUpWithPasswordViewModel
import mikhail.shell.video.hosting.presentation.video.screen.VideoScreen
import mikhail.shell.video.hosting.presentation.video.screen.VideoScreenViewModel
import mikhail.shell.video.hosting.presentation.video.search.SearchVideosScreen
import mikhail.shell.video.hosting.presentation.video.search.SearchVideosViewModel
import mikhail.shell.video.hosting.presentation.video.upload.UploadVideoScreen
import mikhail.shell.video.hosting.presentation.video.upload.UploadVideoViewModel
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
                val navController = rememberNavController()
                Scaffold(
                    modifier = Modifier.fillMaxSize()
                ) { padding ->
                    NavHost(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding),
                        navController = navController,
                        startDestination = Route.UploadVideo
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
                                onSubmit = {
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
                        composable<Route.Search> {
                            val viewModel = hiltViewModel<SearchVideosViewModel>()
                            val state by viewModel.state.collectAsStateWithLifecycle()
                            SearchVideosScreen(
                                state = state,
                                onSubmit = {
                                    viewModel.search(it)
                                },
                                onScrollToBottom = { partNumber, partSize ->
                                    viewModel.loadVideoPart(partSize, partNumber)
                                }
                            )
                        }
                        composable<Route.CreateChannel> {
                            val viewModel = hiltViewModel<CreateChannelViewModel>()
                            val state by viewModel.state.collectAsStateWithLifecycle()
                            val coroutineScope = rememberCoroutineScope()
                            CreateChannelScreen(
                                state = state,
                                onSubmit = {
                                    viewModel.createChannel(it)
                                },
                                onSuccess = {
                                    coroutineScope.launch {
                                        navController.navigate(Route.Channel(it.channelId!!))
                                    }
                                }
                            )
                        }
                        composable<Route.UploadVideo> {
                            val userId = userDetailsProvider.getUserId()
                            val viewModel =
                                hiltViewModel<UploadVideoViewModel, UploadVideoViewModel.Factory>() {
                                    it.create(userId)
                                }
                            val state by viewModel.state.collectAsStateWithLifecycle()
                            val coroutineScope = rememberCoroutineScope()
                            UploadVideoScreen(
                                state = state,
                                onSubmit = {
                                    viewModel.uploadVideo(it)
                                },
                                onSuccess = {
                                    coroutineScope.launch {
                                        delay(1000)
                                        navController.navigate(Route.Video(it.videoId!!))
                                    }
                                },
                                onRefresh = {
                                    viewModel.loadChannels()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
