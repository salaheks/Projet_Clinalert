import 'dart:convert';
import 'package:dio/dio.dart';
import 'package:crypto/crypto.dart';
import '../models/measurement.dart';
import 'package:flutter_secure_storage/flutter_secure_storage.dart';

class ApiService {
  final Dio _dio = Dio();
  final FlutterSecureStorage _secureStorage = const FlutterSecureStorage();
  final String _baseUrl = 'http://10.0.2.2:8080/api'; // Android Emulator localhost

  ApiService() {
    _dio.options.baseUrl = _baseUrl;
    _dio.options.connectTimeout = const Duration(seconds: 5);
    _dio.options.receiveTimeout = const Duration(seconds: 3);
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
}
