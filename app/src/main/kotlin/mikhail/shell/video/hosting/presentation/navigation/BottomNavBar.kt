package mikhail.shell.video.hosting.presentation.navigation

import android.text.Html.ImageGetter
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Subscriptions
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Subscriptions
import androidx.compose.material.icons.sharp.Search
import androidx.compose.material.icons.twotone.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemColors
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

sealed class BottomNavItem(
    val route: Route,
    val title: String,
    val baseIcon: ImageVector,
    val selectedIcon: ImageVector
) {
    data object Subscriptions : BottomNavItem(
        Route.Subscriptions,
        "Подписки",
        Icons.Outlined.Subscriptions,
        Icons.Rounded.Subscriptions
    )

    data object Search : BottomNavItem(
        Route.Search,
        "Поиск",
        Icons.Outlined.Search,
        Icons.Rounded.Search
    )

    data object Profile : BottomNavItem(
        Route.Profile,
        "Профиль",
        Icons.Outlined.Person,
        Icons.Rounded.Person
    )
}

@Composable
fun BottomNavBar(
    onClick: (BottomNavItem) -> Unit
) {
    BottomNavBar(
        navItems = listOf(
            BottomNavItem.Subscriptions,
            BottomNavItem.Search,
            BottomNavItem.Profile
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
    NavigationBar(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp),
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
        colors = NavigationBarItemDefaults.colors(
            indicatorColor = Color.Transparent
        ),
        icon = {
            val imageVector = if (selected) navItem.selectedIcon else navItem.baseIcon
            Icon(
                imageVector = imageVector,
                contentDescription = navItem.title,
                modifier = Modifier.size(27.dp)
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
