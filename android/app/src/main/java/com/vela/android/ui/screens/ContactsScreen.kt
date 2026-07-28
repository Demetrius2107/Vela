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

data class Contact(val id: String, val name: String, val online: Boolean, val signature: String = "")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactsScreen(onBack: () -> Unit) {
    val contacts = remember {
        listOf(
            Contact("user1", "张三", true, "前端开发中"),
            Contact("user2", "李四", true, "后端架构"),
            Contact("user3", "王五", false, "产品经理"),
            Contact("user4", "赵六", true, "设计师"),
            Contact("user5", "陈七", false, "测试工程师")
        )
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("通讯录") },
            navigationIcon = { TextButton(onClick = onBack) { Text("←") } }
        )
        Divider()
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(contacts) { contact ->
                ContactItem(contact)
                Divider()
            }
        }
    }
}

@Composable
fun ContactItem(contact: Contact) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable { }.padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier.size(48.dp),
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.primaryContainer
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(contact.name.take(1), style = MaterialTheme.typography.titleLarge)
            }
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(contact.name, style = MaterialTheme.typography.titleSmall)
                if (contact.online) {
                    Spacer(Modifier.width(6.dp))
                    Surface(modifier = Modifier.size(8.dp), shape = MaterialTheme.shapes.extraSmall,
                        color = MaterialTheme.colorScheme.primary) {}
                }
            }
            Text(contact.signature, style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline)
        }
    }
}
