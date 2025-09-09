package mikhail.shell.video.hosting.presentation.utils

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import mikhail.shell.video.hosting.domain.errors.Error

@Composable
fun <I> PageableBox(
    modifier: Modifier = Modifier,
    itemComponent: @Composable (I) -> Unit,
    emptyComponent: (@Composable () -> Unit)? = null,
    items: List<I>,
    hasMore: Boolean = true,
    error: Error? = null,
    isLoading: Boolean = false,
    onLoad: () -> Unit,
    onReachedBottom: () -> Unit
) {
    Box(
        modifier = modifier
    ) {
        if (items.isNotEmpty()) {
            val lazyGridState = rememberLazyGridState()
            val reachedBottom by remember {
                derivedStateOf {
                    lazyGridState.reachedBottom(buffer = 4)
                }
            }
            LazyVerticalGrid(
                modifier = Modifier
                    .fillMaxSize(),
                columns = GridCells.Adaptive(300.dp),
                state = lazyGridState
            ) {
                items(items) {
                    itemComponent(it)
                }
                item(
                    span = {
                        GridItemSpan(maxLineSpan)
                    }
                ) {
                    if (isLoading) {
                        LoadingComponent(
                            modifier = Modifier.fillMaxSize()
                        )
                    } else if (error != null) {
                        ErrorComponent(
                            modifier = Modifier.fillMaxSize(),
                            onRetry = onLoad
                        )
                    }
                }
            }
            LaunchedEffect(reachedBottom) {
                if (reachedBottom && hasMore) {
                    onReachedBottom()
                }
            }
        } else {
            emptyComponent?.invoke()
        }

    }
}