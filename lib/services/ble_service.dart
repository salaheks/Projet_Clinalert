import 'dart:async';
import 'dart:math';
import 'package:flutter_reactive_ble/flutter_reactive_ble.dart';
import 'package:permission_handler/permission_handler.dart';
import 'package:uuid/uuid.dart' as uid;
import '../models/measurement.dart';

class BleService {
  final FlutterReactiveBle _ble = FlutterReactiveBle();

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
    // For demonstration, we simulate data. 
    // In a real app, you would subscribe to specific characteristics here.
    final controller = StreamController<Measurement>.broadcast();
    Timer? timer;

    void startMockData() {
      timer = Timer.periodic(const Duration(seconds: 2), (t) {
        final random = Random();
        final heartRate = 60 + random.nextInt(40);
        final spo2 = 95 + random.nextInt(5);
        final now = DateTime.now();

        if (!controller.isClosed) {
          controller.add(Measurement(
            id: const uid.Uuid().v4(),
            patientId: patientId,
            deviceId: deviceId,
            type: 'Heart Rate',
            value: heartRate.toDouble(),
            timestamp: now,
            consentId: consentId,
          ));

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

    controller.onListen = startMockData;
    controller.onCancel = () {
      timer?.cancel();
    };

    return controller.stream;
  }
}
