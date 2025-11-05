import 'package:flutter/material.dart';
import '../models/vital_sign_model.dart';
import '../themes/app_theme.dart';

class PatientHistoryScreen extends StatelessWidget {
  const PatientHistoryScreen({super.key});

  @override
  Widget build(BuildContext context) {
    final vitals = <VitalSign>[
      VitalSign(id: 'h1', patientId: 'P-003', type: VitalType.heartRate, value: 82, unit: 'bpm', timestamp: DateTime.now().subtract(const Duration(hours: 1))),
      VitalSign(id: 'h2', patientId: 'P-003', type: VitalType.bloodPressure, value: 125, unit: 'mmHg', timestamp: DateTime.now().subtract(const Duration(hours: 3))),
      VitalSign(id: 'h3', patientId: 'P-003', type: VitalType.temperature, value: 37.1, unit: '°C', timestamp: DateTime.now().subtract(const Duration(days: 1))),
      VitalSign(id: 'h4', patientId: 'P-003', type: VitalType.oxygenSaturation, value: 97, unit: '%', timestamp: DateTime.now().subtract(const Duration(days: 2))),
    ];

    return Scaffold(
      appBar: AppBar(
        title: const Text('Vital History'),
        backgroundColor: AppThemes.primaryBlue,
        foregroundColor: Colors.white,
      ),
      body: ListView.separated(
        padding: const EdgeInsets.all(16),
        itemCount: vitals.length,
        separatorBuilder: (_, __) => const SizedBox(height: 12),
        itemBuilder: (context, index) {
          final v = vitals[index];
          return Card(
            child: ListTile(
              leading: Icon(Icons.monitor_heart, color: AppThemes.primaryBlue),
              title: Text('${v.typeDisplayName} • ${v.displayValue}'),
              subtitle: Text(v.formattedTimestamp),
            ),
          );
        },
      ),
    );
  }
}


