class Doctor {
  final String id;
  final String name;
  final String specialty;
  final String email;
  final String phoneNumber;

  Doctor({
    required this.id,
    required this.name,
    required this.specialty,
    required this.email,
    required this.phoneNumber,
  });

  factory Doctor.fromJson(Map<String, dynamic> json) {
    return Doctor(
      id: json['id'] as String,
      name: json['name'] as String? ?? '',
      specialty: json['specialty'] as String? ?? '',
      email: json['email'] as String? ?? '',
      phoneNumber: json['phoneNumber'] as String? ?? '',
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'id': id,
      'name': name,
      'specialty': specialty,
      'email': email,
      'phoneNumber': phoneNumber,
    };
  }

  /// Get initials from doctor name (e.g., "Dr. Marie Curie" -> "MC")
  String getInitials() {
    final parts = name.trim().split(' ');
    if (parts.isEmpty) return '?';
    if (parts.length == 1) return parts[0][0].toUpperCase();
    
    // Skip "Dr." or "Dr" prefix if present
    final filteredParts = parts.where((p) => 
      p.toLowerCase() != 'dr.' && p.toLowerCase() != 'dr'
    ).toList();
    
    if (filteredParts.isEmpty) return '?';
    if (filteredParts.length == 1) {
      return filteredParts[0][0].toUpperCase();
    }
    
    return filteredParts[0][0].toUpperCase() + 
           filteredParts[filteredParts.length - 1][0].toUpperCase();
  }

  /// Get display name without "Dr." prefix if it exists
  String get displayName {
    if (name.toLowerCase().startsWith('dr.')) {
      return name.substring(3).trim();
    } else if (name.toLowerCase().startsWith('dr ')) {
      return name.substring(2).trim();
    }
    return name;
  }

  /// Copy with method for easy updates
  Doctor copyWith({
    String? id,
    String? name,
    String? specialty,
    String? email,
    String? phoneNumber,
  }) {
    return Doctor(
      id: id ?? this.id,
      name: name ?? this.name,
      specialty: specialty ?? this.specialty,
      email: email ?? this.email,
      phoneNumber: phoneNumber ?? this.phoneNumber,
    );
  }
}
