import 'dart:convert';
import 'package:http/http.dart' as http;
import '../models/models.dart';

class ApiClient {
  static const String baseUrl = 'http://localhost:8888';
  static final ApiClient _instance = ApiClient._();
  factory ApiClient() => _instance;
  ApiClient._();

  String _token = '';

  void setToken(String token) => _token = token;

  Map<String, String> get _headers => {
    'Content-Type': 'application/json',
    if (_token.isNotEmpty) 'token': _token,
  };

  Future<ApiResponse<T>> _request<T>({
    required String method,
    required String path,
    Map<String, String>? query,
    Object? body,
    T Function(dynamic json)? parser,
  }) async {
    final uri = Uri.parse('$baseUrl$path').replace(queryParameters: query);
    final http.Response response;

    switch (method) {
      case 'GET':
        response = await http.get(uri, headers: _headers);
      case 'POST':
        response = await http.post(uri, headers: _headers,
            body: body != null ? jsonEncode(body) : null);
      default:
        throw Exception('Unsupported method: $method');
    }

    if (response.statusCode != 200) {
      throw Exception('HTTP ${response.statusCode}');
    }

    final json = jsonDecode(response.body) as Map<String, dynamic>;
    final code = json['code'] as int;
    final msg = json['msg'] as String?;

    if (code != 200) throw Exception(msg ?? '请求失败');

    return ApiResponse<T>(
      code: code,
      msg: msg,
      data: parser != null ? parser(json['data']) : null,
    );
  }

  // 登录
  Future<String> login(String userId, String password) async {
    final resp = await _request<String>(
      method: 'POST', path: '/v1/user/login',
      body: {'userId': userId, 'password': password},
      parser: (d) => d as String,
    );
    return resp.data ?? '';
  }

  // 注册
  Future<void> register(String userId, String nickName, String password) async {
    await _request(
      method: 'POST', path: '/v1/user/register',
      body: {'userId': userId, 'nickName': nickName, 'password': password},
    );
  }

  // 好友列表
  Future<List<FriendData>> getFriends(String userId) async {
    final resp = await _request<List<FriendData>>(
      method: 'GET', path: '/v1/friend/getAllFriend',
      query: {'appId': '10000', 'fromId': userId},
      parser: (d) => (d as List).map((e) => FriendData(
        userId: e['toId'] ?? '', nickName: e['nickName'] ?? '',
        signature: e['selfSignature'] ?? '',
      )).toList(),
    );
    return resp.data ?? [];
  }

  // Bot 市场
  Future<List<BotData>> getBotMarket() async {
    final resp = await _request<List<BotData>>(
      method: 'GET', path: '/v1/bot/market/list',
      query: {'appId': '10000'},
      parser: (d) => (d as List).map((e) => BotData(
        botId: e['botId'] ?? '', botName: e['botName'] ?? '',
        description: e['description'] ?? '', category: e['category'] ?? '',
      )).toList(),
    );
    return resp.data ?? [];
  }

  // 用户配置
  Future<Map<String, String>> getUserConfig(String userId) async {
    final resp = await _request<Map<String, String>>(
      method: 'GET', path: '/v1/user/config/get',
      query: {'appId': '10000', 'userId': userId, 'clientType': 'desktop'},
      parser: (d) => Map<String, String>.from(d as Map),
    );
    return resp.data ?? {};
  }

  Future<void> saveUserConfig(String userId, List<Map<String, String>> configs) async {
    await _request(
      method: 'POST', path: '/v1/user/config/save',
      query: {'appId': '10000', 'userId': userId, 'clientType': 'desktop'},
      body: configs,
    );
  }
}
