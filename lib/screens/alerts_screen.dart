import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../models/alert_model.dart';
import '../models/vital_sign_model.dart';
import '../themes/app_theme.dart';
import '../providers/auth_provider.dart';

class AlertsScreen extends StatelessWidget {
  const AlertsScreen({super.key});

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final authProvider = context.watch<AuthProvider>();

    final mockAlerts = <Alert>[
      Alert(
        id: 'a1',
        patientId: 'P-001',
        title: 'High Blood Pressure',
        description: 'Systolic above 160 detected.',
        level: AlertLevel.high,
        relatedVitalType: VitalType.bloodPressure,
        relatedVitalValue: 165,
        createdAt: DateTime.now().subtract(const Duration(minutes: 8)),
      ),
      Alert(
        id: 'a2',
        patientId: 'P-003',
        title: 'Low Oxygen Saturation',
        description: 'SpO2 dropped below 92%.',
        level: AlertLevel.critical,
        relatedVitalType: VitalType.oxygenSaturation,
        relatedVitalValue: 89,
        createdAt: DateTime.now().subtract(const Duration(minutes: 2)),
      ),
      Alert(
        id: 'a3',
        patientId: 'P-002',
        title: 'Elevated Temperature',
        description: 'Temperature is slightly elevated.',
        level: AlertLevel.medium,
        relatedVitalType: VitalType.temperature,
        relatedVitalValue: 38.2,
        createdAt: DateTime.now().subtract(const Duration(hours: 1)),
      ),
    ];

    return Scaffold(
      appBar: AppBar(
        title: const Text('Alerts'),
        backgroundColor: AppThemes.primaryBlue,
        foregroundColor: Colors.white,
      ),
      body: ListView.separated(
        padding: const EdgeInsets.all(16),
        itemCount: mockAlerts.length,
        separatorBuilder: (_, __) => const SizedBox(height: 12),
        itemBuilder: (context, index) {
          final alert = mockAlerts[index];
          return Card(
            shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
            child: ListTile(
              leading: CircleAvatar(
                backgroundColor: _levelColor(alert.level).withOpacity(0.15),
                child: Icon(_levelIcon(alert.level), color: _levelColor(alert.level)),
              ),
              title: Text(alert.title, style: theme.textTheme.titleMedium?.copyWith(fontWeight: FontWeight.w600)),
              subtitle: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  const SizedBox(height: 4),
                  Text(alert.description),
                  const SizedBox(height: 6),
                  Text(
                    'Patient: ${alert.patientId} • ${alert.levelDisplayName}',
                    style: theme.textTheme.bodySmall?.copyWith(color: Colors.grey[600]),
                  ),
                ],
              ),
              trailing: Text(
                _formatSince(alert.timeSinceCreated),
                style: theme.textTheme.bodySmall,
              ),
              onTap: () {},
            ),
          );
        },
      ),
    );
  }

  String _formatSince(Duration d) {
    if (d.inMinutes < 1) return 'now';
    if (d.inMinutes < 60) return '${d.inMinutes}m ago';
    final hours = d.inHours;
    return '${hours}h ago';
  }

  IconData _levelIcon(AlertLevel level) {
    switch (level) {
      case AlertLevel.low:
        return Icons.check_circle;
      case AlertLevel.medium:
        return Icons.error_outline;
      case AlertLevel.high:
        return Icons.warning_amber_rounded;
      case AlertLevel.critical:
        return Icons.dangerous;
      case AlertLevel.none:
        return Icons.circle;
    }
  }

  Color _levelColor(AlertLevel level) {
    switch (level) {
      case AlertLevel.low:
        return AppThemes.successGreen;
      case AlertLevel.medium:
        return AppThemes.warningOrange;
      case AlertLevel.high:
        return Colors.deepOrange;
      case AlertLevel.critical:
        return AppThemes.alertRed;
      case AlertLevel.none:
        return Colors.grey;
    }
  }
}


