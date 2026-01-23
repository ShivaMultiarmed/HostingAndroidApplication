package mikhail.shell.video.hosting.presentation.utils

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import mikhail.shell.video.hosting.ui.theme.Black

fun Modifier.borderBottom(
    strokeWidth: Int = 3,
    color: Color = Black
): Modifier {
    return this.drawBehind {
        val y = size.height - strokeWidth / 2
        drawLine(
            strokeWidth = strokeWidth.toFloat(),
            color = color,
            start = Offset(0f, y),
            end = Offset(size.width, y)
        )
    }
}

fun Modifier.borderTop(
    strokeWidth: Int = 3,
    color: Color
): Modifier {
    return this.drawBehind {
        drawLine(
            strokeWidth = strokeWidth.toFloat(),
            color = color,
            start = Offset(0f, 0f),
            end = Offset(size.width, 0f)
        )
    }
}