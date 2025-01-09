package mikhail.shell.video.hosting.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

//private val DarkColorScheme = darkColorScheme(
//    primary = Color.Green,
//    secondary = PurpleGrey80,
//    tertiary = Pink80,
//    background = Color.Black
//)

private val LightColorScheme = lightColorScheme(
    primary = Blue700,
    onPrimary = White,

    secondary = Blue50,
    onSecondary = Black,

    secondaryContainer = White,
    onSecondaryContainer = Black,

    tertiary = Gray,
    onTertiary = Black,
    tertiaryContainer = LightGray,
    onTertiaryContainer = Black,

    background = White,
    onBackground = Black,

    surface = White,
    surfaceContainer = White,
    onSurface = Black,

    error = Red,
    onError = White
)

@Composable
fun VideoHostingTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
//        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
//            val context = LocalContext.current
//            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
//        }
        //darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}