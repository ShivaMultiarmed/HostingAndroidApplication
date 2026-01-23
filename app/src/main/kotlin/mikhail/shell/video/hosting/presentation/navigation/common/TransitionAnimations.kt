package mikhail.shell.video.hosting.presentation.navigation.common

import androidx.compose.animation.ContentTransform
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.ui.unit.IntOffset

object RootAnimations {
    val enteringAnimation = ContentTransform(
        EnterTransition.None,
        ExitTransition.None
    )
    val leavingAnimation = ContentTransform(
        EnterTransition.None,
        ExitTransition.None
    )
}

object SubGraphAnimations {
    val defaultSpec = spring<IntOffset>(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessMedium
    )
    val enteringAnimation = slideInHorizontally(
        initialOffsetX = { it },
        animationSpec = defaultSpec
    ) togetherWith slideOutHorizontally(
        targetOffsetX = { -it },
        animationSpec = defaultSpec
    )
    val leavingAnimation = slideInHorizontally(
        initialOffsetX = { -it },
        animationSpec = defaultSpec
    ) togetherWith slideOutHorizontally(
        targetOffsetX = { it },
        animationSpec = defaultSpec
    )
}