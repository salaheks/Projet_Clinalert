class DailyHealthSummary {
  final String id;
  final String patientId;
  final DateTime date;
  
  // Heart Rate Stats
  final double? avgHeartRate;
  final int? minHeartRate;
  final int? maxHeartRate;
  final int? restingHeartRate;
  
  // Steps & Activity
  final int? totalSteps;
  final double? totalDistanceMeters;
  final int? totalCaloriesBurned;
  final int? activeMinutes;
  
  // Sleep Stats
  final int? totalSleepMinutes;
  final int? deepSleepMinutes;
  final int? lightSleepMinutes;
  final int? remSleepMinutes;
  
  // SpO2 Stats
  final double? avgSpO2;
  final double? minSpO2;
  
  // Blood Pressure Stats
  final double? avgSystolic;
  final double? avgDiastolic;
  
  // Temperature Stats
  final double? avgTemperature;
  
  final int? dataPointsCount;
  final DateTime? createdAt;
  final DateTime? updatedAt;

  DailyHealthSummary({
    required this.id,
    required this.patientId,
    required this.date,
    this.avgHeartRate,
    this.minHeartRate,
    this.maxHeartRate,
    this.restingHeartRate,
    this.totalSteps,
    this.totalDistanceMeters,
    this.totalCaloriesBurned,
    this.activeMinutes,
    this.totalSleepMinutes,
    this.deepSleepMinutes,
    this.lightSleepMinutes,
    this.remSleepMinutes,
    this.avgSpO2,
    this.minSpO2,
    this.avgSystolic,
    this.avgDiastolic,
    this.avgTemperature,
    this.dataPointsCount,
    this.createdAt,
    this.updatedAt,
  });

  factory DailyHealthSummary.fromJson(Map<String, dynamic> json) {
    return DailyHealthSummary(
      id: json['id'] ?? '',
      patientId: json['patientId'] ?? '',
      date: json['date'] != null 
          ? DateTime.parse(json['date']) 
          : DateTime.now(),
      avgHeartRate: json['avgHeartRate']?.toDouble(),
      minHeartRate: json['minHeartRate'],
      maxHeartRate: json['maxHeartRate'],
      restingHeartRate: json['restingHeartRate'],
      totalSteps: json['totalSteps'],
      totalDistanceMeters: json['totalDistanceMeters']?.toDouble(),
      totalCaloriesBurned: json['totalCaloriesBurned'],
      activeMinutes: json['activeMinutes'],
      totalSleepMinutes: json['totalSleepMinutes'],
      deepSleepMinutes: json['deepSleepMinutes'],
      lightSleepMinutes: json['lightSleepMinutes'],
      remSleepMinutes: json['remSleepMinutes'],
      avgSpO2: json['avgSpO2']?.toDouble(),
      minSpO2: json['minSpO2']?.toDouble(),
      avgSystolic: json['avgSystolic']?.toDouble(),
      avgDiastolic: json['avgDiastolic']?.toDouble(),
      avgTemperature: json['avgTemperature']?.toDouble(),
      dataPointsCount: json['dataPointsCount'],
      createdAt: json['createdAt'] != null 
          ? DateTime.parse(json['createdAt']) 
          : null,
      updatedAt: json['updatedAt'] != null 
          ? DateTime.parse(json['updatedAt']) 
          : null,
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'id': id,
      'patientId': patientId,
      'date': date.toIso8601String().split('T')[0],
      'avgHeartRate': avgHeartRate,
      'minHeartRate': minHeartRate,
      'maxHeartRate': maxHeartRate,
      'restingHeartRate': restingHeartRate,
      'totalSteps': totalSteps,
      'totalDistanceMeters': totalDistanceMeters,
      'totalCaloriesBurned': totalCaloriesBurned,
      'activeMinutes': activeMinutes,
      'totalSleepMinutes': totalSleepMinutes,
      'deepSleepMinutes': deepSleepMinutes,
      'lightSleepMinutes': lightSleepMinutes,
      'remSleepMinutes': remSleepMinutes,
      'avgSpO2': avgSpO2,
      'minSpO2': minSpO2,
      'avgSystolic': avgSystolic,
      'avgDiastolic': avgDiastolic,
      'avgTemperature': avgTemperature,
      'dataPointsCount': dataPointsCount,
    };
  }

  // Display helpers
  String get dateFormatted {
    final months = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 
                    'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'];
    return '${date.day} ${months[date.month - 1]} ${date.year}';
  }

  String get heartRateRangeDisplay {
    if (minHeartRate == null || maxHeartRate == null) return '-- - -- bpm';
    return '$minHeartRate - $maxHeartRate bpm';
  }

  String get avgHeartRateDisplay => avgHeartRate != null 
      ? '${avgHeartRate!.toStringAsFixed(0)} bpm' 
      : '-- bpm';

  String get stepsDisplay => totalSteps != null 
      ? '$totalSteps steps' 
      : '-- steps';

  String get distanceDisplay {
    if (totalDistanceMeters == null) return '-- km';
    if (totalDistanceMeters! >= 1000) {
      return '${(totalDistanceMeters! / 1000).toStringAsFixed(2)} km';
    }
    return '${totalDistanceMeters!.toStringAsFixed(0)} m';
  }

  String get sleepDisplay {
    if (totalSleepMinutes == null) return '--h --m';
    final hours = totalSleepMinutes! ~/ 60;
    final minutes = totalSleepMinutes! % 60;
    return '${hours}h ${minutes}m';
  }

  String get caloriesDisplay => totalCaloriesBurned != null 
      ? '$totalCaloriesBurned kcal' 
      : '-- kcal';

  String get avgSpO2Display => avgSpO2 != null 
      ? '${avgSpO2!.toStringAsFixed(1)}%' 
      : '--%';

  String get bloodPressureDisplay {
    if (avgSystolic == null || avgDiastolic == null) return '--/-- mmHg';
    return '${avgSystolic!.toStringAsFixed(0)}/${avgDiastolic!.toStringAsFixed(0)} mmHg';
  }

  String get temperatureDisplay => avgTemperature != null 
      ? '${avgTemperature!.toStringAsFixed(1)}°C' 
      : '--°C';

  // Step goal progress (assuming 10000 steps goal)
  double get stepsProgress {
    if (totalSteps == null) return 0;
    return (totalSteps! / 10000).clamp(0.0, 1.0);
  }

  // Sleep goal progress (assuming 8 hours = 480 minutes goal)
  double get sleepProgress {
    if (totalSleepMinutes == null) return 0;
    return (totalSleepMinutes! / 480).clamp(0.0, 1.0);
  }
}
