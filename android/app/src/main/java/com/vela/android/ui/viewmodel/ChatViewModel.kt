package com.vela.android.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.vela.android.network.RetrofitClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

data class Conversation(
    val id: String,
    val name: String,
    val lastMessage: String = "",
    val time: String = "",
    val unread: Int = 0,
    val online: Boolean = false,
    val isGroup: Boolean = false
)

data class ChatMessage(
    val id: String,
    val content: String,
    val isSelf: Boolean,
    val time: String
)

class ChatViewModel {

    private val api = RetrofitClient.api
    private val scope = CoroutineScope(Dispatchers.IO)

    var conversations by mutableStateOf<List<Conversation>>(emptyList())
    var messages by mutableStateOf<List<ChatMessage>>(emptyList())
    var currentConversation by mutableStateOf<Conversation?>(null)
    var loading by mutableStateOf(false)

    fun loadConversations(appId: Int, userId: String) {
        scope.launch {
            loading = true
            try {
                val resp = api.getFriends(appId, userId)
                if (resp.isOk && resp.data != null) {
                    conversations = resp.data.map { friend ->
                        Conversation(
                            id = friend.toId ?: "",
                            name = friend.nickName ?: friend.toId ?: "未知",
                            online = friend.status == 1,
                            lastMessage = friend.selfSignature ?: ""
                        )
                    }
                }
            } catch (e: Exception) {
                // fallback to empty
            } finally {
                loading = false
            }
        }
    }

    fun selectConversation(conv: Conversation) {
        currentConversation = conv
    }
}
