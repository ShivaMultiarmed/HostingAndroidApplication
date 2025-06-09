package mikhail.shell.video.hosting.presentation.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Subscriptions
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Subscriptions
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
                Route.User.Subscriptions,
                stringResource(R.string.nav_subscribers_item_label),
                Icons.Outlined.Subscriptions,
                Icons.Rounded.Subscriptions
            ),
            BottomNavItem(
                Route.Search,
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
    var selectedItemNumber by rememberSaveable { mutableIntStateOf(1) }
    NavigationBar(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .borderTop(
                color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.4f),
                strokeWidth = 3
            ),
        contentColor = MaterialTheme.colorScheme.onSurface
    ) {
        navItems.forEachIndexed { i, it ->
            BottomNavBarItem(
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
    navItem: BottomNavItem,
    selected: Boolean,
    onClick: () -> Unit
) {
    NavigationBarItem(
        selected = selected,
        onClick = onClick,
        colors = NavigationBarItemDefaults.colors(
            indicatorColor = Color.Transparent
        ),
        icon = {
            val imageVector = if (selected) navItem.selectedIcon else navItem.baseIcon
            Icon(
                imageVector = imageVector,
                contentDescription = navItem.title,
                modifier = Modifier.size(26.dp),
                tint = MaterialTheme.colorScheme.onSurface
            )
        },
        label = {
            Text(
                text = navItem.title,
                fontSize = 9.sp
            )
        }
    )
}
