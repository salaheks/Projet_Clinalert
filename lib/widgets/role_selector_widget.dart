import 'package:clinalert/models/user_model.dart';
import 'package:clinalert/providers/auth_provider.dart';
import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

class RoleSelectorWidget extends StatefulWidget {
  const RoleSelectorWidget({super.key});

  @override
  State<RoleSelectorWidget> createState() => _RoleSelectorWidgetState();
}

class _RoleSelectorWidgetState extends State<RoleSelectorWidget> {
  final List<Map<String, dynamic>> _availableDoctors = [
    {'email': 'house@clinalert.com', 'name': 'Dr. Gregory'},
    {'email': 'cameron@clinalert.com', 'name': 'Dr. Allison'},
  ];

  String? _selectedDoctorEmail;

  @override
  Widget build(BuildContext context) {
    final authProvider = Provider.of<AuthProvider>(context);

    return Card(
      margin: const EdgeInsets.all(16),
      elevation: 4,
      shape: RoundedRectangleBorder(
        borderRadius: BorderRadius.circular(12),
      ),
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          mainAxisSize: MainAxisSize.min,
          children: [
            Row(
              children: [
                const Icon(Icons.admin_panel_settings, color: Colors.purple),
                const SizedBox(width: 8),
                const Text(
                  'Sélection du Rôle (Test)',
                  style: TextStyle(
                    fontSize: 18,
                    fontWeight: FontWeight.bold,
                  ),
                ),
              ],
            ),
            const SizedBox(height: 16),
            const Text(
              'Choisissez un rôle pour tester le contrôle d\'accès :',
              style: TextStyle(fontSize: 14, color: Colors.grey),
            ),
            const SizedBox(height: 16),
            
            // Admin Button
            SizedBox(
              width: double.infinity,
              child: ElevatedButton.icon(
                onPressed: () {
                  authProvider.simulateLogin(role: UserRole.admin);
                  setState(() => _selectedDoctorEmail = null);
                },
                icon: const Icon(Icons.admin_panel_settings),
                label: const Text('Mode ADMIN (Voir tout)'),
                style: ElevatedButton.styleFrom(
                  backgroundColor: authProvider.isAdmin ? Colors.purple : Colors.grey[300],
                  foregroundColor: authProvider.isAdmin ? Colors.white : Colors.black87,
                  padding: const EdgeInsets.symmetric(vertical: 12),
                ),
              ),
            ),
            
            const SizedBox(height: 12),
            
            // Doctor Selection
            Container(
              padding: const EdgeInsets.all(12),
              decoration: BoxDecoration(
                color: Colors.blue.withOpacity(0.1),
                borderRadius: BorderRadius.circular(8),
                border: Border.all(
                  color: authProvider.isDoctor ? Colors.blue : Colors.grey[300]!,
                  width: 2,
                ),
              ),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  const Text(
                    'Mode DOCTOR (Voir uniquement ses patients)',
                    style: TextStyle(
                      fontWeight: FontWeight.bold,
                      fontSize: 14,
                    ),
                  ),
                  const SizedBox(height: 8),
                  DropdownButtonFormField<String>(
                    value: _selectedDoctorEmail,
                    decoration: const InputDecoration(
                      labelText: 'Sélectionner un médecin',
                      border: OutlineInputBorder(),
                      contentPadding: EdgeInsets.symmetric(horizontal: 12, vertical: 8),
                    ),
                    items: _availableDoctors.map((doctor) {
                      return DropdownMenuItem<String>(
                        value: doctor['email'] as String,
                        child: Text(doctor['name'] as String),
                      );
                    }).toList(),
                    onChanged: (email) {
                      if (email != null) {
                        setState(() => _selectedDoctorEmail = email);
                        authProvider.simulateLogin(
                          role: UserRole.doctor,
                          doctorEmail: email,
                        );
                      }
                    },
                  ),
                ],
              ),
            ),
            
            const SizedBox(height: 16),
            
            // Current Status
            if (authProvider.currentUser != null)
              Container(
                padding: const EdgeInsets.all(12),
                decoration: BoxDecoration(
                  color: Colors.green.withOpacity(0.1),
                  borderRadius: BorderRadius.circular(8),
                ),
                child: Row(
                  children: [
                    const Icon(Icons.check_circle, color: Colors.green, size: 20),
                    const SizedBox(width: 8),
                    Expanded(
                      child: Text(
                        'Connecté en tant que: ${authProvider.currentUser!.roleDisplayName}${authProvider.isDoctor && authProvider.currentDoctorEmail != null ? ' (${authProvider.currentDoctorEmail})' : ''}',
                        style: const TextStyle(
                          fontSize: 12,
                          fontWeight: FontWeight.w500,
                        ),
                      ),
                    ),
                  ],
                ),
              ),
          ],
        ),
      ),
    );
  }
}
