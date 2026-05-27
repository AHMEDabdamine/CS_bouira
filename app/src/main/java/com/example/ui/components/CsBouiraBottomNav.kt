package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

enum class NavItem(
    val label: String,
    val activeIcon: ImageVector,
    val inactiveIcon: ImageVector
) {
    Home("Accueil", Icons.Filled.Home, Icons.Outlined.Home),
    Search("", Icons.Filled.Search, Icons.Filled.Search),
    Favoris("Favoris", Icons.Filled.Bookmark, Icons.Filled.Bookmark)
}

@Composable
fun CsBouiraBottomNav(
    selectedItem: NavItem,
    onItemSelected: (NavItem) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        HorizontalDivider(color = Border, thickness = 0.5.dp)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(SurfaceElevated)
                .padding(horizontal = 8.dp, vertical = 4.dp)
                .height(64.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            NavItemButton(
                item = NavItem.Home,
                selected = selectedItem == NavItem.Home,
                onClick = { onItemSelected(NavItem.Home) },
                modifier = Modifier.weight(0.5f)
            )

            Spacer(modifier = Modifier.weight(0.3f))

            Box(
                modifier = Modifier
                    .offset(y = (-12).dp)
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(Primary)
                    .clickable { onItemSelected(NavItem.Search) },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Search,
                    contentDescription = "Rechercher",
                    tint = OnPrimary,
                    modifier = Modifier.size(26.dp)
                )
            }

            Spacer(modifier = Modifier.weight(0.3f))

            NavItemButton(
                item = NavItem.Favoris,
                selected = selectedItem == NavItem.Favoris,
                onClick = { onItemSelected(NavItem.Favoris) },
                modifier = Modifier.weight(0.5f)
            )
        }
    }
}

@Composable
private fun RowScope.NavItemButton(
    item: NavItem,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clickable(onClick = onClick)
            .padding(vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = if (selected) item.activeIcon else item.inactiveIcon,
            contentDescription = item.label,
            tint = if (selected) Primary else TextSecondary,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = item.label,
            color = if (selected) Primary else TextSecondary,
            fontSize = 10.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}
