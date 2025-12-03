import 'package:flutter/material.dart';
import 'package:flutter_reactive_ble/flutter_reactive_ble.dart';
import 'package:provider/provider.dart';
import 'package:fl_chart/fl_chart.dart';
import '../services/ble_service.dart';
import '../services/storage_service.dart';
import '../models/measurement.dart';

class MeasurementScreen extends StatefulWidget {
  final DiscoveredDevice device;
  final String patientId;

  const MeasurementScreen({
    super.key,
    required this.device,
    required this.patientId,
  });

  @override
  State<MeasurementScreen> createState() => _MeasurementScreenState();
}

class _MeasurementScreenState extends State<MeasurementScreen> {
  final List<Measurement> _measurements = [];
  bool _isConnected = false;

  @override
  void initState() {
    super.initState();
    _connectAndSubscribe();
  }

  void _connectAndSubscribe() {
    final bleService = context.read<BleService>();
    final storageService = context.read<StorageService>();
    
    // Connect
    bleService.connectToDevice(widget.device.id).listen((state) {
      if (state.connectionState == DeviceConnectionState.connected) {
        setState(() {
          _isConnected = true;
        });

        // Subscribe (assuming consent is already granted for this flow)
        // In a real app, you'd check/request consent here or pass the consentId
        final consent = storageService.getLastConsent();
        if (consent != null && consent.granted) {
             bleService.subscribeToMeasurements(
              widget.device.id,
              widget.patientId,
              consent.id,
            ).listen((measurement) {
              setState(() {
                _measurements.add(measurement);
                if (_measurements.length > 20) {
                  _measurements.removeAt(0);
                }
              });
              storageService.saveMeasurement(measurement);
            });
        } else {
            ScaffoldMessenger.of(context).showSnackBar(
                const SnackBar(content: Text('Consent required to read data')),
            );
        }
      } else {
        setState(() {
          _isConnected = false;
        });
      }
    });
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text('Live: ${widget.device.name}'),
        actions: [
          Icon(
            _isConnected ? Icons.bluetooth_connected : Icons.bluetooth_disabled,
            color: _isConnected ? Colors.green : Colors.red,
          )
        ],
      ),
      body: Column(
        children: [
          Expanded(
            flex: 2,
            child: Padding(
              padding: const EdgeInsets.all(16.0),
              child: _measurements.isEmpty
                  ? const Center(child: Text('Waiting for data...'))
                  : LineChart(
                      LineChartData(
                        gridData: const FlGridData(show: true),
                        titlesData: const FlTitlesData(show: false),
                        borderData: FlBorderData(show: true),
                        lineBarsData: [
                          LineChartBarData(
                            spots: _measurements
                                .asMap()
                                .entries
                                .map((e) => FlSpot(
                                      e.key.toDouble(),
                                      e.value.value,
                                    ))
                                .toList(),
                            isCurved: true,
                            color: Colors.blue,
                            dotData: const FlDotData(show: false),
                          ),
                        ],
                      ),
                    ),
            ),
          ),
          Expanded(
            flex: 1,
            child: ListView.builder(
              itemCount: _measurements.length,
              itemBuilder: (context, index) {
                // Show latest first
                final m = _measurements[_measurements.length - 1 - index];
                return ListTile(
                  title: Text('${m.type}: ${m.value}'),
                  subtitle: Text(m.timestamp.toString()),
                );
              },
            ),
          ),
        ],
      ),
    );
  }
}
