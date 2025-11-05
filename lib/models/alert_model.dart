import 'package:flutter/foundation.dart';
import 'user_model.dart';
import 'vital_sign_model.dart';

class Alert {
  final String id;
  final String patientId;
  final String? doctorId;
  final String? nurseId;
  final String title;
  final String description;
  final AlertLevel level;
  final AlertStatus status;
  final VitalType? relatedVitalType;
  final double? relatedVitalValue;
  final DateTime createdAt;
  final DateTime? acknowledgedAt;
  final DateTime? resolvedAt;
  final String? acknowledgedBy;
  final String? resolvedBy;

  const Alert({
    required this.id,
    required this.patientId,
    this.doctorId,
    this.nurseId,
    required this.title,
    required this.description,
    required this.level,
    this.status = AlertStatus.active,
    this.relatedVitalType,
    this.relatedVitalValue,
    required this.createdAt,
    this.acknowledgedAt,
    this.resolvedAt,
    this.acknowledgedBy,
    this.resolvedBy,
  });

  String get levelDisplayName {
    switch (level) {
      case AlertLevel.none:
        return 'None';
      case AlertLevel.low:
        return 'Low';
      case AlertLevel.medium:
        return 'Medium';
      case AlertLevel.high:
        return 'High';
      case AlertLevel.critical:
        return 'Critical';
    }
  }

  String get statusDisplayName {
    switch (status) {
      case AlertStatus.active:
        return 'Active';
      case AlertStatus.acknowledged:
        return 'Acknowledged';
      case AlertStatus.resolved:
        return 'Resolved';
    }
  }

  bool get isActive => status == AlertStatus.active;
  bool get isAcknowledged => status == AlertStatus.acknowledged;
  bool get isResolved => status == AlertStatus.resolved;

  Duration get timeSinceCreated => DateTime.now().difference(createdAt);

  Map<String, dynamic> toJson() {
    return {
      'id': id,
      'patientId': patientId,
      'doctorId': doctorId,
      'nurseId': nurseId,
      'title': title,
      'description': description,
      'level': level.name,
      'status': status.name,
      'relatedVitalType': relatedVitalType?.name,
      'relatedVitalValue': relatedVitalValue,
      'createdAt': createdAt.toIso8601String(),
      'acknowledgedAt': acknowledgedAt?.toIso8601String(),
      'resolvedAt': resolvedAt?.toIso8601String(),
      'acknowledgedBy': acknowledgedBy,
      'resolvedBy': resolvedBy,
    };
  }

  factory Alert.fromJson(Map<String, dynamic> json) {
    return Alert(
      id: json['id'],
      patientId: json['patientId'],
      doctorId: json['doctorId'],
      nurseId: json['nurseId'],
      title: json['title'],
      description: json['description'],
      level: AlertLevel.values.firstWhere((e) => e.name == json['level']),
      status: AlertStatus.values.firstWhere((e) => e.name == json['status']),
      relatedVitalType: json['relatedVitalType'] != null 
          ? VitalType.values.firstWhere((e) => e.name == json['relatedVitalType'])
          : null,
      relatedVitalValue: json['relatedVitalValue']?.toDouble(),
      createdAt: DateTime.parse(json['createdAt']),
      acknowledgedAt: json['acknowledgedAt'] != null ? DateTime.parse(json['acknowledgedAt']) : null,
      resolvedAt: json['resolvedAt'] != null ? DateTime.parse(json['resolvedAt']) : null,
      acknowledgedBy: json['acknowledgedBy'],
      resolvedBy: json['resolvedBy'],
    );
  }
}