import 'package:hive/hive.dart';

part 'measurement.g.dart';

@HiveType(typeId: 0)
class Measurement {
  @HiveField(0)
  final String id;

  @HiveField(1)
  final String patientId;

  @HiveField(2)
  final String deviceId;

  @HiveField(3)
  final String type; // e.g., 'steps', 'battery', 'heart_rate'

  @HiveField(4)
  final double value;

  @HiveField(5)
  final DateTime timestamp;

  @HiveField(6)
  final String? consentId;

  @HiveField(7)
  bool isSynced;

  Measurement({
    required this.id,
    required this.patientId,
    required this.deviceId,
    required this.type,
    required this.value,
    required this.timestamp,
    this.consentId,
    this.isSynced = false,
  });

  Map<String, dynamic> toJson() {
    return {
      'id': id,
      'patientId': patientId,
      'deviceId': deviceId,
      'type': type,
      'value': value,
      'timestamp': timestamp.toIso8601String(),
      'consentId': consentId,
    };
  }
}
