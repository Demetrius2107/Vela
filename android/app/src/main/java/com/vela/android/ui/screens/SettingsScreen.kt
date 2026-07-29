package com.vela.android.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vela.android.ui.theme.VelaError
import com.vela.android.ui.theme.VelaPrimary
import com.vela.android.ui.theme.VelaSuccess

data class SwitchSetting(
    val key: String,
    val label: String,
    val desc: String,
    var enabled: Boolean
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onBack: () -> Unit) {
    val scrollState = rememberScrollState()

    // 设置状态
    var notifyEnabled by remember { mutableStateOf(true) }
    var notifySound by remember { mutableStateOf(true) }
    var notifyPreview by remember { mutableStateOf(true) }
    var privacySearchable by remember { mutableStateOf(true) }
    var compactMode by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        TopAppBar(
            title = { Text("设置", fontWeight = FontWeight.SemiBold) },
            navigationIcon = {
                TextButton(onClick = onBack) {
                    Text("←", fontSize = 20.sp, color = MaterialTheme.colorScheme.primary)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
        )

        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(scrollState).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // ===== 显示设置 =====
            SectionCard(title = "🎨 显示设置") {
                SettingSwitch("紧凑模式", "减少聊天列表的间距", compactMode, { compactMode = it })
            }

            // ===== 通知设置 =====
            SectionCard(title = "🔔 通知设置") {
                SettingSwitch("消息通知", "收到新消息时弹出通知", notifyEnabled, { notifyEnabled = it })
                SettingSwitch("提示音", "新消息到达时播放提示音", notifySound, { notifySound = it })
                SettingSwitch("通知预览", "通知栏显示消息内容", notifyPreview, { notifyPreview = it })
            }

            // ===== 隐私设置 =====
            SectionCard(title = "🔒 隐私设置") {
                SettingSwitch("允许被搜索", "其他用户可以通过ID找到你", privacySearchable, { privacySearchable = it })
            }

            // ===== 账号 =====
            SectionCard(title = "🛡️ 账号") {
                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("当前账号", fontWeight = FontWeight.Medium, fontSize = 14.sp, color = Color(0xFF1A1A2E))
                        Text("已登录", fontSize = 12.sp, color = MaterialTheme.colorScheme.outline)
                    }
                }
            }

            // ===== 关于 =====
            SectionCard(title = "ℹ️ 关于") {
                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("应用名称", fontWeight = FontWeight.Medium, fontSize = 14.sp, color = Color(0xFF1A1A2E))
                        Text("Vela IM", fontSize = 12.sp, color = MaterialTheme.colorScheme.outline)
                    }
                }
                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("版本", fontWeight = FontWeight.Medium, fontSize = 14.sp, color = Color(0xFF1A1A2E))
                        Text("1.0", fontSize = 12.sp, color = MaterialTheme.colorScheme.outline)
                    }
                }
            }

            Spacer(Modifier.height(8.dp))
            Button(
                onClick = { },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = VelaError)
            ) {
                Text("退出登录", fontWeight = FontWeight.Medium)
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
fun SectionCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = Color(0xFF1A1A2E))
            Spacer(Modifier.height(8.dp))
            content()
        }
    }
}

@Composable
fun SettingSwitch(label: String, desc: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(label, fontWeight = FontWeight.Medium, fontSize = 14.sp, color = Color(0xFF1A1A2E))
            Text(desc, fontSize = 12.sp, color = MaterialTheme.colorScheme.outline)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedTrackColor = VelaPrimary,
                checkedThumbColor = Color.White
            )
        )
    }
}
