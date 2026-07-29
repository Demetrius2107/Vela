package com.vela.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.vela.android.ui.theme.VelaTheme
import com.vela.android.ui.screens.*
import com.vela.android.ui.viewmodel.ChatViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            VelaTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AppNavigation()
                }
            }
        }
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val chatViewModel = remember { ChatViewModel() }
    var bottomBarTab by remember { mutableStateOf("chat") }

    // 底部导航栏可见的页面
    val showBottomBar = listOf("home", "contacts", "bot", "settings")

    NavHost(navController = navController, startDestination = "login") {
        composable("login") {
            LoginScreen(
                onNavigateToRegister = { navController.navigate("register") },
                onLoginSuccess = { navController.navigate("home") { popUpTo("login") { inclusive = true } } }
            )
        }
        composable("register") {
            RegisterScreen(
                onNavigateToLogin = { navController.popBackStack() },
                onRegisterSuccess = { navController.navigate("login") { popUpTo("register") { inclusive = true } } }
            )
        }
        composable("home") {
            val authViewModel = com.vela.android.ui.viewmodel.AuthViewModel(com.vela.android.VelaApp())
            val userId = authViewModel.getUserId()

            LaunchedEffect(userId) {
                if (userId.isNotEmpty()) chatViewModel.loadConversations(10000, userId)
            }

            HomeScreen(
                conversations = chatViewModel.conversations,
                onSelectConversation = { conv ->
                    chatViewModel.selectConversation(conv)
                    navController.navigate("chat/${conv.id}")
                },
                tab = bottomBarTab,
                onTabChange = { bottomBarTab = it }
            )
        }
        composable("chat/{convId}", arguments = listOf(navArgument("convId") { type = NavType.StringType })) {
            val name = chatViewModel.currentConversation?.name ?: "聊天"
            ChatScreen(
                title = name,
                messages = chatViewModel.messages,
                onSend = { /* TODO: send message */ },
                onBack = { navController.popBackStack() }
            )
        }
        composable("contacts") {
            ContactsScreen(onBack = { navController.popBackStack() })
        }
        composable("bot") {
            BotScreen(onBack = { navController.popBackStack() })
        }
        composable("settings") {
            SettingsScreen(onBack = { navController.popBackStack() })
        }
    }
}
