/// Application configuration for ClinAlert
/// 
/// This class provides centralized configuration for API endpoints.
/// Values can be overridden at runtime using environment variables.
class AppConfig {
  /// Base URL for the backend API
  /// Override with --dart-define=BASE_URL=http://your-server:port
  static const String baseUrl = String.fromEnvironment(
    'BASE_URL',
    defaultValue: 'http://localhost:8080',
  );

  /// Auth API endpoint
  static String get authUrl => '$baseUrl/api/auth';

  /// Main API endpoint
  static String get apiUrl => '$baseUrl/api';

  /// Connection timeout in seconds
  static const int connectTimeout = 10;

  /// Receive timeout in seconds
  static const int receiveTimeout = 10;

  /// Whether we're in debug mode
  static const bool isDebug = bool.fromEnvironment('DEBUG', defaultValue: true);
}
