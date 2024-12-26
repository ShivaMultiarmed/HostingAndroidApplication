package mikhail.shell.video.hosting.presentation.navigation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Subscriptions
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavItem(
    val route: Route,
    val title: String,
    val icon: ImageVector
) {
   data object Subscriptions: BottomNavItem(Route.Subscriptions, "Подписки", Icons.Rounded.Subscriptions)
   data object Search: BottomNavItem(Route.Search, "Поиск", Icons.Rounded.Search)
   data object Profile: BottomNavItem(Route.Profile, "Профиль", Icons.Rounded.Person)
}

@Composable
fun BottomNavBar(
    onClick: (BottomNavItem) -> Unit
) {
    BottomNavBar(
        navItems = listOf(BottomNavItem.Subscriptions, BottomNavItem.Search, BottomNavItem.Profile),
        onClick = onClick
    )
}

@Composable
fun BottomNavBar(
    navItems: List<BottomNavItem>,
    onClick: (BottomNavItem) -> Unit
) {
    var selectedItemNumber by rememberSaveable { mutableIntStateOf(0) }
    NavigationBar(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            navItems.forEachIndexed { i, it ->
                BottomNavBarItem(
                    selected = selectedItemNumber == i,
                    navItem = it,
                    onClick = {
                        selectedItemNumber = i
                        onClick(navItems[selectedItemNumber])
                    },
                )
            }
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
        icon = {
            Column {
                Icon(
                    imageVector = navItem.icon,
                    contentDescription = navItem.title
                )
                if (selected) {
                    Text(
                        text = navItem.title
                    )
                }
            }
        }
    )
}
