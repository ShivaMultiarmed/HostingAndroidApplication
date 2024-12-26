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
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import mikhail.shell.video.hosting.domain.providers.UserDetailsProvider
import mikhail.shell.video.hosting.presentation.navigation.BottomNavBar
import mikhail.shell.video.hosting.presentation.navigation.Route
import mikhail.shell.video.hosting.presentation.channel.create.CreateChannelScreen
import mikhail.shell.video.hosting.presentation.channel.create.CreateChannelViewModel
import mikhail.shell.video.hosting.presentation.channel.screen.ChannelScreen
import mikhail.shell.video.hosting.presentation.channel.screen.ChannelScreenViewModel
import mikhail.shell.video.hosting.presentation.navigation.channelRoute
import mikhail.shell.video.hosting.presentation.navigation.createChannelRoute
import mikhail.shell.video.hosting.presentation.navigation.profileRoute
import mikhail.shell.video.hosting.presentation.navigation.searchRoute
import mikhail.shell.video.hosting.presentation.navigation.signInRoute
import mikhail.shell.video.hosting.presentation.navigation.signUpRoute
import mikhail.shell.video.hosting.presentation.navigation.uploadVideoRoute
import mikhail.shell.video.hosting.presentation.navigation.videoRoute
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
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        val backStackEntry by navController.currentBackStackEntryAsState()
                        if (backStackEntry?.destination?.route !in
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
                    NavHost(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding),
                        navController = navController,
                        startDestination = Route.SignIn
                    ) {
                        signUpRoute(navController)
                        signInRoute(navController)
                        channelRoute(navController, userDetailsProvider)
                        videoRoute(navController, dsFactory, userDetailsProvider)
                        searchRoute()
                        createChannelRoute(navController, userDetailsProvider)
                        uploadVideoRoute(navController, userDetailsProvider)
                        profileRoute(navController, userDetailsProvider)
                    }
                }
            }
        }
    }
}
