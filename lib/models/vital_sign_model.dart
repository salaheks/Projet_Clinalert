import 'package:flutter/foundation.dart';

enum VitalType {
  heartRate,
  bloodPressure,
  bloodSugar,
  oxygenSaturation,
  temperature
}

enum AlertLevel { none, low, medium, high, critical }

enum AlertStatus { active, acknowledged, resolved }

class VitalSign {
  final String id;
  final String patientId;
  final VitalType type;
  final double value;
  final String unit;
  final DateTime timestamp;
  final String? notes;
  final String? nurseId;

  const VitalSign({
    required this.id,
    required this.patientId,
    required this.type,
    required this.value,
    required this.unit,
    required this.timestamp,
    this.notes,
    this.nurseId,
  });

  String get formattedTimestamp {
    // Format the timestamp to a more readable format
    // Example: '10:30 AM, 24 Jun 2024'
    return '${timestamp.hour}:${timestamp.minute} ${timestamp.hour < 12 ? 'AM' : 'PM'}, ${timestamp.day} ${['', 'Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'][timestamp.month]} ${timestamp.year}';
  }

  String get typeDisplayName {
    switch (type) {
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

  String get displayValue => '$value $unit';

  bool get isAbnormal {
    switch (type) {
      case VitalType.heartRate:
        return value < 60 || value > 100;
      case VitalType.bloodPressure:
        return value > 140; // Systolic
      case VitalType.bloodSugar:
        return value < 70 || value > 180;
      case VitalType.oxygenSaturation:
        return value < 95;
      case VitalType.temperature:
        return value < 97.0 || value > 99.5;
    }
  }

  AlertLevel get alertLevel {
    if (!isAbnormal) return AlertLevel.low;
    
    switch (type) {
      case VitalType.heartRate:
        if (value < 40 || value > 150) return AlertLevel.critical;
        if (value < 50 || value > 120) return AlertLevel.high;
        return AlertLevel.medium;
      case VitalType.bloodPressure:
        if (value > 180) return AlertLevel.critical;
        if (value > 160) return AlertLevel.high;
        return AlertLevel.medium;
      case VitalType.bloodSugar:
        if (value < 50 || value > 300) return AlertLevel.critical;
        if (value < 70 || value > 250) return AlertLevel.high;
        return AlertLevel.medium;
      case VitalType.oxygenSaturation:
        if (value < 90) return AlertLevel.critical;
        if (value < 92) return AlertLevel.high;
        return AlertLevel.medium;
      case VitalType.temperature:
        if (value < 95.0 || value > 104.0) return AlertLevel.critical;
        if (value < 96.0 || value > 101.0) return AlertLevel.high;
        return AlertLevel.medium;
    }
  }

  Map<String, dynamic> toJson() {
    return {
      'id': id,
      'patientId': patientId,
      'type': type.name,
      'value': value,
      'unit': unit,
      'timestamp': timestamp.toIso8601String(),
      'notes': notes,
      'nurseId': nurseId,
    };
  }

  factory VitalSign.fromJson(Map<String, dynamic> json) {
    return VitalSign(
      id: json['id'],
      patientId: json['patientId'],
      type: VitalType.values.firstWhere((e) => e.name == json['type']),
      value: json['value'].toDouble(),
      unit: json['unit'],
      timestamp: DateTime.parse(json['timestamp']),
      notes: json['notes'],
      nurseId: json['nurseId'],
    );
  }
}