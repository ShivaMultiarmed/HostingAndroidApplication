package mikhail.shell.video.hosting.presentation.utils

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds

@Composable
@Preview(device = "id:pixel_9a")
private fun RestartableBoxPreview() {
    var isStarting by remember { mutableStateOf(false) }
    val lazyGridState = rememberLazyGridState()
    val coroutineScope = rememberCoroutineScope()
    val isAtStart by remember {
        derivedStateOf {
            lazyGridState.isAtStart()
        }
    }
    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            RestartableBox(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1000.dp),
                onStart = {
                    coroutineScope.launch {
                        isStarting = true
                        delay(5.seconds)
                        isStarting = false
                    }
                },
                canStart = isAtStart,
                isStarting = isStarting
            ) {
                LazyVerticalGrid (
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.4f),
                    state = lazyGridState,
                    columns = GridCells.Fixed(1)
                ) {
                    items(20) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp)
                        ) {
                            Text(
                                text = "Item #$it"
                            )
                        }
                    }
                }
            }
        }
    }
}