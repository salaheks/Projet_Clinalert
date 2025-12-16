import 'package:clinalert/models/clinic_model.dart';
import 'package:clinalert/models/doctor_model.dart';
import 'package:clinalert/services/api_service.dart';
import 'package:clinalert/providers/auth_provider.dart';
import 'package:flutter/material.dart';
import 'package:google_fonts/google_fonts.dart';
import 'package:provider/provider.dart';

class AddEditClinicScreen extends StatefulWidget {
  final Clinic? clinic;
  
  const AddEditClinicScreen({super.key, this.clinic});

  @override
  State<AddEditClinicScreen> createState() => _AddEditClinicScreenState();
}

class _AddEditClinicScreenState extends State<AddEditClinicScreen> {
  final _formKey = GlobalKey<FormState>();
  final ApiService _apiService = ApiService();
  
  late TextEditingController _nameController;
  late TextEditingController _addressController;
  late TextEditingController _phoneController;
  
  String? _selectedDoctorId;
  List<Doctor> _doctors = [];
  bool _isLoading = false;
  bool _isLoadingDoctors = true;

  bool get isEditing => widget.clinic != null;

  @override
  void initState() {
    super.initState();
    _nameController = TextEditingController(text: widget.clinic?.name ?? '');
    _addressController = TextEditingController(text: widget.clinic?.address ?? '');
    _phoneController = TextEditingController(text: widget.clinic?.phone ?? '');
    _selectedDoctorId = widget.clinic?.doctorId;
    _loadDoctors();
  }

  @override
  void dispose() {
    _nameController.dispose();
    _addressController.dispose();
    _phoneController.dispose();
    super.dispose();
  }

  Future<void> _loadDoctors() async {
    try {
      final doctorsData = await _apiService.getAllDoctors();
      setState(() {
        _doctors = doctorsData.map((json) => Doctor.fromJson(json)).toList();
        _isLoadingDoctors = false;
      });
    } catch (e) {
      setState(() => _isLoadingDoctors = false);
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(content: Text('Erreur de chargement des médecins: $e')),
        );
      }
    }
  }

  Future<void> _saveClinic() async {
    if (!_formKey.currentState!.validate()) return;

    setState(() => _isLoading = true);

    try {
      final authProvider = Provider.of<AuthProvider>(context, listen: false);
      final isAdmin = authProvider.isAdmin;
      
      // For non-admin users, use their own ID as doctorId
      final doctorIdToUse = isAdmin ? _selectedDoctorId : authProvider.currentUser?.id;
      
      final clinicData = {
        'name': _nameController.text.trim(),
        'address': _addressController.text.trim(),
        'phone': _phoneController.text.trim(),
        'doctorId': doctorIdToUse,
      };

      if (isEditing) {
        await _apiService.updateClinic(widget.clinic!.id, clinicData);
      } else {
        await _apiService.createClinic(clinicData);
      }

      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(
            content: Text(isEditing ? 'Clinique modifiée avec succès' : 'Clinique créée avec succès'),
            backgroundColor: Colors.green,
          ),
        );
        Navigator.pop(context, true);
      }
    } catch (e) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(
            content: Text('Erreur: $e'),
            backgroundColor: Colors.red,
          ),
        );
      }
    } finally {
      if (mounted) {
        setState(() => _isLoading = false);
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: const Color(0xFFF8FAFC),
      appBar: AppBar(
        title: Text(
          isEditing ? 'Modifier la clinique' : 'Nouvelle clinique',
          style: GoogleFonts.inter(fontWeight: FontWeight.bold),
        ),
        centerTitle: true,
        backgroundColor: Colors.transparent,
        elevation: 0,
        foregroundColor: Colors.black87,
      ),
      body: SingleChildScrollView(
        padding: const EdgeInsets.all(20),
        child: Form(
          key: _formKey,
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              // Clinic Icon Header
              Center(
                child: Container(
                  width: 80,
                  height: 80,
                  decoration: BoxDecoration(
                    gradient: const LinearGradient(
                      colors: [Color(0xFF0066FF), Color(0xFF00C4B4)],
                      begin: Alignment.topLeft,
                      end: Alignment.bottomRight,
                    ),
                    shape: BoxShape.circle,
                    boxShadow: [
                      BoxShadow(
                        color: const Color(0xFF0066FF).withOpacity(0.3),
                        blurRadius: 20,
                        offset: const Offset(0, 10),
                      ),
                    ],
                  ),
                  child: const Icon(
                    Icons.local_hospital,
                    color: Colors.white,
                    size: 40,
                  ),
                ),
              ),
              
              const SizedBox(height: 32),
              
              // Name Field
              Text(
                'Nom de la clinique *',
                style: GoogleFonts.inter(
                  fontWeight: FontWeight.w600,
                  color: Colors.black87,
                ),
              ),
              const SizedBox(height: 8),
              TextFormField(
                controller: _nameController,
                decoration: InputDecoration(
                  hintText: 'Ex: Clinique du Parc',
                  prefixIcon: const Icon(Icons.local_hospital_outlined),
                  filled: true,
                  fillColor: Colors.white,
                  border: OutlineInputBorder(
                    borderRadius: BorderRadius.circular(12),
                    borderSide: BorderSide(color: Colors.grey.shade300),
                  ),
                  enabledBorder: OutlineInputBorder(
                    borderRadius: BorderRadius.circular(12),
                    borderSide: BorderSide(color: Colors.grey.shade300),
                  ),
                  focusedBorder: OutlineInputBorder(
                    borderRadius: BorderRadius.circular(12),
                    borderSide: const BorderSide(color: Color(0xFF0066FF), width: 2),
                  ),
                ),
                validator: (value) {
                  if (value == null || value.trim().isEmpty) {
                    return 'Le nom est requis';
                  }
                  return null;
                },
              ),
              
              const SizedBox(height: 20),
              
              // Address Field
              Text(
                'Adresse',
                style: GoogleFonts.inter(
                  fontWeight: FontWeight.w600,
                  color: Colors.black87,
                ),
              ),
              const SizedBox(height: 8),
              TextFormField(
                controller: _addressController,
                decoration: InputDecoration(
                  hintText: 'Ex: 123 Rue de la Santé, Paris',
                  prefixIcon: const Icon(Icons.location_on_outlined),
                  filled: true,
                  fillColor: Colors.white,
                  border: OutlineInputBorder(
                    borderRadius: BorderRadius.circular(12),
                    borderSide: BorderSide(color: Colors.grey.shade300),
                  ),
                  enabledBorder: OutlineInputBorder(
                    borderRadius: BorderRadius.circular(12),
                    borderSide: BorderSide(color: Colors.grey.shade300),
                  ),
                  focusedBorder: OutlineInputBorder(
                    borderRadius: BorderRadius.circular(12),
                    borderSide: const BorderSide(color: Color(0xFF0066FF), width: 2),
                  ),
                ),
                maxLines: 2,
              ),
              
              const SizedBox(height: 20),
              
              // Phone Field
              Text(
                'Téléphone',
                style: GoogleFonts.inter(
                  fontWeight: FontWeight.w600,
                  color: Colors.black87,
                ),
              ),
              const SizedBox(height: 8),
              TextFormField(
                controller: _phoneController,
                keyboardType: TextInputType.phone,
                decoration: InputDecoration(
                  hintText: 'Ex: +33 1 23 45 67 89',
                  prefixIcon: const Icon(Icons.phone_outlined),
                  filled: true,
                  fillColor: Colors.white,
                  border: OutlineInputBorder(
                    borderRadius: BorderRadius.circular(12),
                    borderSide: BorderSide(color: Colors.grey.shade300),
                  ),
                  enabledBorder: OutlineInputBorder(
                    borderRadius: BorderRadius.circular(12),
                    borderSide: BorderSide(color: Colors.grey.shade300),
                  ),
                  focusedBorder: OutlineInputBorder(
                    borderRadius: BorderRadius.circular(12),
                    borderSide: const BorderSide(color: Color(0xFF0066FF), width: 2),
                  ),
                ),
              ),
              
              const SizedBox(height: 20),
              
              // Doctor Selection - Only show for Admin
              Builder(
                builder: (context) {
                  final authProvider = Provider.of<AuthProvider>(context, listen: false);
                  final isAdmin = authProvider.isAdmin;
                  
                  if (!isAdmin) {
                    // Non-admin: Show info that clinic will be assigned to them
                    return Container(
                      padding: const EdgeInsets.all(16),
                      decoration: BoxDecoration(
                        color: Colors.blue.withOpacity(0.1),
                        borderRadius: BorderRadius.circular(12),
                        border: Border.all(color: Colors.blue.withOpacity(0.3)),
                      ),
                      child: Row(
                        children: [
                          const Icon(Icons.info_outline, color: Colors.blue),
                          const SizedBox(width: 12),
                          Expanded(
                            child: Text(
                              'Cette clinique sera automatiquement associée à votre compte.',
                              style: GoogleFonts.inter(
                                color: Colors.blue.shade700,
                                fontSize: 14,
                              ),
                            ),
                          ),
                        ],
                      ),
                    );
                  }
                  
                  // Admin: Show doctor selection dropdown
                  return Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Text(
                        'Médecin responsable',
                        style: GoogleFonts.inter(
                          fontWeight: FontWeight.w600,
                          color: Colors.black87,
                        ),
                      ),
                      const SizedBox(height: 8),
                      _isLoadingDoctors
                          ? const Center(child: CircularProgressIndicator())
                          : DropdownButtonFormField<String>(
                              value: _selectedDoctorId,
                              decoration: InputDecoration(
                                hintText: 'Sélectionner un médecin',
                                prefixIcon: const Icon(Icons.medical_services_outlined),
                                filled: true,
                                fillColor: Colors.white,
                                border: OutlineInputBorder(
                                  borderRadius: BorderRadius.circular(12),
                                  borderSide: BorderSide(color: Colors.grey.shade300),
                                ),
                                enabledBorder: OutlineInputBorder(
                                  borderRadius: BorderRadius.circular(12),
                                  borderSide: BorderSide(color: Colors.grey.shade300),
                                ),
                                focusedBorder: OutlineInputBorder(
                                  borderRadius: BorderRadius.circular(12),
                                  borderSide: const BorderSide(color: Color(0xFF0066FF), width: 2),
                                ),
                              ),
                              items: [
                                const DropdownMenuItem<String>(
                                  value: null,
                                  child: Text('Aucun médecin'),
                                ),
                                ..._doctors.map((doctor) {
                                  return DropdownMenuItem<String>(
                                    value: doctor.id,
                                    child: Text('${doctor.name} - ${doctor.specialty}'),
                                  );
                                }),
                              ],
                              onChanged: (value) {
                                setState(() => _selectedDoctorId = value);
                              },
                            ),
                    ],
                  );
                },
              ),
              
              const SizedBox(height: 40),
              
              // Save Button
              SizedBox(
                width: double.infinity,
                child: ElevatedButton(
                  onPressed: _isLoading ? null : _saveClinic,
                  style: ElevatedButton.styleFrom(
                    backgroundColor: const Color(0xFF0066FF),
                    foregroundColor: Colors.white,
                    padding: const EdgeInsets.symmetric(vertical: 16),
                    shape: RoundedRectangleBorder(
                      borderRadius: BorderRadius.circular(12),
                    ),
                    elevation: 0,
                  ),
                  child: _isLoading
                      ? const SizedBox(
                          width: 24,
                          height: 24,
                          child: CircularProgressIndicator(
                            color: Colors.white,
                            strokeWidth: 2,
                          ),
                        )
                      : Text(
                          isEditing ? 'Enregistrer les modifications' : 'Créer la clinique',
                          style: GoogleFonts.inter(
                            fontSize: 16,
                            fontWeight: FontWeight.w600,
                          ),
                        ),
                ),
              ),
              
              const SizedBox(height: 16),
              
              // Cancel Button
              SizedBox(
                width: double.infinity,
                child: OutlinedButton(
                  onPressed: () => Navigator.pop(context),
                  style: OutlinedButton.styleFrom(
                    padding: const EdgeInsets.symmetric(vertical: 16),
                    side: BorderSide(color: Colors.grey.shade400),
                    shape: RoundedRectangleBorder(
                      borderRadius: BorderRadius.circular(12),
                    ),
                  ),
                  child: Text(
                    'Annuler',
                    style: GoogleFonts.inter(
                      fontSize: 16,
                      fontWeight: FontWeight.w600,
                      color: Colors.grey.shade700,
                    ),
                  ),
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }
}
