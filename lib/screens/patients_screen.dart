import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../models/patient_model.dart';
import '../models/user_model.dart';
import '../widgets/custom_app_bar.dart';
import '../widgets/patient_card.dart';
import '../providers/auth_provider.dart';

class PatientsScreen extends StatelessWidget {
  const PatientsScreen({super.key});

  @override
  Widget build(BuildContext context) {
    final authProvider = context.read<AuthProvider>();

    final patients = [
      Patient(
        id: '1',
        patientId: 'P-001',
        user: User(
          id: 'u1',
          firstName: 'John',
          lastName: 'Doe',
          email: 'john.doe@example.com',
          phone: '+1-555-0101',
          role: UserRole.patient,
          createdAt: DateTime(2024, 1, 1),
        ),
        dateOfBirth: DateTime(1985, 5, 20),
        bloodType: 'O+',
        status: PatientStatus.active,
        createdAt: DateTime.now(),
      ),
      Patient(
        id: '2',
        patientId: 'P-002',
        user: User(
          id: 'u2',
          firstName: 'Jane',
          lastName: 'Smith',
          email: 'jane.smith@example.com',
          phone: '+1-555-0102',
          role: UserRole.patient,
          createdAt: DateTime(2024, 1, 2),
        ),
        dateOfBirth: DateTime(1992, 8, 15),
        bloodType: 'A-',
        status: PatientStatus.transferred,
        createdAt: DateTime.now(),
      ),
      Patient(
        id: '3',
        patientId: 'P-003',
        user: User(
          id: 'u3',
          firstName: 'Peter',
          lastName: 'Jones',
          email: 'peter.jones@example.com',
          phone: '+1-555-0103',
          role: UserRole.patient,
          createdAt: DateTime(2024, 1, 3),
        ),
        dateOfBirth: DateTime(1978, 12, 1),
        bloodType: 'B+',
        status: PatientStatus.discharged,
        createdAt: DateTime.now(),
      ),
    ];

    return Scaffold(
      appBar: CustomAppBar(
        title: 'Patients',
        user: authProvider.currentUser,
        onLogout: () => authProvider.logout(),
      ),
      body: ListView.builder(
        padding: const EdgeInsets.fromLTRB(16, 16, 16, 24),
        itemCount: patients.length,
        itemBuilder: (context, index) {
          final patient = patients[index];
          return PatientCard(
            patient: patient,
            onTap: () {
              // Reuse existing detail screen if desired in future.
            },
          );
        },
      ),
    );
  }
}


