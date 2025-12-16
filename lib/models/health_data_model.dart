class HealthData {
  final String id;
  final String patientId;
  final String? deviceId;
  final int? heartRate;
  final int? steps;
  final int? sleepMinutes;
  final double? spO2;
  final int? bloodPressureSystolic;
  final int? bloodPressureDiastolic;
  final double? temperature;
  final int? caloriesBurned;
  final double? distanceMeters;
  final DateTime timestamp;
  final String? source;
  final DateTime? receivedAt;

  HealthData({
    required this.id,
    required this.patientId,
    this.deviceId,
    this.heartRate,
    this.steps,
    this.sleepMinutes,
    this.spO2,
    this.bloodPressureSystolic,
    this.bloodPressureDiastolic,
    this.temperature,
    this.caloriesBurned,
    this.distanceMeters,
    required this.timestamp,
    this.source,
    this.receivedAt,
  });

  factory HealthData.fromJson(Map<String, dynamic> json) {
    return HealthData(
      id: json['id'] ?? '',
      patientId: json['patientId'] ?? '',
      deviceId: json['deviceId'],
      heartRate: json['heartRate'],
      steps: json['steps'],
      sleepMinutes: json['sleepMinutes'],
      spO2: json['spO2']?.toDouble(),
      bloodPressureSystolic: json['bloodPressureSystolic'],
      bloodPressureDiastolic: json['bloodPressureDiastolic'],
      temperature: json['temperature']?.toDouble(),
      caloriesBurned: json['caloriesBurned'],
      distanceMeters: json['distanceMeters']?.toDouble(),
      timestamp: json['timestamp'] != null 
          ? DateTime.parse(json['timestamp']) 
          : DateTime.now(),
      source: json['source'],
      receivedAt: json['receivedAt'] != null 
          ? DateTime.parse(json['receivedAt']) 
          : null,
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'id': id,
      'patientId': patientId,
      'deviceId': deviceId,
      'heartRate': heartRate,
      'steps': steps,
      'sleepMinutes': sleepMinutes,
      'spO2': spO2,
      'bloodPressureSystolic': bloodPressureSystolic,
      'bloodPressureDiastolic': bloodPressureDiastolic,
      'temperature': temperature,
      'caloriesBurned': caloriesBurned,
      'distanceMeters': distanceMeters,
      'timestamp': timestamp.toIso8601String(),
      'source': source,
    };
  }

  // Helper methods
  String get bloodPressureDisplay {
    if (bloodPressureSystolic == null || bloodPressureDiastolic == null) {
      return '--/-- mmHg';
    }
    return '$bloodPressureSystolic/$bloodPressureDiastolic mmHg';
  }

  String get heartRateDisplay => heartRate != null ? '$heartRate bpm' : '-- bpm';
  String get spO2Display => spO2 != null ? '${spO2!.toStringAsFixed(0)}%' : '--%';
  String get stepsDisplay => steps != null ? '$steps' : '--';
  String get temperatureDisplay => temperature != null ? '${temperature!.toStringAsFixed(1)}°C' : '--°C';

  String get sleepDisplay {
    if (sleepMinutes == null) return '--h --m';
    final hours = sleepMinutes! ~/ 60;
    final minutes = sleepMinutes! % 60;
    return '${hours}h ${minutes}m';
  }

  String get distanceDisplay {
    if (distanceMeters == null) return '-- km';
    if (distanceMeters! >= 1000) {
      return '${(distanceMeters! / 1000).toStringAsFixed(2)} km';
    }
    return '${distanceMeters!.toStringAsFixed(0)} m';
  }

  String get caloriesDisplay => caloriesBurned != null ? '$caloriesBurned kcal' : '-- kcal';

  // Health status indicators
  bool get isHeartRateNormal => heartRate != null && heartRate! >= 60 && heartRate! <= 100;
  bool get isSpO2Normal => spO2 != null && spO2! >= 95;
  bool get isTemperatureNormal => temperature != null && temperature! >= 36.1 && temperature! <= 37.2;
  bool get isBloodPressureNormal => 
      bloodPressureSystolic != null && 
      bloodPressureDiastolic != null &&
      bloodPressureSystolic! <= 120 && 
      bloodPressureDiastolic! <= 80;
}
