import 'package:clinalert/models/patient_model.dart';
import 'package:clinalert/services/api_service.dart';
import 'package:clinalert/widgets/custom_app_bar.dart';
import 'package:flutter/material.dart';

class AddEditPatientScreen extends StatefulWidget {
  final Patient? patient; // null = add mode, non-null = edit mode

  const AddEditPatientScreen({super.key, this.patient});

  @override
  State<AddEditPatientScreen> createState() => _AddEditPatientScreenState();
}

class _AddEditPatientScreenState extends State<AddEditPatientScreen> {
  final _formKey = GlobalKey<FormState>();
  final ApiService _apiService = ApiService();
  
  late TextEditingController _nameController;
  late TextEditingController _ageController;
  String _selectedGender = 'M';
  String _selectedStatus = 'active';
  bool _isLoading = false;

  @override
  void initState() {
    super.initState();
    _nameController = TextEditingController(text: widget.patient?.fullName ?? '');
    _ageController = TextEditingController(text: widget.patient?.age.toString() ?? '');
    
    if (widget.patient != null) {
      // Try to detect gender from user data or default to M
      final firstName = widget.patient!.user.firstName.toLowerCase();
      if (firstName.contains('marie') || firstName.contains('sophie') || 
          firstName.contains('anne') || firstName.contains('claire')) {
        _selectedGender = 'F';
      } else {
        _selectedGender = 'M';
      }
      _selectedStatus = widget.patient!.status.name;
    }
  }

  @override
  void dispose() {
    _nameController.dispose();
    _ageController.dispose();
    super.dispose();
  }

  bool get isEditMode => widget.patient != null;

  Future<void> _savePatient() async {
    if (!_formKey.currentState!.validate()) return;

    setState(() => _isLoading = true);

    try {
      final patientData = {
        'name': _nameController.text.trim(),
        'age': int.parse(_ageController.text.trim()),
        'gender': _selectedGender,
        'status': _selectedStatus,
        // Use existing doctorId or null (backend will handle it)
        if (widget.patient?.primaryDoctorId != null) 
          'doctorId': widget.patient!.primaryDoctorId,
      };

      if (isEditMode) {
        await _apiService.updatePatient(widget.patient!.id, patientData);
      } else {
        await _apiService.createPatient(patientData);
      }

      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(
            content: Text(isEditMode ? 'Patient modifié avec succès' : 'Patient ajouté avec succès'),
            backgroundColor: Colors.green,
          ),
        );
        Navigator.pop(context, true); // Return true to indicate success
      }
    } catch (e) {
      setState(() => _isLoading = false);
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(
            content: Text('Erreur: $e'),
            backgroundColor: Colors.red,
          ),
        );
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: CustomAppBar(
        title: isEditMode ? 'Modifier patient' : 'Ajouter patient',
      ),
      body: SingleChildScrollView(
        padding: const EdgeInsets.all(20),
        child: Form(
          key: _formKey,
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.stretch,
            children: [
              // Name field
              TextFormField(
                controller: _nameController,
                decoration: InputDecoration(
                  labelText: 'Nom complet',
                  prefixIcon: const Icon(Icons.person),
                  border: OutlineInputBorder(
                    borderRadius: BorderRadius.circular(12),
                  ),
                ),
                validator: (value) {
                  if (value == null || value.trim().isEmpty) {
                    return 'Veuillez entrer le nom';
                  }
                  return null;
                },
              ),
              const SizedBox(height: 16),

              // Age field
              TextFormField(
                controller: _ageController,
                decoration: InputDecoration(
                  labelText: 'Âge',
                  prefixIcon: const Icon(Icons.cake),
                  border: OutlineInputBorder(
                    borderRadius: BorderRadius.circular(12),
                  ),
                ),
                keyboardType: TextInputType.number,
                validator: (value) {
                  if (value == null || value.trim().isEmpty) {
                    return 'Veuillez entrer l\'âge';
                  }
                  final age = int.tryParse(value);
                  if (age == null || age < 0 || age > 150) {
                    return 'Âge invalide';
                  }
                  return null;
                },
              ),
              const SizedBox(height: 16),

              // Gender dropdown
              DropdownButtonFormField<String>(
                value: _selectedGender,
                decoration: InputDecoration(
                  labelText: 'Genre',
                  prefixIcon: const Icon(Icons.wc),
                  border: OutlineInputBorder(
                    borderRadius: BorderRadius.circular(12),
                  ),
                ),
                items: const [
                  DropdownMenuItem(value: 'M', child: Text('Masculin')),
                  DropdownMenuItem(value: 'F', child: Text('Féminin')),
                ],
                onChanged: (value) {
                  if (value != null) {
                    setState(() => _selectedGender = value);
                  }
                },
              ),
              const SizedBox(height: 16),

              // Status dropdown
              DropdownButtonFormField<String>(
                value: _selectedStatus,
                decoration: InputDecoration(
                  labelText: 'Statut',
                  prefixIcon: const Icon(Icons.health_and_safety),
                  border: OutlineInputBorder(
                    borderRadius: BorderRadius.circular(12),
                  ),
                ),
                items: const [
                  DropdownMenuItem(value: 'active', child: Text('Actif')),
                  DropdownMenuItem(value: 'transferred', child: Text('Transféré')),
                  DropdownMenuItem(value: 'discharged', child: Text('Sorti')),
                ],
                onChanged: (value) {
                  if (value != null) {
                    setState(() => _selectedStatus = value);
                  }
                },
              ),
              const SizedBox(height: 32),

              // Save button
              ElevatedButton(
                onPressed: _isLoading ? null : _savePatient,
                style: ElevatedButton.styleFrom(
                  backgroundColor: const Color(0xFF0066FF),
                  foregroundColor: Colors.white,
                  padding: const EdgeInsets.symmetric(vertical: 16),
                  shape: RoundedRectangleBorder(
                    borderRadius: BorderRadius.circular(12),
                  ),
                ),
                child: _isLoading
                    ? const SizedBox(
                        height: 20,
                        width: 20,
                        child: CircularProgressIndicator(
                          strokeWidth: 2,
                          valueColor: AlwaysStoppedAnimation<Color>(Colors.white),
                        ),
                      )
                    : Text(
                        isEditMode ? 'Modifier' : 'Ajouter',
                        style: const TextStyle(fontSize: 16, fontWeight: FontWeight.bold),
                      ),
              ),
            ],
          ),
        ),
      ),
    );
  }
}
