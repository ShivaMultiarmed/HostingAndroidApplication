package mikhail.shell.video.hosting.presentation.navigation.common

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Dataset
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Subscriptions
import androidx.compose.material.icons.rounded.Dataset
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Subscriptions
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import mikhail.shell.video.hosting.R
import mikhail.shell.video.hosting.presentation.utils.borderTop

data class BottomNavItem(
    val route: Route,
    val title: String,
    val baseIcon: ImageVector,
    val selectedIcon: ImageVector
)

@Composable
fun BottomNavBar(
    onClick: (BottomNavItem) -> Unit,
    userId: Long
) {
    BottomNavBar(
        navItems = listOf(
            BottomNavItem(
                Route.Video.Recommendations,
                stringResource(R.string.recommendations_title),
                Icons.Outlined.Dataset,
                Icons.Rounded.Dataset
            ),
            BottomNavItem(
                Route.User.Subscriptions,
                stringResource(R.string.nav_subscribers_item_label),
                Icons.Outlined.Subscriptions,
                Icons.Rounded.Subscriptions
            ),
            BottomNavItem(
                Route.Video.Search,
                stringResource(R.string.nav_search_item_label),
                Icons.Outlined.Search,
                Icons.Rounded.Search
            ),
            BottomNavItem(
                Route.User.Profile(userId),
                stringResource(R.string.nav_profile_item_label),
                Icons.Outlined.Person,
                Icons.Rounded.Person
            )
        ),
        onClick = onClick,
    )
}

@Composable
fun BottomNavBar(
    navItems: List<BottomNavItem>,
    onClick: (BottomNavItem) -> Unit
) {
    var selectedItemNumber by rememberSaveable { mutableIntStateOf(0) }
    Row(
        modifier = Modifier
            .borderTop(
                color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.4f),
                strokeWidth = 3
            )
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(bottom = 10.dp),
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        navItems.forEachIndexed { i, it ->
            BottomNavBarItem(
                modifier = Modifier.width(90.dp),
                selected = selectedItemNumber == i,
                navItem = it,
                onClick = {
                    selectedItemNumber = i
                    onClick(navItems[selectedItemNumber])
                }
            )
        }
    }
}

@Composable
fun RowScope.BottomNavBarItem(
    modifier: Modifier = Modifier,
    navItem: BottomNavItem,
    selected: Boolean,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val indication = ripple(
        radius = 40.dp,
        color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.3f)
    )
    Column(
        modifier = modifier
            .clickable(
                onClick = onClick,
                interactionSource = interactionSource,
                indication = indication
            ),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val imageVector = if (selected) navItem.selectedIcon else navItem.baseIcon
        Icon(
            modifier = Modifier
                .padding(top = 5.dp)
                .size(23.dp),
            imageVector = imageVector,
            contentDescription = navItem.title,
            tint = MaterialTheme.colorScheme.onSurface
        )
        Text(
            modifier = Modifier.padding(bottom = 5.dp),
            text = navItem.title,
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 9.sp
        )
    }
}
