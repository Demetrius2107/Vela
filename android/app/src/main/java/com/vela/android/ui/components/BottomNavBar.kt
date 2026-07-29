package com.vela.android.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

data class BottomNavItem(
    val id: String,
    val label: String,
    val icon: String,
)

val bottomNavItems = listOf(
    BottomNavItem("chat", "会话", "💬"),
    BottomNavItem("contacts", "通讯录", "👥"),
    BottomNavItem("bot", "Bot", "🤖"),
    BottomNavItem("settings", "设置", "⚙️"),
)

@Composable
fun VelaBottomNavBar(
    selectedTab: String,
    onSelectTab: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        modifier = modifier.fillMaxWidth().height(64.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp
    ) {
        bottomNavItems.forEach { item ->
            NavigationBarItem(
                selected = selectedTab == item.id,
                onClick = { onSelectTab(item.id) },
                icon = {
                    Text(
                        item.icon,
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                label = {
                    Text(
                        item.label,
                        style = MaterialTheme.typography.labelSmall
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                )
            )
        }
    }
}
