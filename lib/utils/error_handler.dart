import 'package:flutter/foundation.dart';

/// Global error handler for centralized error management
/// 
/// Use this class to log errors consistently across the app.
/// In production, integrate with Firebase Crashlytics or Sentry.
class AppLogger {
  static final AppLogger _instance = AppLogger._internal();
  factory AppLogger() => _instance;
  AppLogger._internal();

  /// Log debug messages (only in debug mode)
  void d(String message, [String? tag]) {
    if (kDebugMode) {
      final prefix = tag != null ? '[$tag] ' : '';
      debugPrint('🔍 $prefix$message');
    }
  }

  /// Log info messages
  void i(String message, [String? tag]) {
    if (kDebugMode) {
      final prefix = tag != null ? '[$tag] ' : '';
      debugPrint('ℹ️ $prefix$message');
    }
  }

  /// Log warning messages
  void w(String message, [String? tag]) {
    final prefix = tag != null ? '[$tag] ' : '';
    debugPrint('⚠️ $prefix$message');
  }

  /// Log error messages with optional stack trace
  void e(String message, [dynamic error, StackTrace? stackTrace, String? tag]) {
    final prefix = tag != null ? '[$tag] ' : '';
    debugPrint('❌ $prefix$message');
    
    if (error != null) {
      debugPrint('   Error: $error');
    }
    
    if (stackTrace != null && kDebugMode) {
      debugPrint('   Stack trace:\n$stackTrace');
    }

    // TODO: In production, send to crash reporting service
    // FirebaseCrashlytics.instance.recordError(error, stackTrace);
  }

  /// Log API request/response
  void api(String method, String url, {int? statusCode, dynamic body}) {
    if (kDebugMode) {
      final status = statusCode != null ? ' [$statusCode]' : '';
      debugPrint('🌐 $method $url$status');
      if (body != null) {
        debugPrint('   Body: $body');
      }
    }
  }
}

/// Global logger instance for easy access
final logger = AppLogger();

/// Exception wrapper for API errors
class ApiException implements Exception {
  final String message;
  final int? statusCode;
  final dynamic originalError;

  ApiException(this.message, {this.statusCode, this.originalError});

  @override
  String toString() => 'ApiException: $message (status: $statusCode)';

  /// Check if the error is an authentication error
  bool get isAuthError => statusCode == 401 || statusCode == 403;

  /// Check if the error is a network error
  bool get isNetworkError => statusCode == null;

  /// Check if the error is a server error
  bool get isServerError => statusCode != null && statusCode! >= 500;

  /// Get user-friendly error message
  String get userMessage {
    if (isAuthError) {
      return 'Session expirée. Veuillez vous reconnecter.';
    } else if (isNetworkError) {
      return 'Erreur de connexion. Vérifiez votre connexion internet.';
    } else if (isServerError) {
      return 'Erreur serveur. Veuillez réessayer plus tard.';
    }
    return message;
  }
}

/// Extension on dynamic to safely parse responses
extension SafeParsing on dynamic {
  /// Safely get a string value
  String? asString() => this is String ? this as String : null;

  /// Safely get an int value
  int? asInt() => this is int ? this as int : (this is String ? int.tryParse(this as String) : null);

  /// Safely get a double value
  double? asDouble() => this is double 
      ? this as double 
      : (this is int 
          ? (this as int).toDouble() 
          : (this is String ? double.tryParse(this as String) : null));

  /// Safely get a map
  Map<String, dynamic>? asMap() => this is Map<String, dynamic> ? this as Map<String, dynamic> : null;

  /// Safely get a list
  List<dynamic>? asList() => this is List<dynamic> ? this as List<dynamic> : null;
}
