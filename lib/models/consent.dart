import 'package:hive/hive.dart';

part 'consent.g.dart';

@HiveType(typeId: 1)
class Consent {
  @HiveField(0)
  final String id;

  @HiveField(1)
  final String patientId;

  @HiveField(2)
  final DateTime timestamp;

  @HiveField(3)
  final bool granted;

  @HiveField(4)
  final List<String> permissions; // e.g., ['read_ble', 'send_data']

  Consent({
    required this.id,
    required this.patientId,
    required this.timestamp,
    required this.granted,
    required this.permissions,
  });
}
