import 'package:flutter/foundation.dart';
import '../models/models.dart';
import '../services/api_client.dart';

class AppState extends ChangeNotifier {
  final ApiClient _api = ApiClient();

  bool _isLoggedIn = false;
  bool get isLoggedIn => _isLoggedIn;

  String _userId = '';
  String get userId => _userId;

  String _token = '';
  String get token => _token;

  List<Conversation> _conversations = [];
  List<Conversation> get conversations => _conversations;

  bool _loading = false;
  bool get loading => _loading;

  void login(String userId, String token) {
    _userId = userId;
    _token = token;
    _api.setToken(token);
    _isLoggedIn = true;
    notifyListeners();
  }

  void logout() {
    _userId = '';
    _token = '';
    _isLoggedIn = false;
    _conversations = [];
    notifyListeners();
  }

  Future<void> loadConversations() async {
    if (_userId.isEmpty) return;
    _loading = true;
    notifyListeners();

    try {
      final friends = await _api.getFriends(_userId);
      _conversations = friends.asMap().entries.map((e) => Conversation(
        id: e.value.userId,
        name: e.value.nickName.isNotEmpty ? e.value.nickName : e.value.userId,
        lastMessage: e.value.signature,
        online: true,
        colorIndex: e.key % 8,
      )).toList();
    } catch (_) {}

    _loading = false;
    notifyListeners();
  }
}
