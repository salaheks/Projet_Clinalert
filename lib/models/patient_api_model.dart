import 'patient_model.dart';
import 'user_model.dart';

/// Simplified Patient model matching the backend API structure
class PatientApiModel {
  final String id;
  final String name;
  final int age;
  final String gender;
  final String? doctorId;
  final String? clinicId;
  final String? status;

  const PatientApiModel({
    required this.id,
    required this.name,
    required this.age,
    required this.gender,
    this.doctorId,
    this.clinicId,
    this.status,
  });

  /// Convert from JSON (backend response)
  factory PatientApiModel.fromJson(Map<String, dynamic> json) {
    return PatientApiModel(
      id: json['id'] as String,
      name: json['name'] as String,
      age: json['age'] as int,
      gender: json['gender'] as String,
      doctorId: json['doctorId'] as String?,
      clinicId: json['clinicId'] as String?,
      status: json['status'] as String?,
    );
  }

  /// Convert to JSON (for API requests)
  Map<String, dynamic> toJson() {
    return {
      'id': id,
      'name': name,
      'age': age,
      'gender': gender,
      'doctorId': doctorId,
    };
  }

  /// Convert to UI Patient model for display
  Patient toPatient() {
    // Split name into first and last name - handle empty or single names safely
    final nameParts = name.trim().split(' ').where((part) => part.isNotEmpty).toList();
    final firstName = nameParts.isNotEmpty ? nameParts[0] : 'Unknown';
    final lastName = nameParts.length > 1 ? nameParts.sublist(1).join(' ') : '';

    // Calculate approximate date of birth from age
    final now = DateTime.now();
    final approximateBirthYear = now.year - age;
    final dateOfBirth = DateTime(approximateBirthYear, 1, 1);

    // Create a User object with available data
    final user = User(
      id: id,
      firstName: firstName,
      lastName: lastName,
      email: '${firstName.toLowerCase()}.${lastName.toLowerCase()}@clinalert.com',
      phone: '+33-XXX-XXX-XXX',
      role: UserRole.patient,
      createdAt: DateTime.now(),
    );

    // Convert status string to PatientStatus enum
    PatientStatus patientStatus = PatientStatus.active;
    if (status != null) {
      switch (status!.toLowerCase()) {
        case 'active':
          patientStatus = PatientStatus.active;
          break;
        case 'discharged':
          patientStatus = PatientStatus.discharged;
          break;
        case 'transferred':
          patientStatus = PatientStatus.transferred;
          break;
      }
    }

    return Patient(
      id: id,
      patientId: 'P-${id.substring(0, 8)}',
      user: user,
      dateOfBirth: dateOfBirth,
      bloodType: 'Unknown', // Not available from backend
      status: patientStatus,
      createdAt: DateTime.now(),
      primaryDoctorId: doctorId,
      clinicId: clinicId,
    );
  }
}
