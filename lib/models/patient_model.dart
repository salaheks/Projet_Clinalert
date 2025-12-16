import 'package:flutter/foundation.dart';
import 'user_model.dart';

enum PatientStatus { active, discharged, transferred }

class Patient {
  final String id;
  final String patientId;
  final User user;
  final DateTime dateOfBirth;
  final String bloodType;
  final String? medicalRecordNumber;
  final String? emergencyContact;
  final String? emergencyPhone;
  final List<String>? allergies;
  final List<String>? medications;
  final String? primaryDoctorId;
  final String? clinicId;
  final List<String>? assignedNurseIds;
  final PatientStatus status;
  final DateTime createdAt;
  final DateTime? updatedAt;

  const Patient({
    required this.id,
    required this.patientId,
    required this.user,
    required this.dateOfBirth,
    required this.bloodType,
    this.medicalRecordNumber,
    this.emergencyContact,
    this.emergencyPhone,
    this.allergies,
    this.medications,
    this.primaryDoctorId,
    this.clinicId,
    this.assignedNurseIds,
    this.status = PatientStatus.active,
    required this.createdAt,
    this.updatedAt,
  });

  int get age {
    final now = DateTime.now();
    int age = now.year - dateOfBirth.year;
    if (now.month < dateOfBirth.month || 
        (now.month == dateOfBirth.month && now.day < dateOfBirth.day)) {
      age--;
    }
    return age;
  }

  String get fullName => user.fullName;
  String get firstName => user.firstName;
  String get lastName => user.lastName;
  String get email => user.email;
  String get phone => user.phone;
  String? get profileImage => user.profileImage;

  bool get hasAllergies => allergies != null && allergies!.isNotEmpty;
  bool get hasMedications => medications != null && medications!.isNotEmpty;

  Map<String, dynamic> toJson() {
    return {
      'id': id,
      'patientId': patientId,
      'user': user.toJson(),
      'dateOfBirth': dateOfBirth.toIso8601String(),
      'bloodType': bloodType,
      'medicalRecordNumber': medicalRecordNumber,
      'emergencyContact': emergencyContact,
      'emergencyPhone': emergencyPhone,
      'allergies': allergies,
      'medications': medications,
      'primaryDoctorId': primaryDoctorId,
      'assignedNurseIds': assignedNurseIds,
      'status': status.name,
      'createdAt': createdAt.toIso8601String(),
      'updatedAt': updatedAt?.toIso8601String(),
    };
  }

  factory Patient.fromJson(Map<String, dynamic> json) {
    return Patient(
      id: json['id'],
      patientId: json['patientId'],
      user: User.fromJson(json['user']),
      dateOfBirth: DateTime.parse(json['dateOfBirth']),
      bloodType: json['bloodType'],
      medicalRecordNumber: json['medicalRecordNumber'],
      emergencyContact: json['emergencyContact'],
      emergencyPhone: json['emergencyPhone'],
      allergies: json['allergies'] != null ? List<String>.from(json['allergies']) : null,
      medications: json['medications'] != null ? List<String>.from(json['medications']) : null,
      primaryDoctorId: json['primaryDoctorId'],
      assignedNurseIds: json['assignedNurseIds'] != null ? List<String>.from(json['assignedNurseIds']) : null,
      status: PatientStatus.values.firstWhere((e) => e.name == json['status']),
      createdAt: DateTime.parse(json['createdAt']),
      updatedAt: json['updatedAt'] != null ? DateTime.parse(json['updatedAt']) : null,
    );
  }
}