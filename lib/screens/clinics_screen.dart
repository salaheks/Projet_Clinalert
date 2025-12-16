import 'package:clinalert/models/patient_model.dart';
import 'package:clinalert/models/patient_api_model.dart';
import 'package:clinalert/models/doctor_model.dart';
import 'package:clinalert/models/clinic_model.dart';
import 'package:clinalert/services/api_service.dart';
import 'package:clinalert/screens/patient_detail_screen.dart';
import 'package:clinalert/screens/add_edit_patient_screen.dart';
import 'package:clinalert/screens/add_edit_clinic_screen.dart';
import 'package:clinalert/providers/auth_provider.dart';
import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:google_fonts/google_fonts.dart';

class ClinicsScreen extends StatefulWidget {
  const ClinicsScreen({super.key});

  @override
  State<ClinicsScreen> createState() => _ClinicsScreenState();
}

class _ClinicsScreenState extends State<ClinicsScreen> {
  final ApiService _apiService = ApiService();
  final TextEditingController _searchController = TextEditingController();
  
  List<Clinic> _allClinics = [];
  List<Clinic> _filteredClinics = [];
  Map<String, Doctor> _doctorsMap = {};
  Map<String, List<Patient>> _clinicPatients = {};
  List<Patient> _unassignedPatients = [];
  bool _isLoading = true;
  String? _errorMessage;

  @override
  void initState() {
    super.initState();
    _loadData();
    _searchController.addListener(_filterClinics);
  }

  @override
  void dispose() {
    _searchController.dispose();
    super.dispose();
  }

  Future<void> _loadData() async {
    setState(() {
      _isLoading = true;
      _errorMessage = null;
    });

    try {
      final authProvider = Provider.of<AuthProvider>(context, listen: false);
      final currentUser = authProvider.currentUser;
      final isAdmin = authProvider.isAdmin;
      
      // Fetch clinics based on user role
      List<Map<String, dynamic>> clinicsData;
      if (isAdmin) {
        // Admin sees all clinics
        clinicsData = await _apiService.getAllClinics();
      } else {
        // Doctor sees only their clinics
        final userId = currentUser?.id ?? '';
        clinicsData = await _apiService.getClinicsByDoctorId(userId);
      }
      
      // Fetch doctors and patients
      final doctorsData = await _apiService.getAllDoctors();
      final patientsData = await _apiService.getAllPatients();

      // Parse data
      final clinics = clinicsData.map((json) => Clinic.fromJson(json)).toList();
      final doctors = doctorsData.map((json) => Doctor.fromJson(json)).toList();
      final patients = patientsData
          .map((json) => PatientApiModel.fromJson(json))
          .map((api) => api.toPatient())
          .toList();

      // Create doctors map for quick lookup
      final Map<String, Doctor> doctorsMap = {
        for (var doctor in doctors) doctor.id: doctor
      };

      // Get clinic IDs for the current user (for filtering patients)
      final Set<String> userClinicIds = clinics.map((c) => c.id).toSet();

      // Group patients by clinic ID - only include patients from user's clinics
      final Map<String, List<Patient>> patientsByClinic = {};
      final List<Patient> unassignedPatients = [];
      
      for (var patient in patients) {
        // Group patients by their clinicId
        final clinicId = patient.clinicId;
        if (clinicId != null && clinicId.isNotEmpty) {
          // Only include patients from user's clinics (or all if admin)
          if (isAdmin || userClinicIds.contains(clinicId)) {
            if (!patientsByClinic.containsKey(clinicId)) {
              patientsByClinic[clinicId] = [];
            }
            patientsByClinic[clinicId]!.add(patient);
          }
        } else {
          // Only show unassigned patients to admin
          if (isAdmin) {
            unassignedPatients.add(patient);
          }
        }
      }

      setState(() {
        _allClinics = clinics;
        _filteredClinics = clinics;
        _doctorsMap = doctorsMap;
        _clinicPatients = patientsByClinic;
        _unassignedPatients = unassignedPatients;
        _isLoading = false;
      });
    } catch (e) {
      setState(() {
        _errorMessage = 'Erreur de chargement: $e';
        _isLoading = false;
      });
    }
  }

  void _filterClinics() {
    final query = _searchController.text.toLowerCase();
    setState(() {
      if (query.isEmpty) {
        _filteredClinics = _allClinics;
      } else {
        _filteredClinics = _allClinics.where((clinic) {
          final nameMatch = clinic.name.toLowerCase().contains(query);
          final addressMatch = clinic.address.toLowerCase().contains(query);
          
          // Check doctor name
          final doctor = _doctorsMap[clinic.doctorId];
          final doctorMatch = doctor?.name.toLowerCase().contains(query) ?? false;
          
          // Also check if any patient of this clinic matches
          final patients = _clinicPatients[clinic.id] ?? [];
          final hasMatchingPatient = patients.any((patient) =>
              patient.fullName.toLowerCase().contains(query));
          
          return nameMatch || addressMatch || doctorMatch || hasMatchingPatient;
        }).toList();
      }
    });
  }

  Future<void> _assignPatientToClinic(Patient patient) async {
    final selectedClinic = await showDialog<String>(
      context: context,
      builder: (context) => AlertDialog(
        title: const Text('Assigner à une clinique'),
        content: Column(
          mainAxisSize: MainAxisSize.min,
          children: _allClinics.map((clinic) {
            final patientCount = _clinicPatients[clinic.id]?.length ?? 0;
            final doctor = _doctorsMap[clinic.doctorId];
            return RadioListTile<String>(
              title: Text(clinic.name),
              subtitle: Text('${doctor?.name ?? 'Aucun médecin'} • $patientCount patients'),
              value: clinic.id,
              groupValue: null,
              onChanged: (value) => Navigator.pop(context, value),
            );
          }).toList(),
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(context),
            child: const Text('Annuler'),
          ),
        ],
      ),
    );

    if (selectedClinic != null) {
      try {
        await _apiService.updatePatient(patient.id, {
          'name': patient.fullName,
          'age': patient.age,
          'gender': patient.user.firstName.toLowerCase().contains('marie') ? 'F' : 'M',
          'status': patient.status.name,
          'clinicId': selectedClinic,
        });

        if (mounted) {
          ScaffoldMessenger.of(context).showSnackBar(
            const SnackBar(
              content: Text('Patient assigné avec succès'),
              backgroundColor: Colors.green,
            ),
          );
          _loadData();
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
      }
    }
  }

  Future<void> _deletePatient(Patient patient) async {
    final confirmed = await showDialog<bool>(
      context: context,
      builder: (context) => AlertDialog(
        title: const Text('Supprimer le patient'),
        content: Text('Voulez-vous vraiment supprimer ${patient.fullName} ?'),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(context, false),
            child: const Text('Annuler'),
          ),
          TextButton(
            onPressed: () => Navigator.pop(context, true),
            style: TextButton.styleFrom(foregroundColor: Colors.red),
            child: const Text('Supprimer'),
          ),
        ],
      ),
    );

    if (confirmed == true) {
      try {
        await _apiService.deletePatient(patient.id);
        if (mounted) {
          ScaffoldMessenger.of(context).showSnackBar(
            const SnackBar(
              content: Text('Patient supprimé avec succès'),
              backgroundColor: Colors.green,
            ),
          );
          _loadData();
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
      }
    }
  }

  Future<void> _transferPatientToClinic(Patient patient, String? currentClinicId) async {
    final selectedClinic = await showDialog<String>(
      context: context,
      builder: (context) => AlertDialog(
        title: const Text('Transférer vers une clinique'),
        content: SingleChildScrollView(
          child: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              // Option pour retirer de la clinique
              if (currentClinicId != null)
                RadioListTile<String>(
                  title: const Text('Retirer de la clinique'),
                  subtitle: const Text('Le patient sera non assigné'),
                  value: '__REMOVE__',
                  groupValue: null,
                  onChanged: (value) => Navigator.pop(context, value),
                ),
              if (currentClinicId != null) const Divider(),
              // Liste des cliniques
              ..._allClinics.where((c) => c.id != currentClinicId).map((clinic) {
                final patientCount = _clinicPatients[clinic.id]?.length ?? 0;
                final doctor = _doctorsMap[clinic.doctorId];
                return RadioListTile<String>(
                  title: Text(clinic.name),
                  subtitle: Text('${doctor?.name ?? 'Aucun médecin'} • $patientCount patients'),
                  value: clinic.id,
                  groupValue: null,
                  onChanged: (value) => Navigator.pop(context, value),
                );
              }),
            ],
          ),
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(context),
            child: const Text('Annuler'),
          ),
        ],
      ),
    );

    if (selectedClinic != null) {
      try {
        final newClinicId = selectedClinic == '__REMOVE__' ? null : selectedClinic;
        await _apiService.updatePatient(patient.id, {
          'name': patient.fullName,
          'age': patient.age,
          'gender': patient.user.firstName.toLowerCase().contains('marie') ? 'F' : 'M',
          'status': patient.status.name,
          'clinicId': newClinicId,
          'doctorId': patient.primaryDoctorId,
        });

        if (mounted) {
          ScaffoldMessenger.of(context).showSnackBar(
            SnackBar(
              content: Text(newClinicId == null 
                  ? 'Patient retiré de la clinique' 
                  : 'Patient transféré avec succès'),
              backgroundColor: Colors.green,
            ),
          );
          _loadData();
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
      }
    }
  }

  Future<void> _editClinic(Clinic clinic) async {
    final result = await Navigator.push(
      context,
      MaterialPageRoute(
        builder: (context) => AddEditClinicScreen(clinic: clinic),
      ),
    );
    if (result == true) {
      _loadData();
    }
  }

  Future<void> _deleteClinic(Clinic clinic) async {
    final patientCount = _clinicPatients[clinic.id]?.length ?? 0;
    
    final confirmed = await showDialog<bool>(
      context: context,
      builder: (context) => AlertDialog(
        title: const Text('Supprimer la clinique'),
        content: Text(
          patientCount > 0
              ? 'Cette clinique a $patientCount patient(s). Voulez-vous vraiment la supprimer ?'
              : 'Voulez-vous vraiment supprimer ${clinic.name} ?',
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(context, false),
            child: const Text('Annuler'),
          ),
          TextButton(
            onPressed: () => Navigator.pop(context, true),
            style: TextButton.styleFrom(foregroundColor: Colors.red),
            child: const Text('Supprimer'),
          ),
        ],
      ),
    );

    if (confirmed == true) {
      try {
        await _apiService.deleteClinic(clinic.id);
        if (mounted) {
          ScaffoldMessenger.of(context).showSnackBar(
            const SnackBar(
              content: Text('Clinique supprimée avec succès'),
              backgroundColor: Colors.green,
            ),
          );
          _loadData();
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
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    final authProvider = Provider.of<AuthProvider>(context);
    
    return Scaffold(
      backgroundColor: const Color(0xFFF8FAFC),
      appBar: AppBar(
        title: Row(
          children: [
            Text(
              'Cliniques et leurs patients',
              style: GoogleFonts.inter(fontWeight: FontWeight.bold),
            ),
            const SizedBox(width: 12),
            Container(
              padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
              decoration: BoxDecoration(
                color: authProvider.isAdmin ? Colors.purple : Colors.blue,
                borderRadius: BorderRadius.circular(12),
              ),
              child: Text(
                authProvider.isAdmin ? 'ADMIN' : 'DOCTOR',
                style: const TextStyle(
                  color: Colors.white,
                  fontSize: 12,
                  fontWeight: FontWeight.bold,
                ),
              ),
            ),
          ],
        ),
        backgroundColor: Colors.transparent,
        elevation: 0,
        foregroundColor: Colors.black87,
        bottom: PreferredSize(
          preferredSize: const Size.fromHeight(60),
          child: Padding(
            padding: const EdgeInsets.all(12),
            child: TextField(
              controller: _searchController,
              decoration: InputDecoration(
                hintText: 'Rechercher une clinique ou patient...',
                prefixIcon: const Icon(Icons.search),
                suffixIcon: _searchController.text.isNotEmpty
                    ? IconButton(
                        icon: const Icon(Icons.clear),
                        onPressed: () => _searchController.clear(),
                      )
                    : null,
                filled: true,
                fillColor: Colors.white,
                border: OutlineInputBorder(
                  borderRadius: BorderRadius.circular(12),
                  borderSide: BorderSide.none,
                ),
              ),
            ),
          ),
        ),
        actions: [
          IconButton(
            icon: const Icon(Icons.refresh),
            onPressed: _loadData,
          ),
        ],
      ),
      body: _isLoading
          ? const Center(child: CircularProgressIndicator())
          : _errorMessage != null
              ? Center(
                  child: Column(
                    mainAxisAlignment: MainAxisAlignment.center,
                    children: [
                      const Icon(Icons.error_outline, size: 64, color: Colors.red),
                      const SizedBox(height: 16),
                      Text(_errorMessage!),
                      const SizedBox(height: 16),
                      ElevatedButton(
                        onPressed: _loadData,
                        child: const Text('Réessayer'),
                      ),
                    ],
                  ),
                )
              : _filteredClinics.isEmpty
                  ? Center(
                      child: Column(
                        mainAxisAlignment: MainAxisAlignment.center,
                        children: [
                          Icon(Icons.local_hospital_outlined, size: 64, color: Colors.grey.shade400),
                          const SizedBox(height: 16),
                          Text(
                            _searchController.text.isEmpty
                                ? 'Aucune clinique trouvée'
                                : 'Aucun résultat pour "${_searchController.text}"',
                            style: TextStyle(fontSize: 16, color: Colors.grey.shade600),
                          ),
                          const SizedBox(height: 24),
                          ElevatedButton.icon(
                            onPressed: () async {
                              final result = await Navigator.push(
                                context,
                                MaterialPageRoute(
                                  builder: (context) => const AddEditClinicScreen(),
                                ),
                              );
                              if (result == true) _loadData();
                            },
                            icon: const Icon(Icons.add),
                            label: const Text('Créer une clinique'),
                          ),
                        ],
                      ),
                    )
                  : RefreshIndicator(
                      onRefresh: _loadData,
                      child: ListView.builder(
                        padding: const EdgeInsets.all(16),
                        itemCount: _filteredClinics.length + (_unassignedPatients.isNotEmpty ? 1 : 0),
                        itemBuilder: (context, index) {
                          // Show unassigned patients section first
                          if (_unassignedPatients.isNotEmpty && index == 0) {
                            return _buildUnassignedPatientsCard();
                          }
                          
                          // Adjust index for clinics list
                          final clinicIndex = _unassignedPatients.isNotEmpty ? index - 1 : index;
                          final clinic = _filteredClinics[clinicIndex];
                          final patients = _clinicPatients[clinic.id] ?? [];
                          final doctor = _doctorsMap[clinic.doctorId];

                          return _buildClinicCard(clinic, patients, doctor);
                        },
                      ),
                    ),
      floatingActionButton: FloatingActionButton.extended(
        onPressed: () async {
          final result = await Navigator.push(
            context,
            MaterialPageRoute(
              builder: (context) => const AddEditClinicScreen(),
            ),
          );
          if (result == true) {
            _loadData();
          }
        },
        icon: const Icon(Icons.add),
        label: const Text('Nouvelle clinique'),
        backgroundColor: const Color(0xFF0066FF),
      ),
    );
  }

  Widget _buildUnassignedPatientsCard() {
    return Card(
      margin: const EdgeInsets.only(bottom: 16),
      elevation: 2,
      shape: RoundedRectangleBorder(
        borderRadius: BorderRadius.circular(12),
      ),
      child: Column(
        children: [
          ListTile(
            leading: CircleAvatar(
              backgroundColor: Colors.orange.withOpacity(0.1),
              child: const Icon(
                Icons.person_off,
                color: Colors.orange,
              ),
            ),
            title: const Text(
              'Patients non assignés',
              style: TextStyle(
                fontWeight: FontWeight.bold,
                fontSize: 16,
              ),
            ),
            subtitle: Text(
              'Patients sans clinique attribuée',
              style: TextStyle(
                fontSize: 14,
                color: Colors.grey[600],
              ),
            ),
          ),
          const Divider(height: 1),
          ExpansionTile(
            initiallyExpanded: true,
            title: Text(
              '${_unassignedPatients.length} patient${_unassignedPatients.length > 1 ? 's' : ''}',
              style: TextStyle(
                color: Colors.grey[700],
                fontSize: 14,
              ),
            ),
            children: _unassignedPatients.map((patient) => ListTile(
              leading: CircleAvatar(
                radius: 20,
                backgroundColor: _getStatusColor(patient.status).withOpacity(0.1),
                child: Text(
                  _getInitials(patient),
                  style: TextStyle(
                    color: _getStatusColor(patient.status),
                    fontWeight: FontWeight.bold,
                    fontSize: 12,
                  ),
                ),
              ),
              title: Text(patient.fullName),
              subtitle: Text('${patient.age} ans • ${_getStatusLabel(patient.status)}'),
              trailing: PopupMenuButton<String>(
                icon: const Icon(Icons.more_vert),
                onSelected: (value) {
                  if (value == 'assign') {
                    _assignPatientToClinic(patient);
                  } else if (value == 'delete') {
                    _deletePatient(patient);
                  }
                },
                itemBuilder: (context) => [
                  const PopupMenuItem(
                    value: 'assign',
                    child: Row(
                      children: [
                        Icon(Icons.add_circle_outline, size: 20, color: Colors.blue),
                        SizedBox(width: 8),
                        Text('Assigner à une clinique'),
                      ],
                    ),
                  ),
                  const PopupMenuItem(
                    value: 'delete',
                    child: Row(
                      children: [
                        Icon(Icons.delete, size: 20, color: Colors.red),
                        SizedBox(width: 8),
                        Text('Supprimer', style: TextStyle(color: Colors.red)),
                      ],
                    ),
                  ),
                ],
              ),
            )).toList(),
          ),
        ],
      ),
    );
  }

  Widget _buildClinicCard(Clinic clinic, List<Patient> patients, Doctor? doctor) {
    return Card(
      margin: const EdgeInsets.only(bottom: 16),
      elevation: 2,
      shape: RoundedRectangleBorder(
        borderRadius: BorderRadius.circular(12),
      ),
      child: Column(
        children: [
          ListTile(
            leading: Container(
              width: 48,
              height: 48,
              decoration: BoxDecoration(
                gradient: const LinearGradient(
                  colors: [Color(0xFF0066FF), Color(0xFF00C4B4)],
                  begin: Alignment.topLeft,
                  end: Alignment.bottomRight,
                ),
                borderRadius: BorderRadius.circular(12),
              ),
              child: Center(
                child: Text(
                  clinic.getInitials(),
                  style: const TextStyle(
                    color: Colors.white,
                    fontWeight: FontWeight.bold,
                  ),
                ),
              ),
            ),
            title: Text(
              clinic.name,
              style: const TextStyle(
                fontWeight: FontWeight.bold,
                fontSize: 16,
              ),
            ),
            subtitle: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                if (doctor != null)
                  Row(
                    children: [
                      const Icon(Icons.medical_services, size: 14, color: Colors.grey),
                      const SizedBox(width: 4),
                      Text(
                        doctor.name,
                        style: TextStyle(color: Colors.grey.shade700),
                      ),
                    ],
                  ),
                if (clinic.address.isNotEmpty)
                  Row(
                    children: [
                      const Icon(Icons.location_on, size: 14, color: Colors.grey),
                      const SizedBox(width: 4),
                      Expanded(
                        child: Text(
                          clinic.address,
                          style: TextStyle(
                            fontSize: 12,
                            color: Colors.grey[600],
                          ),
                          maxLines: 1,
                          overflow: TextOverflow.ellipsis,
                        ),
                      ),
                    ],
                  ),
              ],
            ),
            trailing: PopupMenuButton<String>(
              icon: const Icon(Icons.more_vert),
              onSelected: (value) {
                if (value == 'edit') {
                  _editClinic(clinic);
                } else if (value == 'delete') {
                  _deleteClinic(clinic);
                }
              },
              itemBuilder: (context) => [
                const PopupMenuItem(
                  value: 'edit',
                  child: Row(
                    children: [
                      Icon(Icons.edit, size: 20),
                      SizedBox(width: 8),
                      Text('Modifier'),
                    ],
                  ),
                ),
                const PopupMenuItem(
                  value: 'delete',
                  child: Row(
                    children: [
                      Icon(Icons.delete, size: 20, color: Colors.red),
                      SizedBox(width: 8),
                      Text('Supprimer', style: TextStyle(color: Colors.red)),
                    ],
                  ),
                ),
              ],
            ),
          ),
          if (patients.isNotEmpty) ...[
            const Divider(height: 1),
            ExpansionTile(
              title: Text(
                '${patients.length} patient${patients.length > 1 ? 's' : ''}',
                style: TextStyle(
                  color: Colors.grey[700],
                  fontSize: 14,
                ),
              ),
              children: patients.map((patient) => ListTile(
                leading: CircleAvatar(
                  radius: 20,
                  backgroundColor: _getStatusColor(patient.status).withOpacity(0.1),
                  child: Text(
                    _getInitials(patient),
                    style: TextStyle(
                      color: _getStatusColor(patient.status),
                      fontWeight: FontWeight.bold,
                      fontSize: 12,
                    ),
                  ),
                ),
                title: Text(patient.fullName),
                subtitle: Text('${patient.age} ans • ${_getStatusLabel(patient.status)}'),
                trailing: PopupMenuButton<String>(
                  icon: const Icon(Icons.more_vert),
                  onSelected: (value) {
                    if (value == 'view') {
                      Navigator.push(
                        context,
                        MaterialPageRoute(
                          builder: (context) => PatientDetailScreen(
                            patient: patient,
                            onStatusChanged: _loadData,
                          ),
                        ),
                      );
                    } else if (value == 'transfer') {
                      _transferPatientToClinic(patient, clinic.id);
                    } else if (value == 'delete') {
                      _deletePatient(patient);
                    }
                  },
                  itemBuilder: (context) => [
                    const PopupMenuItem(
                      value: 'view',
                      child: Row(
                        children: [
                          Icon(Icons.visibility, size: 20, color: Colors.blue),
                          SizedBox(width: 8),
                          Text('Voir détails'),
                        ],
                      ),
                    ),
                    const PopupMenuItem(
                      value: 'transfer',
                      child: Row(
                        children: [
                          Icon(Icons.swap_horiz, size: 20, color: Colors.orange),
                          SizedBox(width: 8),
                          Text('Transférer'),
                        ],
                      ),
                    ),
                    const PopupMenuItem(
                      value: 'delete',
                      child: Row(
                        children: [
                          Icon(Icons.delete, size: 20, color: Colors.red),
                          SizedBox(width: 8),
                          Text('Supprimer', style: TextStyle(color: Colors.red)),
                        ],
                      ),
                    ),
                  ],
                ),
                onTap: () {
                  Navigator.push(
                    context,
                    MaterialPageRoute(
                      builder: (context) => PatientDetailScreen(
                        patient: patient,
                        onStatusChanged: _loadData,
                      ),
                    ),
                  );
                },
              )).toList(),
            ),
          ],
        ],
      ),
    );
  }

  String _getInitials(Patient patient) {
    final first = patient.firstName.isNotEmpty ? patient.firstName[0].toUpperCase() : '';
    final last = patient.lastName.isNotEmpty ? patient.lastName[0].toUpperCase() : '';
    return first + last;
  }

  String _getStatusLabel(PatientStatus status) {
    switch (status) {
      case PatientStatus.active:
        return 'Actif';
      case PatientStatus.discharged:
        return 'Sorti';
      case PatientStatus.transferred:
        return 'Transféré';
    }
  }

  Color _getStatusColor(PatientStatus status) {
    switch (status) {
      case PatientStatus.active:
        return Colors.green;
      case PatientStatus.discharged:
        return Colors.grey;
      case PatientStatus.transferred:
        return Colors.orange;
    }
  }
}
