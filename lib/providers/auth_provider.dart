import 'package:flutter/material.dart';
import '../services/auth_service.dart';
import '../models/user_model.dart';

class AuthProvider with ChangeNotifier {
  final AuthService _authService;
  
  AuthProvider(this._authService) {
    _initializeAuth();
  }

  User? _currentUser;
  bool _isLoading = false;
  String? _error;

  User? get currentUser => _currentUser;
  bool get isLoading => _isLoading;
  String? get error => _error;
  bool get isAuthenticated => _currentUser != null;
  
  // Role-based getters
  bool get isDoctor => _currentUser?.role == UserRole.doctor;
  bool get isNurse => _currentUser?.role == UserRole.nurse;
  bool get isPatient => _currentUser?.role == UserRole.patient;

  Future<void> _initializeAuth() async {
    _isLoading = true;
    notifyListeners();

    try {
      await _authService.checkAuthStatus();
      _currentUser = _authService.currentUser;
      _error = _authService.error;
    } catch (e) {
      _error = e.toString();
    } finally {
      _isLoading = false;
      notifyListeners();
    }
  }

  Future<void> login(String email, String password) async {
    _isLoading = true;
    _error = null;
    notifyListeners();

    try {
      await _authService.login(email: email, password: password);
      _currentUser = _authService.currentUser;
      _error = _authService.error;
      
      if (_error != null) {
        throw Exception(_error);
      }
    } catch (e) {
      _error = e.toString();
      _currentUser = null;
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
      await _authService.signup(
        firstName: firstName,
        lastName: lastName,
        email: email,
        phone: phone,
        password: password,
        role: role,
      );
      _currentUser = _authService.currentUser;
      _error = _authService.error;
      
      if (_error != null) {
        throw Exception(_error);
      }
    } catch (e) {
      _error = e.toString();
      _currentUser = null;
    } finally {
      _isLoading = false;
      notifyListeners();
    }
  }

  Future<void> logout() async {
    _isLoading = true;
    notifyListeners();

    try {
      await _authService.logout();
      _currentUser = null;
      _error = null;
    } catch (e) {
      _error = e.toString();
    } finally {
      _isLoading = false;
      notifyListeners();
    }
  }

  void clearError() {
    _error = null;
    notifyListeners();
  }

  // Role-based navigation
  String getDashboardRoute() {
    if (_currentUser == null) return '/login';
    
    switch (_currentUser!.role) {
      case UserRole.doctor:
        return '/doctor-dashboard';
      case UserRole.nurse:
        return '/nurse-dashboard';
      case UserRole.patient:
        return '/patient-dashboard';
    }
  }

  // Check if user has specific role
  bool hasRole(UserRole role) {
    return _currentUser?.role == role;
  }

  // Check if user has any of the specified roles
  bool hasAnyRole(List<UserRole> roles) {
    return _currentUser != null && roles.contains(_currentUser!.role);
  }

  // Get user display name
  String getUserDisplayName() {
    if (_currentUser == null) return '';
    return '${_currentUser!.firstName} ${_currentUser!.lastName}';
  }

  // Get user initials for avatar
  String getUserInitials() {
    if (_currentUser == null) return '';
    return '${_currentUser!.firstName[0]}${_currentUser!.lastName[0]}'.toUpperCase();
  }
}