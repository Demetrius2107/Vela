package com.vela.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.vela.android.ui.screens.LoginScreen
import com.vela.android.ui.screens.RegisterScreen
import com.vela.android.ui.screens.HomeScreen
import com.vela.android.ui.screens.ChatScreen
import com.vela.android.ui.screens.ContactsScreen
import com.vela.android.ui.screens.SettingsScreen
import com.vela.android.ui.viewmodel.ChatViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
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
                }
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
        composable("settings") {
            SettingsScreen(onBack = { navController.popBackStack() })
        }
    }
}
