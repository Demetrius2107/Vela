import 'package:flutter/material.dart';
import '../services/api_client.dart';
import '../theme/theme.dart';

class RegisterPage extends StatefulWidget {
  const RegisterPage({super.key});
  @override
  State<RegisterPage> createState() => _RegisterPageState();
}

class _RegisterPageState extends State<RegisterPage> {
  final _userIdCtrl = TextEditingController();
  final _nickNameCtrl = TextEditingController();
  final _passwordCtrl = TextEditingController();
  bool _loading = false;
  String? _error;
  String? _success;

  @override
  void dispose() {
    _userIdCtrl.dispose(); _nickNameCtrl.dispose(); _passwordCtrl.dispose();
    super.dispose();
  }

  Future<void> _handleRegister() async {
    final id = _userIdCtrl.text.trim();
    final name = _nickNameCtrl.text.trim();
    final pwd = _passwordCtrl.text.trim();
    if (id.isEmpty || name.isEmpty || pwd.length < 6) {
      setState(() => _error = '请填写完整信息，密码至少6位');
      return;
    }
    setState(() { _loading = true; _error = null; _success = null; });
    try {
      await ApiClient().register(id, name, pwd);
      setState(() => _success = '注册成功！');
      await Future.delayed(const Duration(seconds: 1));
      if (mounted) Navigator.pop(context);
    } catch (e) {
      setState(() => _error = e.toString());
    }
    if (mounted) setState(() => _loading = false);
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: Container(
        width: double.infinity,
        decoration: const BoxDecoration(
          gradient: LinearGradient(
            colors: [Color(0xFF4F6EF7), Color(0xFF7C3AED)],
            begin: Alignment.topLeft, end: Alignment.bottomRight,
          ),
        ),
        child: SafeArea(
          child: SingleChildScrollView(
            padding: const EdgeInsets.all(32),
            child: Column(
              children: [
                const SizedBox(height: 60),
                const Text('创建账号', style: TextStyle(fontSize: 28, fontWeight: FontWeight.bold, color: Colors.white)),
                const SizedBox(height: 8),
                const Text('加入 Vela IM', style: TextStyle(fontSize: 15, color: Colors.white70)),
                const SizedBox(height: 40),
                TextField(controller: _userIdCtrl, decoration: const InputDecoration(hintText: '用户ID', filled: true, fillColor: Colors.white)),
                const SizedBox(height: 16),
                TextField(controller: _nickNameCtrl, decoration: const InputDecoration(hintText: '昵称', filled: true, fillColor: Colors.white)),
                const SizedBox(height: 16),
                TextField(controller: _passwordCtrl, obscureText: true, decoration: const InputDecoration(hintText: '密码（至少6位）', filled: true, fillColor: Colors.white)),
                if (_error != null) Padding(padding: const EdgeInsets.only(top: 8), child: Text(_error!, style: const TextStyle(color: Colors.white, fontSize: 13))),
                if (_success != null) Padding(padding: const EdgeInsets.only(top: 8), child: Text(_success!, style: const TextStyle(color: Colors.greenAccent, fontSize: 13))),
                const SizedBox(height: 24),
                SizedBox(
                  width: double.infinity, height: 48,
                  child: ElevatedButton(
                    onPressed: _loading ? null : _handleRegister,
                    style: ElevatedButton.styleFrom(backgroundColor: Colors.white, foregroundColor: velaPrimary, shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(24))),
                    child: Text(_loading ? '注册中...' : '注 册', style: const TextStyle(fontSize: 17, fontWeight: FontWeight.w600)),
                  ),
                ),
                const SizedBox(height: 16),
                TextButton(onPressed: () => Navigator.pop(context), child: const Text('已有账号？去登录', style: TextStyle(color: Colors.white70))),
              ],
            ),
          ),
        ),
      ),
    );
  }
}
