import 'package:flutter/material.dart';
import '../theme/theme.dart';
import '../services/api_client.dart';
import '../providers/app_state.dart';
import 'package:provider/provider.dart';

class ContactsPage extends StatefulWidget {
  const ContactsPage({super.key});
  @override
  State<ContactsPage> createState() => _ContactsPageState();
}

class _ContactsPageState extends State<ContactsPage> {
  List<Map<String, dynamic>> _contacts = [];
  bool _loading = false;

  @override
  void initState() {
    super.initState();
    _load();
  }

  Future<void> _load() async {
    final userId = context.read<AppState>().userId;
    if (userId.isEmpty) return;
    setState(() => _loading = true);
    try {
      final friends = await ApiClient().getFriends(userId);
      setState(() => _contacts = friends.asMap().entries.map((e) => {
        'id': e.value.userId,
        'name': e.value.nickName.isNotEmpty ? e.value.nickName : e.value.userId,
        'sig': e.value.signature,
        'colorIdx': e.key % 8,
      }).toList());
    } catch (_) {}
    setState(() => _loading = false);
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('通讯录')),
      body: _loading
          ? const Center(child: CircularProgressIndicator())
          : _contacts.isEmpty
              ? const Center(child: Column(
                  mainAxisAlignment: MainAxisAlignment.center,
                  children: [Text('👥', style: TextStyle(fontSize: 48)), SizedBox(height: 12), Text('暂无好友', style: TextStyle(color: velaTextSecondary))],
                ))
              : ListView.builder(
                  padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
                  itemCount: _contacts.length,
                  itemBuilder: (context, i) {
                    final c = _contacts[i];
                    final color = convColors[c['colorIdx'] as int];
                    return Card(
                      child: ListTile(
                        leading: CircleAvatar(
                          radius: 22,
                          backgroundColor: color.withOpacity(0.12),
                          child: Text((c['name'] as String)[0], style: TextStyle(color: color, fontWeight: FontWeight.bold)),
                        ),
                        title: Text(c['name'] as String, style: const TextStyle(fontWeight: FontWeight.w600)),
                        subtitle: c['sig'] != null && (c['sig'] as String).isNotEmpty
                            ? Text(c['sig'] as String, maxLines: 1, overflow: TextOverflow.ellipsis, style: const TextStyle(color: velaTextSecondary))
                            : null,
                      ),
                    );
                  },
                ),
    );
  }
}
