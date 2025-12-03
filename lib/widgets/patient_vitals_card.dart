import 'package:clinalert/models/vital_sign_model.dart';
import 'package:clinalert/themes/app_theme.dart';
import 'package:clinalert/utils/enum_extensions.dart';
import 'package:flutter/material.dart';
import 'package:font_awesome_flutter/font_awesome_flutter.dart';

class PatientVitalsCard extends StatelessWidget {
  final VitalSign vitalSign;

  const PatientVitalsCard({super.key, required this.vitalSign});

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final alertLevel = vitalSign.alertLevel;

    return Card(
      elevation: 2,
      shape: RoundedRectangleBorder(
        borderRadius: BorderRadius.circular(12),
        side: BorderSide(
          color: alertLevel != AlertLevel.none
              ? alertLevel.color.withAlpha(179)
              : Colors.transparent,
          width: 1.5,
        ),
      ),
      child: Padding(
        padding: const EdgeInsets.all(16.0),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Row(
              children: [
                Icon(
                  vitalSign.type.icon,
                  size: 24,
                  color: vitalSign.type.color,
                ),
                const SizedBox(width: 12),
                Text(
                  vitalSign.type.displayName,
                  style: theme.textTheme.titleMedium?.copyWith(
                    fontWeight: FontWeight.bold,
                  ),
                ),
                const Spacer(),
                if (alertLevel != AlertLevel.none)
                  Icon(
                    alertLevel.icon,
                    color: alertLevel.color,
                    size: 20,
                  ),
              ],
            ),
            const SizedBox(height: 12),
            Row(
              children: [
                Expanded(
                  child: FittedBox(
                    alignment: Alignment.centerLeft,
                    fit: BoxFit.scaleDown,
                    child: Text(
                      '\${vitalSign.value} \${vitalSign.unit}',
                      maxLines: 1,
                      overflow: TextOverflow.ellipsis,
                      style: theme.textTheme.headlineSmall?.copyWith(
                        fontWeight: FontWeight.bold,
                        color: vitalSign.type.color,
                      ),
                    ),
                  ),
                ),
                const SizedBox(width: 8),
                Flexible(
                  child: Text(
                    vitalSign.formattedTimestamp,
                    maxLines: 1,
                    overflow: TextOverflow.ellipsis,
                    textAlign: TextAlign.right,
                    style: theme.textTheme.bodySmall,
                  ),
                ),
              ],
            ),
            if (vitalSign.isAbnormal)
              Padding(
                padding: const EdgeInsets.only(top: 8.0),
                child: Text(
                  'Abnormal value detected',
                  style: theme.textTheme.bodySmall?.copyWith(
                    color: alertLevel.color,
                    fontWeight: FontWeight.bold,
                  ),
                ),
              ),
          ],
        ),
      ),
    );
  }
}