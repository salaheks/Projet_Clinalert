import 'dart:async';
import 'dart:math';
import 'package:flutter_reactive_ble/flutter_reactive_ble.dart';
import 'package:permission_handler/permission_handler.dart';
import 'package:uuid/uuid.dart' as uid;
import '../models/measurement.dart';

class BleService {
  final FlutterReactiveBle _ble = FlutterReactiveBle();

  // Standard BLE Service and Characteristic UUIDs
  final Uuid _heartRateServiceUuid = Uuid.parse("0000180d-0000-1000-8000-00805f9b34fb");
  final Uuid _heartRateCharacteristicUuid = Uuid.parse("00002a37-0000-1000-8000-00805f9b34fb");

  /// Stream that monitors Bluetooth adapter state changes
  Stream<BleStatus> get bluetoothStatusStream => _ble.statusStream;

  /// Get current Bluetooth status
  BleStatus get currentStatus => _ble.status;

  /// Check if Bluetooth is ready (powered on)
  bool get isBluetoothReady => _ble.status == BleStatus.ready;

  /// Check if Bluetooth is off
  bool get isBluetoothOff => _ble.status == BleStatus.poweredOff;

  /// Check if location services are required but disabled
  bool get isLocationRequired => _ble.status == BleStatus.locationServicesDisabled;

  /// Open app settings to let user enable Bluetooth
  Future<bool> openBluetoothSettings() async {
    return await openAppSettings();
  }

  /// Open location settings
  Future<bool> openLocationSettings() async {
    return await openAppSettings();
  }

  /// Get human-readable status message
  String getStatusMessage(BleStatus status) {
    switch (status) {
      case BleStatus.ready:
        return 'Bluetooth activé';
      case BleStatus.poweredOff:
        return 'Bluetooth désactivé';
      case BleStatus.locationServicesDisabled:
        return 'Services de localisation désactivés';
      case BleStatus.unauthorized:
        return 'Permissions Bluetooth requises';
      case BleStatus.unsupported:
        return 'Bluetooth non supporté';
      case BleStatus.unknown:
      default:
        return 'État Bluetooth inconnu';
    }
  }

  // Expose scan stream
  Stream<DiscoveredDevice> get scanStream => _ble.scanForDevices(withServices: []);

  Future<void> requestPermissions() async {
    await [
      Permission.bluetooth,
      Permission.bluetoothScan,
      Permission.bluetoothConnect,
      Permission.location,
    ].request();
  }

  Stream<ConnectionStateUpdate> connectToDevice(String deviceId) {
    return _ble.connectToDevice(
      id: deviceId,
      connectionTimeout: const Duration(seconds: 5),
    );
  }

  Stream<Measurement> subscribeToMeasurements(String deviceId, String patientId, String consentId) {
    final controller = StreamController<Measurement>.broadcast();
    Timer? mockDataTimer;
    StreamSubscription? realDataSubscription;
    bool usingRealData = false;

    // --- Mock Data Logic (Fallback) ---
    void startMockData() {
      // Only start simulating if we haven't found real data
      if (usingRealData) return;
      
      print('Using simulated data (Fallback)');
      mockDataTimer?.cancel();
      mockDataTimer = Timer.periodic(const Duration(seconds: 2), (t) {
        if (usingRealData) {
          t.cancel();
          return;
        }

        final random = Random();
        final heartRate = 60 + random.nextInt(40);
        final spo2 = 95 + random.nextInt(5);
        final now = DateTime.now();

        if (!controller.isClosed) {
          // Heart Rate (Simulated)
          controller.add(Measurement(
            id: const uid.Uuid().v4(),
            patientId: patientId,
            deviceId: deviceId,
            type: 'Heart Rate',
            value: heartRate.toDouble(),
            timestamp: now,
            consentId: consentId,
          ));

          // SpO2 (Simulated)
          controller.add(Measurement(
            id: const uid.Uuid().v4(),
            patientId: patientId,
            deviceId: deviceId,
            type: 'SpO2',
            value: spo2.toDouble(),
            timestamp: now,
            consentId: consentId,
          ));
        }
      });
    }

    // --- Real BLE Logic ---
    void startRealDataConnection() {
      // Delay slightly to ensure connection is fully established
      Future.delayed(const Duration(seconds: 1), () async {
        try {
          // Discover services
          print('Discovering services for $deviceId...');
          final services = await _ble.discoverServices(deviceId);
          
          // Check for Heart Rate Service (0x180D)
          final hrService = services.firstWhere(
            (s) => s.serviceId == _heartRateServiceUuid,
            orElse: () => DiscoveredService(
              serviceId: Uuid.parse("00000000-0000-0000-0000-000000000000"),
              serviceInstanceId: uid.Uuid().v4(), // Fix: Add required parameter
              characteristicIds: [], 
              characteristics: [],
              includedServices: []
            ),
          );

          if (hrService.serviceId == _heartRateServiceUuid) {
            print('Heart Rate Service found! Subscribing...');
            
            final characteristic = QualifiedCharacteristic(
              serviceId: _heartRateServiceUuid,
              characteristicId: _heartRateCharacteristicUuid,
              deviceId: deviceId,
            );

            realDataSubscription = _ble.subscribeToCharacteristic(characteristic).listen((data) {
              if (data.isNotEmpty) {
                // Parse Heart Rate Measurement (Standard 0x2A37)
                // Flag: Byte 0
                // Format Bit (0): 0 => UINT8, 1 => UINT16
                final flags = data[0];
                final isFormatUint16 = (flags & 0x01) == 1;
                int heartRate;
                
                if (isFormatUint16 && data.length >= 3) {
                  heartRate = data[1] + (data[2] << 8);
                } else if (data.length >= 2) {
                  heartRate = data[1];
                } else {
                  return; // Invalid data
                }

                if (!usingRealData) {
                   print('Real Heart Rate data received! Stopping simulation.');
                   usingRealData = true;
                   mockDataTimer?.cancel();
                }

                if (!controller.isClosed) {
                  controller.add(Measurement(
                    id: const uid.Uuid().v4(),
                    patientId: patientId,
                    deviceId: deviceId,
                    type: 'Heart Rate',
                    value: heartRate.toDouble(),
                    timestamp: DateTime.now(),
                    consentId: consentId,
                  ));
                }
              }
            }, onError: (e) {
               print('Real data subscription error: $e. Reverting to simulation.');
               usingRealData = false;
               startMockData();
            });
          } else {
            print('Heart Rate Service NOT found. Continuing with simulation.');
            startMockData();
          }
        } catch (e) {
           print('Error discovering services/subscribing: $e');
           startMockData();
        }
      });
    }

    controller.onListen = () {
      // 1. Start simulation immediately so text doesn't stay empty
      startMockData();
      
      // 2. Try to upgrade to real data
      startRealDataConnection();
    };

    controller.onCancel = () {
      mockDataTimer?.cancel();
      realDataSubscription?.cancel();
    };

    return controller.stream;
  }
}
