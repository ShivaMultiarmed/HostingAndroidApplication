package mikhail.shell.video.hosting.presentation.user.screen

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.tooling.preview.Preview
import mikhail.shell.video.hosting.domain.models.ImageSize
import mikhail.shell.video.hosting.presentation.user.models.UserUi

@Composable
@Preview
fun ProfileScreenPreview () {
    ProfileScreen(
        state = ProfileScreenState(
            signedInUserId = 534,
            user = UserUi(
                userId = 756,
                nick = "some_nick",
                avatar = mapOf(ImageSize.LARGE to "http://something.ru"),
                name = null,
                bio = null,
                tel = null,
                email = null
            ),
            channelsState = OwnedChannelsState(
                channels = listOf()
            )
        ),
        owns = true,
        onAction = {},
        snackBarHostState = remember { SnackbarHostState() }
    )
}