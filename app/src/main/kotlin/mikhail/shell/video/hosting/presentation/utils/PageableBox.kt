package mikhail.shell.video.hosting.presentation.utils

import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import mikhail.shell.video.hosting.domain.errors.Error

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun <I> PageableBox(
    modifier: Modifier = Modifier,
    state: PageableBoxState<I>,
    itemComponent: @Composable (I) -> Unit,
    emptyComponent: (@Composable () -> Unit)? = null,
    onReload: () -> Unit,
    onReachedEnd: () -> Unit
) {
    val windowSize = calculateWindowSizeClass(LocalActivity.current!!)
    val isWidthCompact = windowSize.widthSizeClass == WindowWidthSizeClass.Compact
    Box(
        modifier = modifier
    ) {
        if (state.items.isNotEmpty()) {
            val reachedBottom by remember {
                derivedStateOf {
                    state.gridState.reachedBottom(buffer = 4)
                }
            }
            LazyVerticalGrid(
                modifier = Modifier.then(
                    if (isWidthCompact) {
                        Modifier
                    } else {
                        Modifier
                            .padding(top = 10.dp)
                            .padding(horizontal = 10.dp)
                    }
                ),
                columns = GridCells.Adaptive(300.dp),
                state = state.gridState,
                verticalArrangement = Arrangement.spacedBy(if (isWidthCompact) 0.dp else 10.dp),
                horizontalArrangement = Arrangement.spacedBy(if (isWidthCompact) 0.dp else 10.dp),
            ) {
                items(state.items) {
                    itemComponent(it)
                }
                item(
                    span = {
                        GridItemSpan(maxLineSpan)
                    }
                ) {
                    if (state.isLoading) {
                        StartingComponent(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(vertical = 10.dp)
                        )
                    } else if (state.error != null && state.hasMore) {
                        ErrorComponent(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(vertical = 10.dp),
                            onRetry = onReload
                        )
                    }
                }
            }
            LaunchedEffect(reachedBottom, state.hasMore) {
                if (reachedBottom && state.hasMore) {
                    onReachedEnd()
                }
            }
        } else {
            emptyComponent?.invoke()
        }
    }
}

@Stable
class PageableBoxState<out I>(
    val gridState: LazyGridState,
    val items: List<I>,
    val hasMore: Boolean = true,
    val error: Error? = null,
    val isLoading: Boolean = false
)

@Composable
fun <I> rememberPageableBoxState(
    items: List<I>,
    hasMore: Boolean,
    error: Error?,
    isLoading: Boolean
): PageableBoxState<I> {
    val gridState = rememberLazyGridState()
    return remember(items, hasMore, error, isLoading) {
        PageableBoxState(
            gridState = gridState,
            items = items,
            hasMore = hasMore,
            error = error,
            isLoading = isLoading
        )
    }
}

fun LazyGridState.isAtStart(): Boolean {
    return firstVisibleItemIndex == 0 && firstVisibleItemScrollOffset == 0
}