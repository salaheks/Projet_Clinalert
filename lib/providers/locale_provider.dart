import 'package:flutter/material.dart';
import 'package:shared_preferences/shared_preferences.dart';

/// Locale provider for managing app language settings
class LocaleProvider with ChangeNotifier {
  Locale _locale = const Locale('fr'); // Default to French
  
  Locale get locale => _locale;
  
  /// Get language display name
  String get languageName {
    switch (_locale.languageCode) {
      case 'ar':
        return 'العربية';
      case 'fr':
        return 'Français';
      case 'en':
        return 'English';
      default:
        return 'Français';
    }
  }
  
  /// Check if current locale is RTL
  bool get isRTL => _locale.languageCode == 'ar';
  
  /// Available locales
  static const List<Locale> supportedLocales = [
    Locale('fr'), // French
    Locale('en'), // English
    Locale('ar'), // Arabic
  ];
  
  /// Initialize locale from saved preferences
  Future<void> init() async {
    final prefs = await SharedPreferences.getInstance();
    final savedLang = prefs.getString('language_code') ?? 'fr';
    _locale = Locale(savedLang);
    notifyListeners();
  }
  
  /// Change locale
  Future<void> setLocale(Locale locale) async {
    if (!supportedLocales.contains(locale)) return;
    
    _locale = locale;
    final prefs = await SharedPreferences.getInstance();
    await prefs.setString('language_code', locale.languageCode);
    notifyListeners();
  }
  
  /// Set locale by language code
  Future<void> setLanguageCode(String languageCode) async {
    await setLocale(Locale(languageCode));
  }
}
