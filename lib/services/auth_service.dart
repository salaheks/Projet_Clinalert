import 'dart:convert';
import 'package:flutter/foundation.dart';
import 'package:shared_preferences/shared_preferences.dart';
import 'package:flutter_secure_storage/flutter_secure_storage.dart';
import 'package:dio/dio.dart';
import '../models/user_model.dart';
import '../config/app_config.dart';

class AuthService extends ChangeNotifier {
  User? _currentUser;
  bool _isLoading = false;
  String? _error;
  
  final Dio _dio = Dio();
  final FlutterSecureStorage _secureStorage = const FlutterSecureStorage();

  User? get currentUser => _currentUser;
  bool get isLoading => _isLoading;
  String? get error => _error;
  bool get isAuthenticated => _currentUser != null;

  AuthService() {
    _dio.options.baseUrl = AppConfig.authUrl;
    _dio.options.connectTimeout = Duration(seconds: AppConfig.connectTimeout);
    _dio.options.receiveTimeout = Duration(seconds: AppConfig.receiveTimeout);
    _dio.options.headers = {
      'Content-Type': 'application/json',
      'Accept': 'application/json',
    };
  }

  Future<void> login({
    required String email,
    required String password,
  }) async {
    _isLoading = true;
    _error = null;
    notifyListeners();

    try {
      print('🔐 Attempting login for: $email');
      print('🌐 Backend URL: ${AppConfig.authUrl}/login');
      
      // Call backend login endpoint
      final response = await _dio.post(
        '/login',
        data: {
          'email': email,
          'password': password,
        },
      );
      
      print('✅ Login response received: ${response.statusCode}');
      print('📦 Response data: ${response.data}');
      
      // Extract data from response
      final data = response.data;
      final token = data['token'];
      final userId = data['userId'];
      final userEmail = data['email'];
      final roleStr = data['role'];
      
      print('🎫 Token received: ${token?.substring(0, 20)}...');
      print('👤 User ID: $userId');
      print('📧 Email: $userEmail');
      print('🎭 Role: $roleStr');
      
      // Store JWT token in secure storage
      await _secureStorage.write(key: 'jwt_token', value: token);
      print('💾 Token stored in secure storage');
      
      // Map role string to UserRole enum
      UserRole role;
      switch (roleStr.toUpperCase()) {
        case 'ADMIN':
          role = UserRole.admin;
          break;
        case 'DOCTOR':
          role = UserRole.doctor;
          break;
        case 'NURSE':
          role = UserRole.nurse;
          break;
        case 'PATIENT':
          role = UserRole.patient;
          break;
        default:
          role = UserRole.patient;
      }
      
      // Fetch complete user profile from /me endpoint
      String firstName = '';
      String lastName = '';
      String phone = '';
      
      try {
        final meResponse = await _dio.get(
          '/me',
          options: Options(
            headers: {'Authorization': 'Bearer $token'},
          ),
        );
        print('📋 User profile fetched: ${meResponse.data}');
        firstName = meResponse.data['firstName'] ?? '';
        lastName = meResponse.data['lastName'] ?? '';
        phone = meResponse.data['phone'] ?? '';
      } catch (e) {
        print('⚠️ Could not fetch user profile: $e');
      }
      
      // Create user object with complete data
      _currentUser = User(
        id: userId,
        firstName: firstName,
        lastName: lastName,
        email: userEmail,
        phone: phone,
        role: role,
        createdAt: DateTime.now(),
      );

      // Store user data locally
      final prefs = await SharedPreferences.getInstance();
      await prefs.setString('currentUser', jsonEncode(_currentUser!.toJson()));
      
      print('✅ Login successful! User authenticated as $roleStr');
      
    } on DioException catch (e) {
      print('❌ DioException during login:');
      print('   Status code: ${e.response?.statusCode}');
      print('   Response data: ${e.response?.data}');
      print('   Error message: ${e.message}');
      
      if (e.response?.statusCode == 400 || e.response?.statusCode == 401) {
        _error = 'Invalid email or password';
      } else {
        _error = 'Connection error: ${e.message}';
      }
    } catch (e) {
      print('❌ Unexpected error during login: $e');
      _error = e.toString();
    } finally {
      _isLoading = false;
      notifyListeners();
    }
  }

  Future<void> signup({
    required String firstName,
    required String lastName,
    required String email,
    required String phone,
    required String password,
    required UserRole role,
  }) async {
    _isLoading = true;
    _error = null;
    notifyListeners();

    try {
      // Call backend register endpoint
      final response = await _dio.post(
        '/register',
        data: {
          'email': email,
          'password': password,
          'role': role.name.toUpperCase(),
        },
      );
      
      // Extract data from response
      final data = response.data;
      final token = data['token'];
      final userId = data['userId'];
      
      // Store JWT token in secure storage
      await _secureStorage.write(key: 'jwt_token', value: token);
      
      _currentUser = User(
        id: userId,
        firstName: firstName,
        lastName: lastName,
        email: email,
        phone: phone,
        role: role,
        createdAt: DateTime.now(),
      );

      // Store user data locally
      final prefs = await SharedPreferences.getInstance();
      await prefs.setString('currentUser', jsonEncode(_currentUser!.toJson()));
      
    } on DioException catch (e) {
      if (e.response?.statusCode == 400) {
        _error = e.response?.data['error'] ?? 'Registration failed';
      } else {
        _error = 'Connection error: ${e.message}';
      }
    } catch (e) {
      _error = e.toString();
    } finally {
      _isLoading = false;
      notifyListeners();
    }
  }

  Future<void> logout() async {
    _isLoading = true;
    notifyListeners();

    try {
      await Future.delayed(const Duration(milliseconds: 500));
      
      // Clear JWT token from secure storage
      await _secureStorage.delete(key: 'jwt_token');
      
      final prefs = await SharedPreferences.getInstance();
      await prefs.remove('currentUser');
      
      _currentUser = null;
      _error = null;
    } catch (e) {
      _error = e.toString();
    } finally {
      _isLoading = false;
      notifyListeners();
    }
  }

  Future<void> checkAuthStatus() async {
    try {
      final prefs = await SharedPreferences.getInstance();
      final userData = prefs.getString('currentUser');
      
      // Check if we have both user data and a JWT token
      final token = await _secureStorage.read(key: 'jwt_token');
      
      if (userData != null && token != null && token.isNotEmpty) {
        _currentUser = User.fromJson(jsonDecode(userData));
        notifyListeners();
      } else {
        // If either is missing, clear both to ensure clean state
        await prefs.remove('currentUser');
        await _secureStorage.delete(key: 'jwt_token');
      }
    } catch (e) {
      _error = e.toString();
      notifyListeners();
    }
  }

  void clearError() {
    _error = null;
    notifyListeners();
  }
}