import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../providers/app_state.dart';
import '../theme/theme.dart';
import 'chat_page.dart';

class MainPage extends StatefulWidget {
  const MainPage({super.key});
  @override
  State<MainPage> createState() => _MainPageState();
}

class _MainPageState extends State<MainPage> {
  int _selectedIndex = 0;

  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) {
      context.read<AppState>().loadConversations();
    });
  }

  @override
  Widget build(BuildContext context) {
    final pages = [
      _buildConversationList(context),
      const Center(child: Text('通讯录', style: TextStyle(fontSize: 18))),
      const Center(child: Text('Bot 市场', style: TextStyle(fontSize: 18))),
      _buildSettings(context),
    ];

    return Scaffold(
      body: pages[_selectedIndex],
      bottomNavigationBar: NavigationBar(
        selectedIndex: _selectedIndex,
        onDestinationSelected: (i) => setState(() => _selectedIndex = i),
        backgroundColor: Colors.white,
        indicatorColor: velaPrimary.withOpacity(0.12),
        destinations: const [
          NavigationDestination(icon: Icon(Icons.chat_bubble_outline), selectedIcon: Icon(Icons.chat_bubble, color: velaPrimary), label: '会话'),
          NavigationDestination(icon: Icon(Icons.people_outline), selectedIcon: Icon(Icons.people, color: velaPrimary), label: '通讯录'),
          NavigationDestination(icon: Icon(Icons.smart_toy_outlined), selectedIcon: Icon(Icons.smart_toy, color: velaPrimary), label: 'Bot'),
          NavigationDestination(icon: Icon(Icons.settings_outlined), selectedIcon: Icon(Icons.settings, color: velaPrimary), label: '设置'),
        ],
      ),
    );
  }

  Widget _buildConversationList(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Row(
          children: [
            Container(
              width: 28, height: 28,
              decoration: BoxDecoration(
                gradient: const LinearGradient(colors: [Color(0xFF4F6EF7), Color(0xFF7C3AED)]),
                borderRadius: BorderRadius.circular(8),
              ),
              child: const Center(child: Text('V', style: TextStyle(color: Colors.white, fontWeight: FontWeight.bold, fontSize: 14))),
            ),
            const SizedBox(width: 8),
            const Text('Vela', style: TextStyle(fontWeight: FontWeight.bold)),
          ],
        ),
        actions: [
          IconButton(
            icon: const Icon(Icons.logout, color: velaTextSecondary),
            onPressed: () => context.read<AppState>().logout(),
          ),
        ],
      ),
      body: Consumer<AppState>(
        builder: (context, state, _) {
          if (state.loading) return const Center(child: CircularProgressIndicator());
          if (state.conversations.isEmpty) {
            return const Center(
              child: Column(
                mainAxisAlignment: MainAxisAlignment.center,
                children: [
                  Text('💬', style: TextStyle(fontSize: 48)),
                  SizedBox(height: 12),
                  Text('暂无会话', style: TextStyle(color: velaTextSecondary)),
                ],
              ),
            );
          }
          return ListView.separated(
            padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 8),
            itemCount: state.conversations.length,
            separatorBuilder: (_, __) => const SizedBox(height: 4),
            itemBuilder: (context, i) {
              final conv = state.conversations[i];
              final color = convColors[conv.colorIndex];
              return Card(
                child: ListTile(
                  contentPadding: const EdgeInsets.only(left: 4, right: 16, top: 8, bottom: 8),
                  leading: Row(
                    mainAxisSize: MainAxisSize.min,
                    children: [
                      Container(width: 3, height: 40, color: color, margin: const EdgeInsets.only(right: 12)),
                      CircleAvatar(
                        radius: 22,
                        backgroundColor: color.withOpacity(0.12),
                        child: Text(conv.name[0], style: TextStyle(color: color, fontWeight: FontWeight.bold, fontSize: 16)),
                      ),
                    ],
                  ),
                  title: Text(conv.name, style: TextStyle(color: color, fontWeight: FontWeight.w600)),
                  subtitle: Text(conv.lastMessage, maxLines: 1, overflow: TextOverflow.ellipsis, style: const TextStyle(color: velaTextSecondary)),
                  trailing: conv.online ? Container(width: 8, height: 8, decoration: const BoxDecoration(color: velaSuccess, shape: BoxShape.circle)) : null,
                  onTap: () => Navigator.push(context, MaterialPageRoute(builder: (_) => ChatPage(conversation: conv, color: color))),
                ),
              );
            },
          );
        },
      ),
    );
  }

  Widget _buildSettings(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('设置')),
      body: ListView(
        padding: const EdgeInsets.all(16),
        children: [
          Card(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                const Padding(padding: EdgeInsets.all(16), child: Text('🎨 显示设置', style: TextStyle(fontWeight: FontWeight.w600, fontSize: 15))),
                SwitchListTile(title: const Text('紧凑模式'), subtitle: const Text('减少聊天列表间距', style: TextStyle(fontSize: 12, color: velaTextSecondary)), value: false, onChanged: (_) {}),
              ],
            ),
          ),
          const SizedBox(height: 12),
          Card(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                const Padding(padding: EdgeInsets.all(16), child: Text('🔔 通知设置', style: TextStyle(fontWeight: FontWeight.w600, fontSize: 15))),
                SwitchListTile(title: const Text('消息通知'), subtitle: const Text('新消息弹出通知', style: TextStyle(fontSize: 12, color: velaTextSecondary)), value: true, onChanged: (_) {}),
                SwitchListTile(title: const Text('提示音'), subtitle: const Text('新消息播放提示音', style: TextStyle(fontSize: 12, color: velaTextSecondary)), value: true, onChanged: (_) {}),
                SwitchListTile(title: const Text('通知预览'), subtitle: const Text('通知栏显示消息内容', style: TextStyle(fontSize: 12, color: velaTextSecondary)), value: true, onChanged: (_) {}),
              ],
            ),
          ),
          const SizedBox(height: 12),
          Card(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                const Padding(padding: EdgeInsets.all(16), child: Text('🛡️ 账号', style: TextStyle(fontWeight: FontWeight.w600, fontSize: 15))),
                ListTile(title: const Text('当前账号'), trailing: Text(context.read<AppState>().userId, style: const TextStyle(color: velaTextSecondary))),
              ],
            ),
          ),
        ],
      ),
    );
  }
}
