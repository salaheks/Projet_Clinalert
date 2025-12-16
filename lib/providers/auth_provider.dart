import 'package:flutter/material.dart';
import '../services/auth_service.dart';
import '../services/api_service.dart';
import '../models/user_model.dart';

class AuthProvider with ChangeNotifier {
  final AuthService _authService;
  
  AuthProvider(this._authService) {
    _initializeAuth();
  }

  User? _currentUser;
  bool _isLoading = false;
  String? _error;
  String? _currentDoctorEmail; // For doctor role simulation

  User? get currentUser => _currentUser;
  bool get isLoading => _isLoading;
  String? get error => _error;
  bool get isAuthenticated => _currentUser != null;
  String? get currentDoctorEmail => _currentDoctorEmail;
  
  // Role-based getters
  bool get isAdmin => _currentUser?.role == UserRole.admin;
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
      case UserRole.admin:
        return '/doctor-dashboard'; // Admin uses doctor dashboard
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
    final first = _currentUser!.firstName.isNotEmpty ? _currentUser!.firstName[0] : '';
    final last = _currentUser!.lastName.isNotEmpty ? _currentUser!.lastName[0] : '';
    return '$first$last'.toUpperCase();
  }

  // Simulate login for testing role-based access
  void simulateLogin({required UserRole role, String? doctorEmail}) {
    _currentUser = User(
      id: 'simulated-${role.name}',
      firstName: role == UserRole.admin ? 'Admin' : 'Test',
      lastName: role == UserRole.admin ? 'User' : role.name.toUpperCase(),
      email: doctorEmail ?? '${role.name}@clinalert.com',
      phone: '+33-XXX-XXX-XXX',
      role: role,
      createdAt: DateTime.now(),
    );
    _currentDoctorEmail = doctorEmail;
    notifyListeners();
  }

  // Clear simulated login
  void clearSimulation() {
    _currentUser = null;
    _currentDoctorEmail = null;
    notifyListeners();
  }

  // Update user profile via API
  Future<void> updateProfile({
    String? firstName,
    String? lastName,
    String? phone,
    String? email,
  }) async {
    if (_currentUser == null) return;

    _isLoading = true;
    _error = null;
    notifyListeners();

    try {
      final apiService = ApiService();
      
      // Update profile (firstName, lastName, phone)
      final updatedData = await apiService.updateUserProfile(
        _currentUser!.id,
        firstName: firstName,
        lastName: lastName,
        phone: phone,
      );

      // Update email separately if changed
      if (email != null && email != _currentUser!.email) {
        await apiService.updateUserEmail(_currentUser!.id, email);
      }

      // Update local user state
      _currentUser = _currentUser!.copyWith(
        firstName: firstName ?? _currentUser!.firstName,
        lastName: lastName ?? _currentUser!.lastName,
        phone: phone ?? _currentUser!.phone,
        email: email ?? _currentUser!.email,
      );
    } catch (e) {
      _error = e.toString();
      rethrow;
    } finally {
      _isLoading = false;
      notifyListeners();
    }
  }
}