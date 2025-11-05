import 'package:flutter/material.dart';
import '../models/alert_model.dart';
import '../models/vital_sign_model.dart';
import '../themes/app_theme.dart';

class AlertCard extends StatelessWidget {
  final Alert alert;
  final VoidCallback? onTap;
  const AlertCard({super.key, required this.alert, this.onTap});

  @override
  Widget build(BuildContext context) {
    final color = _levelColor(alert.level);
    return InkWell(
      onTap: onTap,
      borderRadius: BorderRadius.circular(14),
      child: AnimatedContainer(
        duration: const Duration(milliseconds: 200),
        curve: Curves.easeOut,
        decoration: BoxDecoration(
          color: Colors.white,
          borderRadius: BorderRadius.circular(14),
          boxShadow: [
            BoxShadow(color: Colors.black.withOpacity(0.04), blurRadius: 12, offset: const Offset(0, 6)),
          ],
          border: Border.all(color: color.withOpacity(0.15)),
        ),
        padding: const EdgeInsets.all(14),
        child: Row(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            CircleAvatar(backgroundColor: color.withOpacity(0.12), child: Icon(_levelIcon(alert.level), color: color)),
            const SizedBox(width: 12),
            Expanded(
              child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
                Text(alert.title, style: const TextStyle(fontWeight: FontWeight.w600)),
                const SizedBox(height: 6),
                Text(alert.description, maxLines: 2, overflow: TextOverflow.ellipsis, style: TextStyle(color: Colors.grey[700])),
                const SizedBox(height: 8),
                Row(children: [
                  Container(
                    padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
                    decoration: BoxDecoration(color: color.withOpacity(0.1), borderRadius: BorderRadius.circular(8)),
                    child: Text(alert.levelDisplayName, style: TextStyle(color: color, fontWeight: FontWeight.w600, fontSize: 12)),
                  ),
                  const SizedBox(width: 8),
                  Text('${alert.timeSinceCreated.inMinutes}m ago', style: TextStyle(color: Colors.grey[600], fontSize: 12)),
                ]),
              ]),
            )
          ],
        ),
      ),
    );
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
    return Colors.grey;
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
    return Icons.circle;
  }
}


