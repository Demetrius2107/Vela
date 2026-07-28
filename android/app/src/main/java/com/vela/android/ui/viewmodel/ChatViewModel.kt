package com.vela.android.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

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

    var conversations by mutableStateOf(listOf(
        Conversation("user1", "张三", "明天见", "10:30", 3, true),
        Conversation("group1", "项目团队", "收到", "09:15", 0, false, true),
        Conversation("user2", "李四", "文件已发", "昨天", 1, true)
    ))

    var messages by mutableStateOf(listOf(
        ChatMessage("1", "你好", false, "10:00"),
        ChatMessage("2", "你好，有事吗", true, "10:01"),
        ChatMessage("3", "明天开会别忘了", false, "10:05"),
        ChatMessage("4", "好的收到", true, "10:06")
    ))

    var currentConversation by mutableStateOf<Conversation?>(null)

    fun selectConversation(conv: Conversation) {
        currentConversation = conv
    }
}
