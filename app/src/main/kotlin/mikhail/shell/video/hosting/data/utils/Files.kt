package mikhail.shell.video.hosting.data.utils

import android.content.Context
import coil.annotation.ExperimentalCoilApi
import coil.imageLoader
import coil.memory.MemoryCache

@OptIn(ExperimentalCoilApi::class)
fun Context.invalidateCache(uri: String) {
    imageLoader.diskCache?.remove(uri)
    imageLoader.memoryCache?.remove(MemoryCache.Key(uri))
}