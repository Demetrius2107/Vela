import 'package:flutter/material.dart';
import '../theme/theme.dart';
import '../services/api_client.dart';

class BotMarketPage extends StatefulWidget {
  const BotMarketPage({super.key});
  @override
  State<BotMarketPage> createState() => _BotMarketPageState();
}

class _BotMarketPageState extends State<BotMarketPage> {
  List<Map<String, dynamic>> _bots = [];
  final Set<String> _installedIds = {};
  bool _loading = false;

  @override
  void initState() {
    super.initState();
    _load();
  }

  Future<void> _load() async {
    setState(() => _loading = true);
    try {
      final bots = await ApiClient().getBotMarket();
      setState(() => _bots = bots.map((b) => {
        'botId': b.botId, 'botName': b.botName, 'desc': b.description, 'cat': b.category,
      }).toList());
    } catch (_) {}
    setState(() => _loading = false);
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Bot 市场')),
      body: _loading
          ? const Center(child: CircularProgressIndicator())
          : _bots.isEmpty
              ? const Center(child: Column(
                  mainAxisAlignment: MainAxisAlignment.center,
                  children: [Text('🤖', style: TextStyle(fontSize: 48)), SizedBox(height: 12), Text('暂无可用 Bot', style: TextStyle(color: velaTextSecondary))],
                ))
              : ListView.builder(
                  padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
                  itemCount: _bots.length,
                  itemBuilder: (context, i) {
                    final bot = _bots[i];
                    final installed = _installedIds.contains(bot['botId']);
                    return Card(
                      child: ListTile(
                        leading: Container(
                          width: 48, height: 48,
                          decoration: BoxDecoration(
                            color: const Color(0xFF722ED1).withOpacity(0.1),
                            borderRadius: BorderRadius.circular(14),
                          ),
                          child: const Center(child: Text('🤖', style: TextStyle(fontSize: 24))),
                        ),
                        title: Row(
                          children: [
                            Text(bot['botName'] as String, style: const TextStyle(fontWeight: FontWeight.w600)),
                            if ((bot['cat'] as String).isNotEmpty) ...[
                              const SizedBox(width: 6),
                              Container(
                                padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 2),
                                decoration: BoxDecoration(
                                  color: velaPrimary.withOpacity(0.1),
                                  borderRadius: BorderRadius.circular(6),
                                ),
                                child: Text(bot['cat'] as String, style: const TextStyle(fontSize: 10, color: velaPrimary)),
                              ),
                            ],
                          ],
                        ),
                        subtitle: Text(bot['desc'] as String, maxLines: 1, overflow: TextOverflow.ellipsis, style: const TextStyle(color: velaTextSecondary)),
                        trailing: TextButton(
                          onPressed: installed ? null : () => setState(() => _installedIds.add(bot['botId'] as String)),
                          child: Text(installed ? '已安装' : '安装', style: TextStyle(color: installed ? velaTextSecondary : velaPrimary)),
                        ),
                      ),
                    );
                  },
                ),
    );
  }
}
