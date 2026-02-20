package mikhail.shell.video.hosting.presentation.utils

import android.os.Build
import androidx.activity.compose.LocalActivity
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowHeightSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.window.layout.WindowMetricsCalculator

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun rememberIsSmallWindow(): Boolean {
    val activity = LocalActivity.current!!
    val windowSizeClass = calculateWindowSizeClass(activity)
    val configuration = LocalConfiguration.current

    return remember(windowSizeClass, configuration) {
        // Check if either dimension is Compact (handles multi-window/split-screen)
        val hasCompactDimension =
            windowSizeClass.widthSizeClass == WindowWidthSizeClass.Companion.Compact ||
                    windowSizeClass.heightSizeClass == WindowHeightSizeClass.Companion.Compact

        // Check physical screen characteristics
        val isPhysicallySmall = configuration.smallestScreenWidthDp < 600 ||
                configuration.screenWidthDp < 600 ||
                configuration.screenHeightDp < 600

        // Special handling for foldables
        val isFolded = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val metrics = WindowMetricsCalculator.Companion.getOrCreate()
                .computeCurrentWindowMetrics(activity)
            val bounds = metrics.bounds
            val density = activity.resources.displayMetrics.density
            bounds.width() / density < 600 || bounds.height() / density < 600
        } else true

        hasCompactDimension && (isPhysicallySmall || isFolded)
    }
}