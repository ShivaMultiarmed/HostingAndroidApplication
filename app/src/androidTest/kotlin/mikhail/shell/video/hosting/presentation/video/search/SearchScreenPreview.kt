package mikhail.shell.video.hosting.presentation.video.search

import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import mikhail.shell.video.hosting.domain.errors.TextError
import mikhail.shell.video.hosting.presentation.utils.FieldState

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
@Preview
fun SearchScreenPreview() {
    SearchScreen(
        state = SearchScreenState(
            query = FieldState("Really long text", error = TextError.LONG)
        ),
        onAction = {},
        windowSize = WindowSizeClass.calculateFromSize(
            DpSize(
                width = 480.dp,
                height = 900.dp
            )
        ),
        snackBarHostState = remember { SnackbarHostState() }
    )
}