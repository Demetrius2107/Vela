package com.vela.android.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vela.android.ui.components.VelaBottomNavBar
import com.vela.android.ui.theme.ConvColors
import com.vela.android.ui.viewmodel.Conversation

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    conversations: List<Conversation>,
    onSelectConversation: (Conversation) -> Unit,
    tab: String,
    onTabChange: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        // 标题栏
        TopAppBar(
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        modifier = Modifier.size(28.dp),
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.primary
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("V", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }
                    Spacer(Modifier.width(8.dp))
                    Text("Vela", fontWeight = FontWeight.Bold)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface,
                scrolledContainerColor = MaterialTheme.colorScheme.surface
            )
        )

        // 内容区（根据 tab 切换）
        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            when (tab) {
                "chat" -> ConversationList(conversations, onSelectConversation)
                "contacts" -> ContactsScreen(onBack = {})
                "bot" -> BotScreen(onBack = {})
                "settings" -> SettingsScreen(onBack = {})
            }
        }

        // 底部导航
        VelaBottomNavBar(selectedTab = tab, onSelectTab = onTabChange)
    }
}

@Composable
fun ConversationList(
    conversations: List<Conversation>,
    onSelectConversation: (Conversation) -> Unit
) {
    if (conversations.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("💬", fontSize = 48.sp)
                Spacer(Modifier.height(12.dp))
                Text("暂无会话", color = MaterialTheme.colorScheme.outline)
            }
        }
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        items(conversations) { conv ->
            val colorIdx = conv.id.hashCode().let { kotlin.math.abs(it) % ConvColors.size }
            val convColor = ConvColors[colorIdx]

            ConversationItem(conv = conv, color = convColor, onClick = { onSelectConversation(conv) })
        }
    }
}

@Composable
fun ConversationItem(conv: Conversation, color: Color, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).clickable(onClick = onClick),
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(12.dp),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(start = 4.dp, end = 16.dp, top = 12.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 左侧彩色边条
            Surface(
                modifier = Modifier.width(3.dp).height(40.dp),
                color = color,
                shape = RoundedCornerShape(2.dp)
            ) {}
            Spacer(Modifier.width(12.dp))

            // 头像
            Surface(
                modifier = Modifier.size(44.dp),
                shape = RoundedCornerShape(22.dp),
                color = color.copy(alpha = 0.15f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        conv.name.take(1),
                        color = color,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }
            }
            Spacer(Modifier.width(12.dp))

            // 文字内容
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        conv.name,
                        style = MaterialTheme.typography.titleSmall,
                        color = color,
                        fontWeight = FontWeight.SemiBold
                    )
                    if (conv.online) {
                        Spacer(Modifier.width(6.dp))
                        Surface(
                            modifier = Modifier.size(6.dp),
                            shape = MaterialTheme.shapes.extraSmall,
                            color = com.vela.android.ui.theme.VelaSuccess
                        ) {}
                    }
                    Spacer(Modifier.weight(1f))
                    Text(
                        conv.time,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
                Spacer(Modifier.height(3.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        conv.lastMessage,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.outline,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    if (conv.unread > 0) {
                        Spacer(Modifier.width(8.dp))
                        Badge(
                            containerColor = MaterialTheme.colorScheme.error
                        ) {
                            Text(
                                "${conv.unread}",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}
