import 'dart:convert';
import 'package:flutter_secure_storage/flutter_secure_storage.dart';
import 'package:hive_flutter/hive_flutter.dart';
import '../models/measurement.dart';
import '../models/consent.dart';

class StorageService {
  final FlutterSecureStorage _secureStorage = const FlutterSecureStorage();
  late Box<Measurement> _measurementBox;
  late Box<Consent> _consentBox;

  Future<void> init() async {
    await Hive.initFlutter();
    
    // Register Adapters
    Hive.registerAdapter(MeasurementAdapter());
    Hive.registerAdapter(ConsentAdapter());

    // Encryption Key
    String? keyString = await _secureStorage.read(key: 'hive_key');
    if (keyString == null) {
      final key = Hive.generateSecureKey();
      await _secureStorage.write(key: 'hive_key', value: base64UrlEncode(key));
      keyString = base64UrlEncode(key);
    }
    final encryptionKey = base64Url.decode(keyString);

    // Open Boxes
    _measurementBox = await Hive.openBox<Measurement>(
      'measurements',
      encryptionCipher: HiveAesCipher(encryptionKey),
    );
    
    _consentBox = await Hive.openBox<Consent>(
      'consents',
      encryptionCipher: HiveAesCipher(encryptionKey),
    );
  }

  Future<void> saveMeasurement(Measurement measurement) async {
    await _measurementBox.put(measurement.id, measurement);
  }

  List<Measurement> getMeasurements() {
    return _measurementBox.values.toList();
  }

  Future<void> saveConsent(Consent consent) async {
    await _consentBox.put(consent.id, consent);
  }

  Consent? getLastConsent() {
    if (_consentBox.isEmpty) return null;
    return _consentBox.values.last;
  }
}
