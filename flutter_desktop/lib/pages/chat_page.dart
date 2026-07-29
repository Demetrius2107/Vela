import 'package:flutter/material.dart';
import '../models/models.dart';
import '../theme/theme.dart';

class ChatPage extends StatefulWidget {
  final Conversation conversation;
  final Color color;

  const ChatPage({super.key, required this.conversation, required this.color});

  @override
  State<ChatPage> createState() => _ChatPageState();
}

class _ChatPageState extends State<ChatPage> {
  final _msgCtrl = TextEditingController();
  final _messages = <ChatMessage>[
    ChatMessage(id: '1', content: '你好，最近怎么样？', isSelf: false, time: '10:00'),
    ChatMessage(id: '2', content: '挺好的，刚忙完一个项目', isSelf: true, time: '10:01'),
  ];

  @override
  void dispose() {
    _msgCtrl.dispose();
    super.dispose();
  }

  void _sendMessage() {
    final text = _msgCtrl.text.trim();
    if (text.isEmpty) return;
    setState(() {
      _messages.add(ChatMessage(
        id: DateTime.now().millisecondsSinceEpoch.toString(),
        content: text, isSelf: true,
        time: '${DateTime.now().hour}:${DateTime.now().minute.toString().padLeft(2, '0')}',
      ));
      _msgCtrl.clear();
    });
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Row(
          children: [
            CircleAvatar(
              radius: 16,
              backgroundColor: widget.color.withOpacity(0.15),
              child: Text(widget.conversation.name[0],
                  style: TextStyle(color: widget.color, fontWeight: FontWeight.bold)),
            ),
            const SizedBox(width: 8),
            Text(widget.conversation.name),
          ],
        ),
      ),
      body: Column(
        children: [
          // 消息区
          Expanded(
            child: Container(
              decoration: BoxDecoration(
                gradient: LinearGradient(
                  colors: [
                    widget.color.withOpacity(0.04),
                    const Color(0xFFF0F2F5),
                  ],
                  begin: Alignment.topCenter, end: Alignment.bottomCenter,
                ),
              ),
              child: ListView.builder(
                padding: const EdgeInsets.all(16),
                itemCount: _messages.length,
                itemBuilder: (context, i) {
                  final msg = _messages[i];
                  return _buildBubble(msg, context);
                },
              ),
            ),
          ),
          // 输入区
          Container(
            color: Colors.white,
            child: SafeArea(
              child: Padding(
                padding: const EdgeInsets.all(8),
                child: Row(
                  children: [
                    Expanded(
                      child: TextField(
                        controller: _msgCtrl,
                        decoration: InputDecoration(
                          hintText: '输入消息...',
                          filled: true,
                          fillColor: const Color(0xFFF7F8FA),
                          border: OutlineInputBorder(
                            borderRadius: BorderRadius.circular(12),
                            borderSide: const BorderSide(color: Color(0xFFEEF0F4)),
                          ),
                          contentPadding: const EdgeInsets.symmetric(horizontal: 12, vertical: 10),
                        ),
                        onSubmitted: (_) => _sendMessage(),
                      ),
                    ),
                    const SizedBox(width: 8),
                    ElevatedButton(
                      onPressed: _sendMessage,
                      style: ElevatedButton.styleFrom(
                        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
                        padding: const EdgeInsets.symmetric(horizontal: 20, vertical: 14),
                      ),
                      child: const Text('发送'),
                    ),
                  ],
                ),
              ),
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildBubble(ChatMessage msg, BuildContext context) {
    final align = msg.isSelf ? CrossAxisAlignment.end : CrossAxisAlignment.start;
    return Padding(
      padding: const EdgeInsets.only(bottom: 10),
      child: Column(
        crossAxisAlignment: align,
        children: [
          Container(
            constraints: BoxConstraints(maxWidth: MediaQuery.of(context).size.width * 0.7),
            padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 10),
            decoration: BoxDecoration(
              gradient: msg.isSelf
                  ? const LinearGradient(colors: [Color(0xFF4F6EF7), Color(0xFF7C3AED)])
                  : null,
              color: msg.isSelf ? null : Colors.white,
              borderRadius: msg.isSelf
                  ? const BorderRadius.only(
                      topLeft: Radius.circular(18), bottomLeft: Radius.circular(18),
                      bottomRight: Radius.circular(18), topRight: Radius.circular(4))
                  : const BorderRadius.only(
                      topRight: Radius.circular(18), bottomLeft: Radius.circular(18),
                      bottomRight: Radius.circular(18), topLeft: Radius.circular(4)),
              boxShadow: [
                BoxShadow(
                  color: msg.isSelf ? const Color(0xFF4F6EF7).withOpacity(0.2) : Colors.black.withOpacity(0.04),
                  blurRadius: 4, offset: const Offset(0, 2),
                ),
              ],
            ),
            child: Text(
              msg.content,
              style: TextStyle(
                color: msg.isSelf ? Colors.white : const Color(0xFF1A1A2E),
                fontSize: 15,
              ),
            ),
          ),
          Padding(
            padding: const EdgeInsets.only(top: 2, left: 4, right: 4),
            child: Text(msg.time, style: const TextStyle(fontSize: 11, color: velaTextSecondary)),
          ),
        ],
      ),
    );
  }
}
