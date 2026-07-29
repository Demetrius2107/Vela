import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'providers/app_state.dart';
import 'theme/theme.dart';
import 'pages/login_page.dart';
import 'pages/main_page.dart';

void main() {
  runApp(
    ChangeNotifierProvider(
      create: (_) => AppState(),
      child: const VelaApp(),
    ),
  );
}

class VelaApp extends StatelessWidget {
  const VelaApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Vela IM',
      theme: velaTheme,
      debugShowCheckedModeBanner: false,
      home: Consumer<AppState>(
        builder: (context, state, _) {
          return state.isLoggedIn ? const MainPage() : const LoginPage();
        },
      ),
    );
  }
}
