import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../models/alert_model.dart';
import '../models/vital_sign_model.dart';
import '../themes/app_theme.dart';
import '../providers/auth_provider.dart';
import '../widgets/modern_card.dart';
import '../widgets/modern_badge.dart';
import 'package:google_fonts/google_fonts.dart';

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
        title: Text(
          'Alerts',
          style: GoogleFonts.inter(fontWeight: FontWeight.w600),
        ),
        backgroundColor: Colors.white,
        foregroundColor: Colors.black,
        elevation: 0,
      ),
      body: ListView.separated(
        padding: const EdgeInsets.all(20),
        itemCount: mockAlerts.length,
        separatorBuilder: (_, __) => const SizedBox(height: 16),
        itemBuilder: (context, index) {
          final alert = mockAlerts[index];
          return ModernCard(
            padding: const EdgeInsets.all(16),
            child: Row(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Container(
                  padding: const EdgeInsets.all(12),
                  decoration: BoxDecoration(
                    color: _levelColor(alert.level).withOpacity(0.1),
                    shape: BoxShape.circle,
                  ),
                  child: Icon(_levelIcon(alert.level), color: _levelColor(alert.level)),
                ),
                const SizedBox(width: 16),
                Expanded(
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Row(
                        mainAxisAlignment: MainAxisAlignment.spaceBetween,
                        children: [
                          Expanded(
                            child: Text(
                              alert.title,
                              style: theme.textTheme.titleMedium?.copyWith(
                                fontWeight: FontWeight.bold,
                              ),
                            ),
                          ),
                          Text(
                            _formatSince(alert.timeSinceCreated),
                            style: theme.textTheme.bodySmall?.copyWith(
                              color: Colors.grey[500],
                            ),
                          ),
                        ],
                      ),
                      const SizedBox(height: 4),
                      Text(
                        alert.description,
                        style: theme.textTheme.bodyMedium?.copyWith(
                          color: Colors.grey[700],
                        ),
                      ),
                      const SizedBox(height: 12),
                      Row(
                        children: [
                          ModernBadge(
                            text: alert.levelDisplayName.toUpperCase(),
                            color: _levelColor(alert.level),
                          ),
                          const SizedBox(width: 8),
                          Container(
                            padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
                            decoration: BoxDecoration(
                              color: Colors.grey[100],
                              borderRadius: BorderRadius.circular(4),
                            ),
                            child: Text(
                              'ID: ${alert.patientId}',
                              style: TextStyle(
                                fontSize: 12,
                                color: Colors.grey[600],
                                fontWeight: FontWeight.w500,
                              ),
                            ),
                          ),
                        ],
                      ),
                    ],
                  ),
                ),
              ],
            ),
          );
        },
      ),
    );
  }

  String _formatSince(Duration d) {
    if (d.inMinutes < 1) return 'Just now';
    if (d.inMinutes < 60) return '${d.inMinutes}m ago';
    final hours = d.inHours;
    return '${hours}h ago';
  }

  IconData _levelIcon(AlertLevel level) {
    switch (level) {
      case AlertLevel.low:
        return Icons.check_circle_outline;
      case AlertLevel.medium:
        return Icons.info_outline;
      case AlertLevel.high:
        return Icons.warning_amber_rounded;
      case AlertLevel.critical:
        return Icons.dangerous_outlined;
      case AlertLevel.none:
        return Icons.circle_notifications_outlined;
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
        return AppThemes.errorRed;
      case AlertLevel.none:
        return Colors.grey;
    }
  }
}


