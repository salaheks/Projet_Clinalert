import 'package:flutter/material.dart';

// Simple ThemeProvider to toggle between light and dark modes.
class ThemeProvider with ChangeNotifier {
  ThemeMode _themeMode = ThemeMode.light; // Default to white/light theme

  ThemeMode get themeMode => _themeMode;

  bool get isDarkMode => _themeMode == ThemeMode.dark;

  void toggleTheme() {
    _themeMode = isDarkMode ? ThemeMode.light : ThemeMode.dark;
    notifyListeners();
  }

  void setTheme(ThemeMode mode) {
    _themeMode = mode;
    notifyListeners();
  }
}


