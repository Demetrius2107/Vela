class Conversation {
  final String id;
  final String name;
  final String lastMessage;
  final String time;
  final int unread;
  final bool online;
  final bool isGroup;
  final int colorIndex;

  Conversation({
    required this.id,
    required this.name,
    this.lastMessage = '',
    this.time = '',
    this.unread = 0,
    this.online = false,
    this.isGroup = false,
    this.colorIndex = 0,
  });
}

class ChatMessage {
  final String id;
  final String content;
  final bool isSelf;
  final String time;

  ChatMessage({
    required this.id,
    required this.content,
    required this.isSelf,
    this.time = '',
  });
}

class BotData {
  final String botId;
  final String botName;
  final String description;
  final String category;

  BotData({
    required this.botId,
    required this.botName,
    this.description = '',
    this.category = '',
  });
}

class FriendData {
  final String userId;
  final String nickName;
  final String signature;

  FriendData({
    required this.userId,
    this.nickName = '',
    this.signature = '',
  });
}

// API 响应
class ApiResponse<T> {
  final int code;
  final String? msg;
  final T? data;

  bool get isOk => code == 200;

  ApiResponse({required this.code, this.msg, this.data});
}
