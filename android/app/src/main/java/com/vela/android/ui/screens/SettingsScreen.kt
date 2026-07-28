package com.vela.android.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onBack: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("设置") },
            navigationIcon = { TextButton(onClick = onBack) { Text("←") } }
        )
        Divider()
        Column(modifier = Modifier.padding(16.dp)) {
            SettingItem("账号信息", "Vela 用户")
            Divider()
            SettingItem("通知设置", "已开启")
            Divider()
            SettingItem("关于", "v1.0")
            Divider()
            Spacer(Modifier.weight(1f))
            Button(
                onClick = { /* logout */ },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.error)
            ) { Text("退出登录") }
        }
    }
}

@Composable
fun SettingItem(title: String, subtitle: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, style = MaterialTheme.typography.bodyLarge)
        Text(subtitle, style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.outline)
    }
}
