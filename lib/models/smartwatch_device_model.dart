class SmartWatchDevice {
  final String id;
  final String patientId;
  final String? deviceName;
  final String deviceAddress;
  final String? deviceType;
  final DateTime? lastConnected;
  final bool isActive;
  final DateTime? createdAt;

  SmartWatchDevice({
    required this.id,
    required this.patientId,
    this.deviceName,
    required this.deviceAddress,
    this.deviceType,
    this.lastConnected,
    this.isActive = true,
    this.createdAt,
  });

  factory SmartWatchDevice.fromJson(Map<String, dynamic> json) {
    return SmartWatchDevice(
      id: json['id'] ?? '',
      patientId: json['patientId'] ?? '',
      deviceName: json['deviceName'],
      deviceAddress: json['deviceAddress'] ?? '',
      deviceType: json['deviceType'],
      lastConnected: json['lastConnected'] != null 
          ? DateTime.parse(json['lastConnected']) 
          : null,
      isActive: json['isActive'] ?? true,
      createdAt: json['createdAt'] != null 
          ? DateTime.parse(json['createdAt']) 
          : null,
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'id': id,
      'patientId': patientId,
      'deviceName': deviceName,
      'deviceAddress': deviceAddress,
      'deviceType': deviceType,
      'lastConnected': lastConnected?.toIso8601String(),
      'isActive': isActive,
      'createdAt': createdAt?.toIso8601String(),
    };
  }

  SmartWatchDevice copyWith({
    String? id,
    String? patientId,
    String? deviceName,
    String? deviceAddress,
    String? deviceType,
    DateTime? lastConnected,
    bool? isActive,
    DateTime? createdAt,
  }) {
    return SmartWatchDevice(
      id: id ?? this.id,
      patientId: patientId ?? this.patientId,
      deviceName: deviceName ?? this.deviceName,
      deviceAddress: deviceAddress ?? this.deviceAddress,
      deviceType: deviceType ?? this.deviceType,
      lastConnected: lastConnected ?? this.lastConnected,
      isActive: isActive ?? this.isActive,
      createdAt: createdAt ?? this.createdAt,
    );
  }

  String get displayName => deviceName ?? 'Unknown Device';

  String get connectionStatus {
    if (!isActive) return 'Inactive';
    if (lastConnected == null) return 'Never Connected';
    final diff = DateTime.now().difference(lastConnected!);
    if (diff.inMinutes < 5) return 'Connected';
    if (diff.inHours < 1) return 'Last seen ${diff.inMinutes}m ago';
    if (diff.inDays < 1) return 'Last seen ${diff.inHours}h ago';
    return 'Last seen ${diff.inDays}d ago';
  }

  bool get isConnected {
    if (!isActive || lastConnected == null) return false;
    return DateTime.now().difference(lastConnected!).inMinutes < 5;
  }
}
