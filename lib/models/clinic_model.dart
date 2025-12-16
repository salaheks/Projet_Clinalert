class Clinic {
  final String id;
  final String name;
  final String address;
  final String phone;
  final String? doctorId;

  Clinic({
    required this.id,
    required this.name,
    required this.address,
    required this.phone,
    this.doctorId,
  });

  factory Clinic.fromJson(Map<String, dynamic> json) {
    return Clinic(
      id: json['id'] ?? '',
      name: json['name'] ?? '',
      address: json['address'] ?? '',
      phone: json['phone'] ?? '',
      doctorId: json['doctorId'],
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'id': id,
      'name': name,
      'address': address,
      'phone': phone,
      'doctorId': doctorId,
    };
  }

  /// Get initials for avatar display
  String getInitials() {
    final words = name.split(' ');
    if (words.length >= 2) {
      return '${words[0][0]}${words[1][0]}'.toUpperCase();
    } else if (words.isNotEmpty && words[0].isNotEmpty) {
      return words[0].substring(0, words[0].length >= 2 ? 2 : 1).toUpperCase();
    }
    return 'CL';
  }

  Clinic copyWith({
    String? id,
    String? name,
    String? address,
    String? phone,
    String? doctorId,
  }) {
    return Clinic(
      id: id ?? this.id,
      name: name ?? this.name,
      address: address ?? this.address,
      phone: phone ?? this.phone,
      doctorId: doctorId ?? this.doctorId,
    );
  }

  @override
  bool operator ==(Object other) {
    if (identical(this, other)) return true;
    return other is Clinic && other.id == id;
  }

  @override
  int get hashCode => id.hashCode;
}
