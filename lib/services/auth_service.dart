import 'dart:convert';
import 'package:flutter/foundation.dart';
import 'package:shared_preferences/shared_preferences.dart';
import 'package:uuid/uuid.dart';
import '../models/user_model.dart';

class AuthService extends ChangeNotifier {
  User? _currentUser;
  bool _isLoading = false;
  String? _error;

  User? get currentUser => _currentUser;
  bool get isLoading => _isLoading;
  String? get error => _error;
  bool get isAuthenticated => _currentUser != null;

  Future<void> login({
    required String email,
    required String password,
  }) async {
    _isLoading = true;
    _error = null;
    notifyListeners();

    try {
      // Simulate API call
      await Future.delayed(const Duration(seconds: 2));
      
      // Mock authentication - in real app, this would be an API call
      if (email == 'doctor@clinalert.com' && password == 'password123') {
        _currentUser = User(
          id: '1',
          firstName: 'Dr. Sarah',
          lastName: 'Johnson',
          email: email,
          phone: '+1-555-0123',
          role: UserRole.doctor,
          createdAt: DateTime.now(),
        );
      } else if (email == 'nurse@clinalert.com' && password == 'password123') {
        _currentUser = User(
          id: '2',
          firstName: 'Michael',
          lastName: 'Chen',
          email: email,
          phone: '+1-555-0124',
          role: UserRole.nurse,
          createdAt: DateTime.now(),
        );
      } else if (email == 'patient@clinalert.com' && password == 'password123') {
        _currentUser = User(
          id: '3',
          firstName: 'Emma',
          lastName: 'Williams',
          email: email,
          phone: '+1-555-0125',
          role: UserRole.patient,
          createdAt: DateTime.now(),
        );
      } else {
        throw Exception('Invalid email or password');
      }

      // Store user data locally
      final prefs = await SharedPreferences.getInstance();
      await prefs.setString('currentUser', jsonEncode(_currentUser!.toJson()));
      
    } catch (e) {
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
      // Simulate API call
      await Future.delayed(const Duration(seconds: 2));
      
      // Mock signup - in real app, this would create user in backend
      _currentUser = User(
        id: const Uuid().v4(),
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
      
      if (userData != null) {
        _currentUser = User.fromJson(jsonDecode(userData));
        notifyListeners();
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