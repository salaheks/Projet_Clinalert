import 'dart:async';
import 'dart:math';
import 'package:flutter/material.dart';
import 'package:flutter_reactive_ble/flutter_reactive_ble.dart';
import 'package:google_fonts/google_fonts.dart';
import 'package:uuid/uuid.dart' as uid;
import '../models/smartwatch_device_model.dart';
import '../models/health_data_model.dart';
import '../services/ble_service.dart';
import '../services/api_service.dart';

class SmartWatchConnectionScreen extends StatefulWidget {
  final String patientId;
  final String? patientName;

  const SmartWatchConnectionScreen({
    super.key,
    required this.patientId,
    this.patientName,
  });

  @override
  State<SmartWatchConnectionScreen> createState() => _SmartWatchConnectionScreenState();
}

class _SmartWatchConnectionScreenState extends State<SmartWatchConnectionScreen> {
  final BleService _bleService = BleService();
  final ApiService _apiService = ApiService();
  
  final List<DiscoveredDevice> _discoveredDevices = [];
  List<SmartWatchDevice> _registeredDevices = [];
  
  StreamSubscription? _scanSubscription;
  StreamSubscription? _connectionSubscription;
  StreamSubscription? _dataSubscription;
  StreamSubscription? _bluetoothStatusSubscription;
  
  bool _isScanning = false;
  bool _isLoading = true;
  String? _connectingDeviceId;
  String? _connectedDeviceId;
  BleStatus _bluetoothStatus = BleStatus.unknown;
  
  // Simulated real-time data
  int? _currentHeartRate;
  double? _currentSpO2;
  int? _currentSteps;

  @override
  void initState() {
    super.initState();
    _loadRegisteredDevices();
    _monitorBluetoothStatus();
  }

  void _monitorBluetoothStatus() {
    // Get initial status
    _bluetoothStatus = _bleService.currentStatus;
    
    // Listen to status changes
    _bluetoothStatusSubscription = _bleService.bluetoothStatusStream.listen((status) {
      if (mounted) {
        setState(() => _bluetoothStatus = status);
      }
    });
  }

  @override
  void dispose() {
    _scanSubscription?.cancel();
    _connectionSubscription?.cancel();
    _dataSubscription?.cancel();
    _bluetoothStatusSubscription?.cancel();
    super.dispose();
  }

  Future<void> _loadRegisteredDevices() async {
    setState(() => _isLoading = true);
    try {
      final devicesData = await _apiService.getPatientDevices(widget.patientId);
      setState(() {
        _registeredDevices = devicesData.map((d) => SmartWatchDevice.fromJson(d)).toList();
        _isLoading = false;
      });
    } catch (e) {
      print('Error loading devices: $e');
      setState(() => _isLoading = false);
    }
  }

  Future<void> _startScan() async {
    await _bleService.requestPermissions();
    
    setState(() {
      _discoveredDevices.clear();
      _isScanning = true;
    });

    _scanSubscription = _bleService.scanStream.listen((device) {
      final existingIndex = _discoveredDevices.indexWhere((d) => d.id == device.id);
      setState(() {
        if (existingIndex >= 0) {
          _discoveredDevices[existingIndex] = device;
        } else {
          _discoveredDevices.add(device);
        }
      });
    }, onError: (e) {
      print('Scan error: $e');
      setState(() => _isScanning = false);
    });

    // Auto stop after 30 seconds
    Future.delayed(const Duration(seconds: 30), () {
      if (_isScanning) {
        _stopScan();
      }
    });
  }

  void _stopScan() {
    _scanSubscription?.cancel();
    setState(() => _isScanning = false);
  }

  Future<void> _connectToDevice(DiscoveredDevice device) async {
    setState(() => _connectingDeviceId = device.id);

    try {
      // Register device in backend
      await _apiService.registerSmartWatchDevice(
        patientId: widget.patientId,
        deviceAddress: device.id,
        deviceName: device.name.isNotEmpty ? device.name : 'Unknown Device',
        deviceType: 'Generic',
      );

      // Start connection
      _connectionSubscription = _bleService.connectToDevice(device.id).listen((state) {
        if (state.connectionState == DeviceConnectionState.connected) {
          setState(() {
            _connectingDeviceId = null;
            _connectedDeviceId = device.id;
          });
          _startDataSimulation(device.id);
          _loadRegisteredDevices();
        } else if (state.connectionState == DeviceConnectionState.disconnected) {
          setState(() {
            _connectedDeviceId = null;
            _currentHeartRate = null;
            _currentSpO2 = null;
            _currentSteps = null;
          });
        }
      });

      // Simulate immediate connection for demo
      await Future.delayed(const Duration(seconds: 2));
      setState(() {
        _connectingDeviceId = null;
        _connectedDeviceId = device.id;
      });
      _startDataSimulation(device.id);
      _loadRegisteredDevices();

    } catch (e) {
      print('Connection error: $e');
      setState(() => _connectingDeviceId = null);
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(content: Text('Connection error: $e'), backgroundColor: Colors.red),
        );
      }
    }
  }

  void _startDataSimulation(String deviceId) {
    final random = Random();
    _dataSubscription?.cancel();
    
    // Simulate data coming in every 2 seconds
    _dataSubscription = Stream.periodic(const Duration(seconds: 2)).listen((_) async {
      if (!mounted || _connectedDeviceId == null) {
        _dataSubscription?.cancel();
        return;
      }

      final heartRate = 60 + random.nextInt(40);
      final spO2 = 95.0 + random.nextDouble() * 4;
      final steps = random.nextInt(100);

      setState(() {
        _currentHeartRate = heartRate;
        _currentSpO2 = spO2;
        _currentSteps = (_currentSteps ?? 0) + steps;
      });

      // Send to backend
      try {
        await _apiService.submitSingleHealthData({
          'patientId': widget.patientId,
          'deviceId': deviceId,
          'heartRate': heartRate,
          'spO2': spO2,
          'steps': _currentSteps,
          'timestamp': DateTime.now().toIso8601String(),
          'source': 'smartwatch',
        });
      } catch (e) {
        print('Error sending health data: $e');
      }
    });
  }

  void _disconnectDevice() {
    _dataSubscription?.cancel();
    _connectionSubscription?.cancel();
    setState(() {
      _connectedDeviceId = null;
      _currentHeartRate = null;
      _currentSpO2 = null;
      _currentSteps = null;
    });
  }

  Future<void> _removeDevice(SmartWatchDevice device) async {
    final confirmed = await showDialog<bool>(
      context: context,
      builder: (context) => AlertDialog(
        title: const Text('Remove Device'),
        content: Text('Remove ${device.displayName} from this patient?'),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(context, false),
            child: const Text('Cancel'),
          ),
          TextButton(
            onPressed: () => Navigator.pop(context, true),
            style: TextButton.styleFrom(foregroundColor: Colors.red),
            child: const Text('Remove'),
          ),
        ],
      ),
    );

    if (confirmed == true) {
      try {
        await _apiService.deleteSmartWatchDevice(device.id);
        _loadRegisteredDevices();
      } catch (e) {
        if (mounted) {
          ScaffoldMessenger.of(context).showSnackBar(
            SnackBar(content: Text('Error: $e'), backgroundColor: Colors.red),
          );
        }
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text('Connect SmartWatch'),
        actions: [
          if (_isScanning)
            IconButton(
              icon: const Icon(Icons.stop),
              onPressed: _stopScan,
              tooltip: 'Stop Scan',
            )
          else
            IconButton(
              icon: const Icon(Icons.refresh),
              onPressed: _startScan,
              tooltip: 'Scan Devices',
            ),
        ],
      ),
      body: _isLoading
          ? const Center(child: CircularProgressIndicator())
          : RefreshIndicator(
              onRefresh: _loadRegisteredDevices,
              child: CustomScrollView(
                slivers: [
                  // Bluetooth Status Banner
                  SliverToBoxAdapter(
                    child: _buildBluetoothStatusBanner(),
                  ),
                  
                  // Connected Device Section
                  if (_connectedDeviceId != null)
                    SliverToBoxAdapter(
                      child: _buildConnectedSection(),
                    ),

                  // Registered Devices Section
                  SliverToBoxAdapter(
                    child: _buildSectionHeader('Registered Devices', 
                      trailing: Text('${_registeredDevices.length} devices')),
                  ),
                  if (_registeredDevices.isEmpty)
                    const SliverToBoxAdapter(
                      child: Padding(
                        padding: EdgeInsets.all(16),
                        child: Text('No devices registered. Scan for new devices.'),
                      ),
                    )
                  else
                    SliverList(
                      delegate: SliverChildBuilderDelegate(
                        (context, index) => _buildRegisteredDeviceTile(_registeredDevices[index]),
                        childCount: _registeredDevices.length,
                      ),
                    ),

                  // Discovered Devices Section
                  SliverToBoxAdapter(
                    child: _buildSectionHeader('Available Devices', 
                      trailing: _isScanning 
                          ? const SizedBox(
                              width: 16, height: 16,
                              child: CircularProgressIndicator(strokeWidth: 2),
                            )
                          : null),
                  ),
                  if (_discoveredDevices.isEmpty && !_isScanning)
                    SliverToBoxAdapter(
                      child: Padding(
                        padding: const EdgeInsets.all(16),
                        child: Column(
                          children: [
                            Icon(Icons.bluetooth_searching, size: 48, color: Colors.grey[400]),
                            const SizedBox(height: 8),
                            Text(
                              'Tap the refresh button to scan for devices',
                              style: TextStyle(color: Colors.grey[600]),
                            ),
                          ],
                        ),
                      ),
                    )
                  else
                    SliverList(
                      delegate: SliverChildBuilderDelegate(
                        (context, index) => _buildDiscoveredDeviceTile(_discoveredDevices[index]),
                        childCount: _discoveredDevices.length,
                      ),
                    ),

                  const SliverToBoxAdapter(child: SizedBox(height: 100)),
                ],
              ),
            ),
      floatingActionButton: _isScanning
          ? null
          : FloatingActionButton.extended(
              onPressed: _startScan,
              icon: const Icon(Icons.bluetooth_searching),
              label: const Text('Scan'),
            ),
    );
  }

  Widget _buildBluetoothStatusBanner() {
    final isReady = _bluetoothStatus == BleStatus.ready;
    final isOff = _bluetoothStatus == BleStatus.poweredOff;
    final needsLocation = _bluetoothStatus == BleStatus.locationServicesDisabled;
    final isUnauthorized = _bluetoothStatus == BleStatus.unauthorized;
    
    Color bannerColor;
    IconData bannerIcon;
    String statusText;
    String buttonText;
    
    if (isReady) {
      bannerColor = Colors.green;
      bannerIcon = Icons.bluetooth_connected;
      statusText = 'Bluetooth activé';
      buttonText = 'Désactiver';
    } else if (isOff) {
      bannerColor = Colors.red;
      bannerIcon = Icons.bluetooth_disabled;
      statusText = 'Bluetooth désactivé';
      buttonText = 'Activer';
    } else if (needsLocation) {
      bannerColor = Colors.orange;
      bannerIcon = Icons.location_off;
      statusText = 'Localisation requise';
      buttonText = 'Paramètres';
    } else if (isUnauthorized) {
      bannerColor = Colors.orange;
      bannerIcon = Icons.security;
      statusText = 'Permissions requises';
      buttonText = 'Autoriser';
    } else {
      bannerColor = Colors.grey;
      bannerIcon = Icons.bluetooth_searching;
      statusText = 'Vérification...';
      buttonText = 'Paramètres';
    }
    
    return Container(
      margin: const EdgeInsets.all(16),
      padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
      decoration: BoxDecoration(
        color: bannerColor.withOpacity(0.1),
        borderRadius: BorderRadius.circular(12),
        border: Border.all(color: bannerColor.withOpacity(0.3)),
      ),
      child: Row(
        children: [
          Container(
            padding: const EdgeInsets.all(8),
            decoration: BoxDecoration(
              color: bannerColor.withOpacity(0.2),
              shape: BoxShape.circle,
            ),
            child: Icon(bannerIcon, color: bannerColor, size: 24),
          ),
          const SizedBox(width: 12),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  statusText,
                  style: GoogleFonts.inter(
                    fontWeight: FontWeight.bold,
                    color: bannerColor,
                    fontSize: 14,
                  ),
                ),
                Text(
                  isReady 
                      ? 'Prêt à scanner les appareils'
                      : 'Appuyez pour ouvrir les paramètres',
                  style: TextStyle(
                    color: Colors.grey[600],
                    fontSize: 12,
                  ),
                ),
              ],
            ),
          ),
          ElevatedButton(
            onPressed: () async {
              await _bleService.openBluetoothSettings();
            },
            style: ElevatedButton.styleFrom(
              backgroundColor: bannerColor,
              foregroundColor: Colors.white,
              padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
              shape: RoundedRectangleBorder(
                borderRadius: BorderRadius.circular(20),
              ),
            ),
            child: Text(buttonText, style: const TextStyle(fontSize: 12)),
          ),
        ],
      ),
    );
  }

  Widget _buildSectionHeader(String title, {Widget? trailing}) {
    return Padding(
      padding: const EdgeInsets.fromLTRB(16, 24, 16, 8),
      child: Row(
        children: [
          Text(
            title,
            style: GoogleFonts.inter(
              fontSize: 16,
              fontWeight: FontWeight.bold,
              color: Colors.grey[800],
            ),
          ),
          const Spacer(),
          if (trailing != null) trailing,
        ],
      ),
    );
  }

  Widget _buildConnectedSection() {
    return Container(
      margin: const EdgeInsets.all(16),
      padding: const EdgeInsets.all(20),
      decoration: BoxDecoration(
        gradient: LinearGradient(
          colors: [const Color(0xFF4A90E2), const Color(0xFF357ABD)],
          begin: Alignment.topLeft,
          end: Alignment.bottomRight,
        ),
        borderRadius: BorderRadius.circular(16),
        boxShadow: [
          BoxShadow(
            color: const Color(0xFF4A90E2).withOpacity(0.3),
            blurRadius: 12,
            offset: const Offset(0, 6),
          ),
        ],
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            children: [
              Container(
                padding: const EdgeInsets.all(8),
                decoration: BoxDecoration(
                  color: Colors.white.withOpacity(0.2),
                  borderRadius: BorderRadius.circular(8),
                ),
                child: const Icon(Icons.watch, color: Colors.white, size: 24),
              ),
              const SizedBox(width: 12),
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      'Connected',
                      style: GoogleFonts.inter(
                        color: Colors.white,
                        fontSize: 18,
                        fontWeight: FontWeight.bold,
                      ),
                    ),
                    Text(
                      'Receiving live data...',
                      style: GoogleFonts.inter(
                        color: Colors.white70,
                        fontSize: 12,
                      ),
                    ),
                  ],
                ),
              ),
              IconButton(
                icon: const Icon(Icons.close, color: Colors.white),
                onPressed: _disconnectDevice,
              ),
            ],
          ),
          const SizedBox(height: 20),
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceAround,
            children: [
              _buildLiveMetric(
                icon: Icons.favorite,
                value: _currentHeartRate?.toString() ?? '--',
                unit: 'bpm',
                label: 'Heart Rate',
              ),
              _buildLiveMetric(
                icon: Icons.air,
                value: _currentSpO2?.toStringAsFixed(0) ?? '--',
                unit: '%',
                label: 'SpO2',
              ),
              _buildLiveMetric(
                icon: Icons.directions_walk,
                value: _currentSteps?.toString() ?? '--',
                unit: '',
                label: 'Steps',
              ),
            ],
          ),
        ],
      ),
    );
  }

  Widget _buildLiveMetric({
    required IconData icon,
    required String value,
    required String unit,
    required String label,
  }) {
    return Column(
      children: [
        Icon(icon, color: Colors.white70, size: 20),
        const SizedBox(height: 4),
        Row(
          mainAxisSize: MainAxisSize.min,
          crossAxisAlignment: CrossAxisAlignment.end,
          children: [
            Text(
              value,
              style: GoogleFonts.inter(
                color: Colors.white,
                fontSize: 24,
                fontWeight: FontWeight.bold,
              ),
            ),
            if (unit.isNotEmpty) ...[
              const SizedBox(width: 2),
              Text(
                unit,
                style: GoogleFonts.inter(
                  color: Colors.white70,
                  fontSize: 12,
                ),
              ),
            ],
          ],
        ),
        Text(
          label,
          style: GoogleFonts.inter(
            color: Colors.white70,
            fontSize: 10,
          ),
        ),
      ],
    );
  }

  Widget _buildRegisteredDeviceTile(SmartWatchDevice device) {
    final isConnected = _connectedDeviceId == device.deviceAddress;
    
    return ListTile(
      leading: Container(
        padding: const EdgeInsets.all(10),
        decoration: BoxDecoration(
          color: isConnected ? Colors.green.withOpacity(0.1) : Colors.blue.withOpacity(0.1),
          borderRadius: BorderRadius.circular(10),
        ),
        child: Icon(
          Icons.watch,
          color: isConnected ? Colors.green : Colors.blue,
        ),
      ),
      title: Text(device.displayName),
      subtitle: Text(device.connectionStatus),
      trailing: Row(
        mainAxisSize: MainAxisSize.min,
        children: [
          if (device.isActive)
            Container(
              padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
              decoration: BoxDecoration(
                color: isConnected ? Colors.green.withOpacity(0.1) : Colors.grey.withOpacity(0.1),
                borderRadius: BorderRadius.circular(12),
              ),
              child: Text(
                isConnected ? 'Connected' : 'Active',
                style: TextStyle(
                  color: isConnected ? Colors.green : Colors.grey[600],
                  fontSize: 12,
                ),
              ),
            ),
          IconButton(
            icon: const Icon(Icons.delete_outline, color: Colors.red),
            onPressed: () => _removeDevice(device),
          ),
        ],
      ),
    );
  }

  Widget _buildDiscoveredDeviceTile(DiscoveredDevice device) {
    final isConnecting = _connectingDeviceId == device.id;
    final isConnected = _connectedDeviceId == device.id;
    final isRegistered = _registeredDevices.any((d) => d.deviceAddress == device.id);

    return ListTile(
      leading: Container(
        padding: const EdgeInsets.all(10),
        decoration: BoxDecoration(
          color: Colors.grey.withOpacity(0.1),
          borderRadius: BorderRadius.circular(10),
        ),
        child: const Icon(Icons.bluetooth, color: Colors.blue),
      ),
      title: Text(device.name.isNotEmpty ? device.name : 'Unknown Device'),
      subtitle: Text('${device.rssi} dBm • ${device.id.substring(0, 8)}...'),
      trailing: isConnecting
          ? const SizedBox(
              width: 24, height: 24,
              child: CircularProgressIndicator(strokeWidth: 2),
            )
          : isConnected
              ? const Icon(Icons.check_circle, color: Colors.green)
              : isRegistered
                  ? const Text('Registered', style: TextStyle(color: Colors.grey))
                  : TextButton(
                      onPressed: () => _connectToDevice(device),
                      child: const Text('Connect'),
                    ),
    );
  }
}
