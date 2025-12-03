import 'dart:async';
import 'dart:convert';
import 'package:flutter_reactive_ble/flutter_reactive_ble.dart';
import 'package:permission_handler/permission_handler.dart';
import '../models/measurement.dart';
import 'package:uuid/uuid.dart' as uuid_lib;

class BleService {
  final FlutterReactiveBle _ble = FlutterReactiveBle();
  final _uuid = const uuid_lib.Uuid();
  
  // UUIDs - Replace these with your actual device UUIDs
  static final Uuid _serviceUuid = Uuid.parse("0000180d-0000-1000-8000-00805f9b34fb");
  static final Uuid _charUuid = Uuid.parse("00002a37-0000-1000-8000-00805f9b34fb");

  Stream<DiscoveredDevice> get scanStream => _ble.scanForDevices(
    withServices: [_serviceUuid],
    scanMode: ScanMode.lowLatency,
  );

  Stream<ConnectionStateUpdate> get connectionStream => _ble.connectedDeviceStream;

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
      connectionTimeout: const Duration(seconds: 10),
    );
  }

  Stream<Measurement> subscribeToMeasurements(String deviceId, String patientId, String consentId) {
    final characteristic = QualifiedCharacteristic(
      serviceId: _serviceUuid,
      characteristicId: _charUuid,
      deviceId: deviceId,
    );

    return _ble.subscribeToCharacteristic(characteristic).map((data) {
      return _parseHeartRate(data, patientId, deviceId, consentId);
    });
  }

  Measurement _parseHeartRate(List<int> data, String patientId, String deviceId, String consentId) {
    if (data.isEmpty) {
      throw Exception("Empty data received");
    }

    // Byte 0: Flags
    int flags = data[0];
    bool is16Bit = (flags & 0x01) != 0;
    
    double value;
    if (is16Bit && data.length >= 3) {
      // UINT16 (Little Endian)
      value = (data[1] + (data[2] << 8)).toDouble();
    } else if (!is16Bit && data.length >= 2) {
      // UINT8
      value = data[1].toDouble();
    } else {
      value = 0.0; // Error or unknown format
    }

    return Measurement(
      id: _uuid.v4(),
      patientId: patientId,
      deviceId: deviceId,
      type: 'heart_rate',
      value: value,
      timestamp: DateTime.now(),
      consentId: consentId,
    );
  }
}
