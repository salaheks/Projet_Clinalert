import 'dart:async';
import 'package:flutter/material.dart';
import 'package:flutter_reactive_ble/flutter_reactive_ble.dart';
import 'package:provider/provider.dart';
import '../services/ble_service.dart';

class BleScanScreen extends StatefulWidget {
  const BleScanScreen({super.key});

  @override
  State<BleScanScreen> createState() => _BleScanScreenState();
}

class _BleScanScreenState extends State<BleScanScreen> {
  final List<DiscoveredDevice> _devices = [];
  StreamSubscription? _scanSubscription;
  bool _isScanning = false;

  @override
  void dispose() {
    _scanSubscription?.cancel();
    super.dispose();
  }

  void _startScan() async {
    final bleService = context.read<BleService>();
    await bleService.requestPermissions();
    
    setState(() {
      _devices.clear();
      _isScanning = true;
    });

    _scanSubscription = bleService.scanStream.listen((device) {
      final knownDeviceIndex = _devices.indexWhere((d) => d.id == device.id);
      if (knownDeviceIndex >= 0) {
        setState(() {
          _devices[knownDeviceIndex] = device;
        });
      } else {
        setState(() {
          _devices.add(device);
        });
      }
    }, onError: (e) {
      print('Scan error: $e');
      setState(() {
        _isScanning = false;
      });
    });
  }

  void _stopScan() {
    _scanSubscription?.cancel();
    setState(() {
      _isScanning = false;
    });
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Scan Devices'),
        actions: [
          if (_isScanning)
            IconButton(
              icon: const Icon(Icons.stop),
              onPressed: _stopScan,
            )
          else
            IconButton(
              icon: const Icon(Icons.refresh),
              onPressed: _startScan,
            ),
        ],
      ),
      body: ListView.builder(
        itemCount: _devices.length,
        itemBuilder: (context, index) {
          final device = _devices[index];
          return ListTile(
            title: Text(device.name.isNotEmpty ? device.name : 'Unknown Device'),
            subtitle: Text(device.id),
            trailing: Text('${device.rssi} dBm'),
            onTap: () {
              _stopScan();
              // Navigate to connection/measurement screen or return device
              Navigator.pop(context, device);
            },
          );
        },
      ),
    );
  }
}
