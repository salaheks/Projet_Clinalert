import 'dart:convert';
import 'package:dio/dio.dart';
import 'package:crypto/crypto.dart';
import '../models/measurement.dart';
import 'package:flutter_secure_storage/flutter_secure_storage.dart';
import '../config/app_config.dart';

class ApiService {
  final Dio _dio = Dio();
  final FlutterSecureStorage _secureStorage = const FlutterSecureStorage();

  ApiService() {
    _dio.options.baseUrl = AppConfig.apiUrl;
    _dio.options.connectTimeout = Duration(seconds: AppConfig.connectTimeout);
    _dio.options.receiveTimeout = Duration(seconds: AppConfig.receiveTimeout);
    
    // Add interceptor to automatically include JWT token in all requests
    _dio.interceptors.add(
      InterceptorsWrapper(
        onRequest: (options, handler) async {
          // Get token from secure storage
          final token = await _secureStorage.read(key: 'jwt_token');
          if (token != null && token.isNotEmpty) {
            options.headers['Authorization'] = 'Bearer $token';
          }
          return handler.next(options);
        },
        onError: (DioException error, handler) async {
          // Handle 401 errors (unauthorized)
          if (error.response?.statusCode == 401) {
            print('Authentication failed - token may be invalid or expired');
            // You could trigger a re-login here if needed
          }
          return handler.next(error);
        },
      ),
    );
  }

  Future<void> sendMeasurements(List<Measurement> measurements) async {
    final token = await _secureStorage.read(key: 'jwt_token');
    final hmacSecret = await _secureStorage.read(key: 'hmac_secret') ?? 'default_secret'; // In prod, fetch/provision this securely

    final payload = measurements.map((m) => m.toJson()).toList();
    final jsonBody = json.encode(payload);

    // Calculate HMAC
    final hmac = Hmac(sha256, utf8.encode(hmacSecret));
    final digest = hmac.convert(utf8.encode(jsonBody));
    final signature = digest.toString();

    try {
      await _dio.post(
        '/measurements',
        data: jsonBody,
        options: Options(
          headers: {
            'Content-Type': 'application/json',
            'Authorization': 'Bearer $token',
            'X-Signature': signature,
          },
        ),
      );
    } catch (e) {
      print('Error sending measurements: $e');
      throw e;
    }
  }

  /// Get all patients from the API
  Future<List<Map<String, dynamic>>> getAllPatients() async {
    try {
      final response = await _dio.get('/patients');
      return List<Map<String, dynamic>>.from(response.data);
    } catch (e) {
      print('Error fetching patients: $e');
      rethrow;
    }
  }

  /// Get patients by doctor ID
  Future<List<Map<String, dynamic>>> getPatientsByDoctor(String doctorId) async {
    try {
      final response = await _dio.get('/patients/doctor/$doctorId');
      return List<Map<String, dynamic>>.from(response.data);
    } catch (e) {
      print('Error fetching patients for doctor $doctorId: $e');
      rethrow;
    }
  }

  /// Get patients by clinic ID
  Future<List<Map<String, dynamic>>> getPatientsByClinic(String clinicId) async {
    try {
      final response = await _dio.get('/patients/clinic/$clinicId');
      return List<Map<String, dynamic>>.from(response.data);
    } catch (e) {
      print('Error fetching patients for clinic $clinicId: $e');
      rethrow;
    }
  }

  /// Update patient status
  Future<Map<String, dynamic>> updatePatientStatus(String patientId, String status) async {
    try {
      final response = await _dio.put(
        '/patients/$patientId/status',
        data: {'status': status},
      );
      return response.data as Map<String, dynamic>;
    } catch (e) {
      print('Error updating patient status: $e');
      rethrow;
    }
  }

  /// Create a new patient
  Future<Map<String, dynamic>> createPatient(Map<String, dynamic> patientData) async {
    try {
      final response = await _dio.post(
        '/patients',
        data: patientData,
      );
      return response.data as Map<String, dynamic>;
    } catch (e) {
      print('Error creating patient: $e');
      rethrow;
    }
  }

  /// Update patient information
  Future<Map<String, dynamic>> updatePatient(String patientId, Map<String, dynamic> patientData) async {
    try {
      final response = await _dio.put(
        '/patients/$patientId',
        data: patientData,
      );
      return response.data as Map<String, dynamic>;
    } catch (e) {
      print('Error updating patient: $e');
      rethrow;
    }
  }

  /// Delete a patient
  Future<void> deletePatient(String patientId) async {
    try {
      await _dio.delete('/patients/$patientId');
    } catch (e) {
      print('Error deleting patient: $e');
      rethrow;
    }
  }

  // ==================== Doctor CRUD Operations ====================

  /// Get all doctors from the API
  Future<List<Map<String, dynamic>>> getAllDoctors() async {
    try {
      final response = await _dio.get('/doctors');
      return List<Map<String, dynamic>>.from(response.data);
    } catch (e) {
      print('Error fetching doctors: $e');
      rethrow;
    }
  }

  /// Get a single doctor by ID
  Future<Map<String, dynamic>> getDoctorById(String doctorId) async {
    try {
      final response = await _dio.get('/doctors/$doctorId');
      return response.data as Map<String, dynamic>;
    } catch (e) {
      print('Error fetching doctor $doctorId: $e');
      rethrow;
    }
  }

  /// Create a new doctor
  Future<Map<String, dynamic>> createDoctor(Map<String, dynamic> doctorData) async {
    try {
      final response = await _dio.post(
        '/doctors',
        data: doctorData,
      );
      return response.data as Map<String, dynamic>;
    } catch (e) {
      print('Error creating doctor: $e');
      rethrow;
    }
  }

  /// Update doctor information
  Future<Map<String, dynamic>> updateDoctor(String doctorId, Map<String, dynamic> doctorData) async {
    try {
      final response = await _dio.put(
        '/doctors/$doctorId',
        data: doctorData,
      );
      return response.data as Map<String, dynamic>;
    } catch (e) {
      print('Error updating doctor: $e');
      rethrow;
    }
  }

  /// Delete a doctor
  Future<void> deleteDoctor(String doctorId) async {
    try {
      await _dio.delete('/doctors/$doctorId');
    } catch (e) {
      print('Error deleting doctor: $e');
      rethrow;
    }
  }

  // ==================== Clinic CRUD Operations ====================

  /// Get all clinics from the API
  Future<List<Map<String, dynamic>>> getAllClinics() async {
    try {
      final response = await _dio.get('/clinics');
      return List<Map<String, dynamic>>.from(response.data);
    } catch (e) {
      print('Error fetching clinics: $e');
      rethrow;
    }
  }

  /// Get a single clinic by ID
  Future<Map<String, dynamic>> getClinicById(String clinicId) async {
    try {
      final response = await _dio.get('/clinics/$clinicId');
      return response.data as Map<String, dynamic>;
    } catch (e) {
      print('Error fetching clinic $clinicId: $e');
      rethrow;
    }
  }

  /// Get clinics by doctor ID
  Future<List<Map<String, dynamic>>> getClinicsByDoctorId(String doctorId) async {
    try {
      final response = await _dio.get('/clinics/doctor/$doctorId');
      return List<Map<String, dynamic>>.from(response.data);
    } catch (e) {
      print('Error fetching clinics for doctor $doctorId: $e');
      rethrow;
    }
  }

  /// Create a new clinic
  Future<Map<String, dynamic>> createClinic(Map<String, dynamic> clinicData) async {
    try {
      final response = await _dio.post(
        '/clinics',
        data: clinicData,
      );
      return response.data as Map<String, dynamic>;
    } catch (e) {
      print('Error creating clinic: $e');
      rethrow;
    }
  }

  /// Update clinic information
  Future<Map<String, dynamic>> updateClinic(String clinicId, Map<String, dynamic> clinicData) async {
    try {
      final response = await _dio.put(
        '/clinics/$clinicId',
        data: clinicData,
      );
      return response.data as Map<String, dynamic>;
    } catch (e) {
      print('Error updating clinic: $e');
      rethrow;
    }
  }

  /// Delete a clinic
  Future<void> deleteClinic(String clinicId) async {
    try {
      await _dio.delete('/clinics/$clinicId');
    } catch (e) {
      print('Error deleting clinic: $e');
      rethrow;
    }
  }

  // ==================== User Management Operations ====================

  /// Get all users (admin only)
  Future<List<Map<String, dynamic>>> getAllUsers() async {
    try {
      final response = await _dio.get('/users');
      return List<Map<String, dynamic>>.from(response.data);
    } catch (e) {
      print('Error fetching users: $e');
      rethrow;
    }
  }

  /// Create a new user (admin only)
  Future<Map<String, dynamic>> createUser(Map<String, dynamic> userData) async {
    try {
      final response = await _dio.post('/users', data: userData);
      return response.data as Map<String, dynamic>;
    } catch (e) {
      print('Error creating user: $e');
      rethrow;
    }
  }

  /// Update user (admin only)
  Future<Map<String, dynamic>> updateUser(String userId, Map<String, dynamic> userData) async {
    try {
      final response = await _dio.put('/users/$userId', data: userData);
      return response.data as Map<String, dynamic>;
    } catch (e) {
      print('Error updating user: $e');
      rethrow;
    }
  }

  /// Delete a user (admin only)
  Future<void> deleteUser(String userId) async {
    try {
      await _dio.delete('/users/$userId');
    } catch (e) {
      print('Error deleting user: $e');
      rethrow;
    }
  }

  /// Update user email (admin only)
  Future<Map<String, dynamic>> updateUserEmail(String userId, String newEmail) async {
    try {
      final response = await _dio.put('/users/$userId/email', data: {'email': newEmail});
      return response.data as Map<String, dynamic>;
    } catch (e) {
      print('Error updating user email: $e');
      rethrow;
    }
  }

  /// Update user password (admin only)
  Future<Map<String, dynamic>> updateUserPassword(String userId, String newPassword) async {
    try {
      final response = await _dio.put('/users/$userId/password', data: {'password': newPassword});
      return response.data as Map<String, dynamic>;
    } catch (e) {
      print('Error updating user password: $e');
      rethrow;
    }
  }

  /// Update user profile (firstName, lastName, phone)
  Future<Map<String, dynamic>> updateUserProfile(String userId, {
    String? firstName,
    String? lastName,
    String? phone,
  }) async {
    try {
      final response = await _dio.put('/users/$userId/profile', data: {
        if (firstName != null) 'firstName': firstName,
        if (lastName != null) 'lastName': lastName,
        if (phone != null) 'phone': phone,
      });
      return response.data as Map<String, dynamic>;
    } catch (e) {
      print('Error updating user profile: $e');
      rethrow;
    }
  }

  // ==================== SmartWatch Device Operations ====================

  /// Register a smartwatch device for a patient
  Future<Map<String, dynamic>> registerSmartWatchDevice({
    required String patientId,
    required String deviceAddress,
    String? deviceName,
    String? deviceType,
  }) async {
    try {
      final response = await _dio.post('/smartwatch/devices', data: {
        'patientId': patientId,
        'deviceAddress': deviceAddress,
        'deviceName': deviceName,
        'deviceType': deviceType,
      });
      return response.data as Map<String, dynamic>;
    } catch (e) {
      print('Error registering smartwatch device: $e');
      rethrow;
    }
  }

  /// Get all devices for a patient
  Future<List<Map<String, dynamic>>> getPatientDevices(String patientId) async {
    try {
      final response = await _dio.get('/smartwatch/devices/$patientId');
      return List<Map<String, dynamic>>.from(response.data);
    } catch (e) {
      print('Error fetching patient devices: $e');
      rethrow;
    }
  }

  /// Get active devices for a patient
  Future<List<Map<String, dynamic>>> getActiveDevices(String patientId) async {
    try {
      final response = await _dio.get('/smartwatch/devices/$patientId/active');
      return List<Map<String, dynamic>>.from(response.data);
    } catch (e) {
      print('Error fetching active devices: $e');
      rethrow;
    }
  }

  /// Deactivate a device
  Future<void> deactivateDevice(String deviceId) async {
    try {
      await _dio.put('/smartwatch/devices/$deviceId/deactivate');
    } catch (e) {
      print('Error deactivating device: $e');
      rethrow;
    }
  }

  /// Delete a device
  Future<void> deleteSmartWatchDevice(String deviceId) async {
    try {
      await _dio.delete('/smartwatch/devices/$deviceId');
    } catch (e) {
      print('Error deleting device: $e');
      rethrow;
    }
  }

  /// Ping device to update last connected time
  Future<void> pingDevice(String deviceId) async {
    try {
      await _dio.put('/smartwatch/devices/$deviceId/ping');
    } catch (e) {
      print('Error pinging device: $e');
      rethrow;
    }
  }

  // ==================== Health Data Operations ====================

  /// Submit health data batch from smartwatch
  Future<Map<String, dynamic>> submitHealthData(List<Map<String, dynamic>> healthDataList) async {
    try {
      final response = await _dio.post('/smartwatch/health-data', data: healthDataList);
      return response.data as Map<String, dynamic>;
    } catch (e) {
      print('Error submitting health data: $e');
      rethrow;
    }
  }

  /// Submit single health data point
  Future<Map<String, dynamic>> submitSingleHealthData(Map<String, dynamic> healthData) async {
    try {
      final response = await _dio.post('/smartwatch/health-data/single', data: healthData);
      return response.data as Map<String, dynamic>;
    } catch (e) {
      print('Error submitting health data: $e');
      rethrow;
    }
  }

  /// Get patient health data history
  Future<List<Map<String, dynamic>>> getPatientHealthData(String patientId) async {
    try {
      final response = await _dio.get('/smartwatch/health-data/$patientId');
      return List<Map<String, dynamic>>.from(response.data);
    } catch (e) {
      print('Error fetching health data: $e');
      rethrow;
    }
  }

  /// Get patient health data for a specific range
  Future<List<Map<String, dynamic>>> getPatientHealthDataRange(
    String patientId,
    DateTime start,
    DateTime end,
  ) async {
    try {
      final response = await _dio.get(
        '/smartwatch/health-data/$patientId/range',
        queryParameters: {
          'start': start.toIso8601String(),
          'end': end.toIso8601String(),
        },
      );
      return List<Map<String, dynamic>>.from(response.data);
    } catch (e) {
      print('Error fetching health data range: $e');
      rethrow;
    }
  }

  /// Get heart rate history
  Future<List<Map<String, dynamic>>> getHeartRateHistory(String patientId) async {
    try {
      final response = await _dio.get('/smartwatch/health-data/$patientId/heart-rate');
      return List<Map<String, dynamic>>.from(response.data);
    } catch (e) {
      print('Error fetching heart rate history: $e');
      rethrow;
    }
  }

  /// Get steps history
  Future<List<Map<String, dynamic>>> getStepsHistory(String patientId) async {
    try {
      final response = await _dio.get('/smartwatch/health-data/$patientId/steps');
      return List<Map<String, dynamic>>.from(response.data);
    } catch (e) {
      print('Error fetching steps history: $e');
      rethrow;
    }
  }

  /// Get SpO2 history
  Future<List<Map<String, dynamic>>> getSpO2History(String patientId) async {
    try {
      final response = await _dio.get('/smartwatch/health-data/$patientId/spo2');
      return List<Map<String, dynamic>>.from(response.data);
    } catch (e) {
      print('Error fetching SpO2 history: $e');
      rethrow;
    }
  }

  /// Get sleep history
  Future<List<Map<String, dynamic>>> getSleepHistory(String patientId) async {
    try {
      final response = await _dio.get('/smartwatch/health-data/$patientId/sleep');
      return List<Map<String, dynamic>>.from(response.data);
    } catch (e) {
      print('Error fetching sleep history: $e');
      rethrow;
    }
  }

  /// Get patient health stats
  Future<Map<String, dynamic>> getPatientHealthStats(String patientId) async {
    try {
      final response = await _dio.get('/smartwatch/health-data/$patientId/stats');
      return response.data as Map<String, dynamic>;
    } catch (e) {
      print('Error fetching health stats: $e');
      rethrow;
    }
  }

  // ==================== Daily Summary Operations ====================

  /// Generate daily summary for a patient
  Future<Map<String, dynamic>> generateDailySummary(String patientId, {DateTime? date}) async {
    try {
      final response = await _dio.post(
        '/smartwatch/daily-summary/$patientId/generate',
        queryParameters: date != null ? {'date': date.toIso8601String().split('T')[0]} : null,
      );
      return response.data as Map<String, dynamic>;
    } catch (e) {
      print('Error generating daily summary: $e');
      rethrow;
    }
  }

  /// Get recent daily summaries
  Future<List<Map<String, dynamic>>> getRecentDailySummaries(String patientId) async {
    try {
      final response = await _dio.get('/smartwatch/daily-summary/$patientId');
      return List<Map<String, dynamic>>.from(response.data);
    } catch (e) {
      print('Error fetching daily summaries: $e');
      rethrow;
    }
  }

  /// Get daily summary for a specific date
  Future<Map<String, dynamic>?> getDailySummary(String patientId, DateTime date) async {
    try {
      final dateStr = date.toIso8601String().split('T')[0];
      final response = await _dio.get('/smartwatch/daily-summary/$patientId/$dateStr');
      if (response.data is Map && response.data['message'] != null) {
        return null; // No summary available
      }
      return response.data as Map<String, dynamic>;
    } catch (e) {
      print('Error fetching daily summary: $e');
      rethrow;
    }
  }

  /// Get daily summaries for a date range
  Future<List<Map<String, dynamic>>> getDailySummariesRange(
    String patientId,
    DateTime start,
    DateTime end,
  ) async {
    try {
      final response = await _dio.get(
        '/smartwatch/daily-summary/$patientId/range',
        queryParameters: {
          'start': start.toIso8601String().split('T')[0],
          'end': end.toIso8601String().split('T')[0],
        },
      );
      return List<Map<String, dynamic>>.from(response.data);
    } catch (e) {
      print('Error fetching daily summaries range: $e');
      rethrow;
    }
  }

  // ==================== Alerts Operations ====================

  /// Get all alerts
  Future<List<Map<String, dynamic>>> getAllAlerts() async {
    try {
      final response = await _dio.get('/alerts');
      return List<Map<String, dynamic>>.from(response.data);
    } catch (e) {
      print('Error fetching alerts: $e');
      rethrow;
    }
  }

  /// Get alerts for a specific patient
  Future<List<Map<String, dynamic>>> getPatientAlerts(String patientId) async {
    try {
      final response = await _dio.get('/alerts/patient/$patientId');
      return List<Map<String, dynamic>>.from(response.data);
    } catch (e) {
      print('Error fetching patient alerts: $e');
      rethrow;
    }
  }

  /// Get unread alerts
  Future<List<Map<String, dynamic>>> getUnreadAlerts() async {
    try {
      final response = await _dio.get('/alerts/unread');
      return List<Map<String, dynamic>>.from(response.data);
    } catch (e) {
      print('Error fetching unread alerts: $e');
      rethrow;
    }
  }

  /// Mark alert as read
  Future<void> markAlertAsRead(String alertId) async {
    try {
      await _dio.put('/alerts/$alertId/read');
    } catch (e) {
      print('Error marking alert as read: $e');
      rethrow;
    }
  }

  // ==================== Report Operations ====================

  /// Download patient report PDF
  Future<void> downloadPatientReport(String patientId, String savePath) async {
    try {
      await _dio.download(
        '/reports/patient/$patientId',
        savePath,
        options: Options(
          responseType: ResponseType.bytes,
          followRedirects: false,
        ),
      );
    } catch (e) {
      print('Error downloading report: $e');
      rethrow;
    }
  }
}
