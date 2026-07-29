import 'package:flutter/material.dart';

final Color velaPrimary = const Color(0xFF4F6EF7);
final Color velaPrimaryDark = const Color(0xFF3B57D9);
final Color velaPrimaryLight = const Color(0xFF6B86FA);
final Color velaSuccess = const Color(0xFF22C55E);
final Color velaWarning = const Color(0xFFF59E0B);
final Color velaError = const Color(0xFFEF4444);
final Color velaBg = const Color(0xFFF0F2F5);
final Color velaSurface = const Color(0xFFF8F9FF);
final Color velaTextPrimary = const Color(0xFF1A1A2E);
final Color velaTextSecondary = const Color(0xFF999999);
final Color velaDivider = const Color(0xFFE8E8E8);

final List<Color> convColors = [
  const Color(0xFF4F6EF7),
  const Color(0xFF22C55E),
  const Color(0xFFEF4444),
  const Color(0xFFF59E0B),
  const Color(0xFF8B5CF6),
  const Color(0xFF06B6D4),
  const Color(0xFFEC4899),
  const Color(0xFF14B8A6),
];

final ThemeData velaTheme = ThemeData(
  primaryColor: velaPrimary,
  colorScheme: ColorScheme.light(
    primary: velaPrimary,
    secondary: velaSuccess,
    error: velaError,
    surface: Colors.white,
    outline: velaTextSecondary,
  ),
  scaffoldBackgroundColor: velaBg,
  appBarTheme: const AppBarTheme(
    backgroundColor: Colors.white,
    foregroundColor: velaTextPrimary,
    elevation: 0,
    centerTitle: true,
  ),
  cardTheme: CardThemeData(
    elevation: 0,
    shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(14)),
    color: Colors.white,
  ),
  inputDecorationTheme: InputDecorationTheme(
    border: OutlineInputBorder(borderRadius: BorderRadius.circular(12)),
    filled: true,
    fillColor: Colors.white,
    contentPadding: const EdgeInsets.symmetric(horizontal: 16, vertical: 14),
  ),
  elevatedButtonTheme: ElevatedButtonThemeData(
    style: ElevatedButton.styleFrom(
      backgroundColor: velaPrimary,
      foregroundColor: Colors.white,
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(22)),
      padding: const EdgeInsets.symmetric(vertical: 14),
    ),
  ),
);
