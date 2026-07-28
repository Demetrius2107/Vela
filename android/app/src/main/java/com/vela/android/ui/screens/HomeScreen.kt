package com.vela.android.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vela.android.ui.viewmodel.Conversation

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    conversations: List<Conversation>,
    onSelectConversation: (Conversation) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Vela") },
            actions = {
                TextButton(onClick = { }) { Text("设置") }
            }
        )
        Divider()
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(conversations) { conv ->
                ConversationItem(conv, onClick = { onSelectConversation(conv) })
                Divider()
            }
        }
    }
}

@Composable
fun ConversationItem(conv: Conversation, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier.size(48.dp),
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.primaryContainer
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(conv.name.take(1), style = MaterialTheme.typography.titleLarge)
            }
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(conv.name, style = MaterialTheme.typography.titleSmall)
                if (conv.online) {
                    Spacer(Modifier.width(6.dp))
                    Surface(modifier = Modifier.size(8.dp), shape = MaterialTheme.shapes.extraSmall,
                        color = MaterialTheme.colorScheme.primary) {}
                }
                Spacer(Modifier.weight(1f))
                Text(conv.time, style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline)
            }
            Spacer(Modifier.height(2.dp))
            Row {
                Text(conv.lastMessage, style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.weight(1f))
                if (conv.unread > 0) {
                    Spacer(Modifier.width(8.dp))
                    Badge { Text("${conv.unread}") }
                }
            }
        }
    }
}
