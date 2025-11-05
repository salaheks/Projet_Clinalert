import 'package:clinalert/models/vital_sign_model.dart';
import 'package:clinalert/themes/app_theme.dart';
import 'package:flutter/material.dart';
import 'package:font_awesome_flutter/font_awesome_flutter.dart';

extension VitalTypeExtension on VitalType {
  IconData get icon {
    switch (this) {
      case VitalType.heartRate:
        return FontAwesomeIcons.heartPulse;
      case VitalType.bloodPressure:
        return FontAwesomeIcons.stethoscope;
      case VitalType.bloodSugar:
        return FontAwesomeIcons.syringe;
      case VitalType.oxygenSaturation:
        return FontAwesomeIcons.percent;
      case VitalType.temperature:
        return FontAwesomeIcons.thermometerHalf;
    }
  }

  Color get color {
    switch (this) {
      case VitalType.heartRate:
        return AppThemes.primaryBlue;
      case VitalType.bloodPressure:
        return AppThemes.primaryGreen;
      case VitalType.bloodSugar:
        return AppThemes.warningOrange;
      case VitalType.oxygenSaturation:
        return Colors.blueAccent;
      case VitalType.temperature:
        return Colors.redAccent;
    }
  }

  String get displayName {
    switch (this) {
      case VitalType.heartRate:
        return 'Heart Rate';
      case VitalType.bloodPressure:
        return 'Blood Pressure';
      case VitalType.bloodSugar:
        return 'Blood Sugar';
      case VitalType.oxygenSaturation:
        return 'Oxygen Saturation';
      case VitalType.temperature:
        return 'Temperature';
    }
  }
}

extension AlertLevelExtension on AlertLevel {
  IconData get icon {
    switch (this) {
      case AlertLevel.low:
        return FontAwesomeIcons.checkCircle;
      case AlertLevel.medium:
        return FontAwesomeIcons.exclamationTriangle;
      case AlertLevel.high:
        return FontAwesomeIcons.exclamationCircle;
      case AlertLevel.critical:
        return FontAwesomeIcons.skullCrossbones;
      case AlertLevel.none:
        return Icons.circle;
    }
  }

  Color get color {
    switch (this) {
      case AlertLevel.low:
        return AppThemes.successGreen;
      case AlertLevel.medium:
        return AppThemes.warningOrange;
      case AlertLevel.high:
        return AppThemes.alertRed;
      case AlertLevel.critical:
        return Colors.purple;
      case AlertLevel.none:
        return Colors.transparent;
    }
  }
}