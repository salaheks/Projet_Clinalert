import 'package:clinalert/models/patient_model.dart';
import 'package:clinalert/models/patient_api_model.dart';
import 'package:clinalert/models/doctor_model.dart';
import 'package:clinalert/services/api_service.dart';
import 'package:clinalert/screens/patient_detail_screen.dart';
import 'package:clinalert/screens/add_edit_patient_screen.dart';
import 'package:clinalert/screens/add_edit_doctor_screen.dart';
import 'package:clinalert/providers/auth_provider.dart';
import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

class DoctorsScreen extends StatefulWidget {
  const DoctorsScreen({super.key});

  @override
  State<DoctorsScreen> createState() => _DoctorsScreenState();
}

class _DoctorsScreenState extends State<DoctorsScreen> {
  final ApiService _apiService = ApiService();
  final TextEditingController _searchController = TextEditingController();
  
  List<Doctor> _allDoctors = [];
  List<Doctor> _filteredDoctors = [];
  Map<String, List<Patient>> _doctorPatients = {};
  List<Patient> _unassignedPatients = [];
  bool _isLoading = true;
  String? _errorMessage;

  @override
  void initState() {
    super.initState();
    _loadDoctorsAndPatients();
    _searchController.addListener(_filterDoctors);
  }

  @override
  void dispose() {
    _searchController.dispose();
    super.dispose();
  }

  Future<void> _loadDoctorsAndPatients() async {
    setState(() {
      _isLoading = true;
      _errorMessage = null;
    });

    try {
      final authProvider = Provider.of<AuthProvider>(context, listen: false);
      
      // Fetch doctors and patients in parallel
      final results = await Future.wait([
        _apiService.getAllDoctors(),
        _apiService.getAllPatients(),
      ]);

      final doctorsData = results[0] as List<Map<String, dynamic>>;
      final patientsData = results[1] as List<Map<String, dynamic>>;

      // Parse doctors
      var doctors = doctorsData
          .map((json) => Doctor.fromJson(json))
          .toList();

      // Parse patients
      var patients = patientsData
          .map((json) => PatientApiModel.fromJson(json))
          .map((api) => api.toPatient())
          .toList();

      // Filter based on role
      if (authProvider.isDoctor && authProvider.currentDoctorEmail != null) {
        // Doctor: show only their own data
        doctors = doctors.where((d) => d.email == authProvider.currentDoctorEmail).toList();
        
        // Find the doctor's ID
        final doctorId = doctors.isNotEmpty ? doctors.first.id : null;
        if (doctorId != null) {
          patients = patients.where((p) => p.primaryDoctorId == doctorId).toList();
        } else {
          patients = [];
        }
      }
      // Admin sees all (no filtering needed)

      // Group patients by doctor ID
      final Map<String, List<Patient>> patientsByDoctor = {};
      final List<Patient> unassignedPatients = [];
      
      for (var patient in patients) {
        final doctorId = patient.primaryDoctorId;
        if (doctorId != null && doctorId.isNotEmpty) {
          if (!patientsByDoctor.containsKey(doctorId)) {
            patientsByDoctor[doctorId] = [];
          }
          patientsByDoctor[doctorId]!.add(patient);
        } else {
          // Patient has no assigned doctor
          unassignedPatients.add(patient);
        }
      }

      setState(() {
        _allDoctors = doctors;
        _filteredDoctors = doctors;
        _doctorPatients = patientsByDoctor;
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

  void _filterDoctors() {
    final query = _searchController.text.toLowerCase();
    setState(() {
      if (query.isEmpty) {
        _filteredDoctors = _allDoctors;
      } else {
        _filteredDoctors = _allDoctors.where((doctor) {
          final nameMatch = doctor.name.toLowerCase().contains(query);
          final specialtyMatch = doctor.specialty.toLowerCase().contains(query);
          final emailMatch = doctor.email.toLowerCase().contains(query);
          
          // Also check if any patient of this doctor matches
          final patients = _doctorPatients[doctor.id] ?? [];
          final hasMatchingPatient = patients.any((patient) =>
              patient.fullName.toLowerCase().contains(query));
          
          return nameMatch || specialtyMatch || emailMatch || hasMatchingPatient;
        }).toList();
      }
    });
  }

  Future<void> _assignPatientToDoctor(Patient patient) async {
    final selectedDoctor = await showDialog<String>(
      context: context,
      builder: (context) => AlertDialog(
        title: const Text('Assigner à un médecin'),
        content: Column(
          mainAxisSize: MainAxisSize.min,
          children: _allDoctors.map((doctor) {
            final patientCount = _doctorPatients[doctor.id]?.length ?? 0;
            return RadioListTile<String>(
              title: Text(doctor.name),
              subtitle: Text('${doctor.specialty} • $patientCount patients'),
              value: doctor.id,
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

    if (selectedDoctor != null) {
      try {
        await _apiService.updatePatient(patient.id, {
          'name': patient.fullName,
          'age': patient.age,
          'gender': patient.user.firstName.toLowerCase().contains('marie') ? 'F' : 'M',
          'status': patient.status.name,
          'doctorId': selectedDoctor,
        });

        if (mounted) {
          ScaffoldMessenger.of(context).showSnackBar(
            const SnackBar(
              content: Text('Patient assigné avec succès'),
              backgroundColor: Colors.green,
            ),
          );
          _loadDoctorsAndPatients();
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

  Future<void> _reassignPatient(Patient patient, String currentDoctorId) async {
    final selectedDoctor = await showDialog<String>(
      context: context,
      builder: (context) => AlertDialog(
        title: const Text('Réassigner le patient'),
        content: Column(
          mainAxisSize: MainAxisSize.min,
          children: _allDoctors.map((doctor) {
            final patientCount = _doctorPatients[doctor.id]?.length ?? 0;
            return RadioListTile<String>(
              title: Text(doctor.name),
              subtitle: Text('${doctor.specialty} • $patientCount patients'),
              value: doctor.id,
              groupValue: currentDoctorId,
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

    if (selectedDoctor != null && selectedDoctor != currentDoctorId) {
      try {
        await _apiService.updatePatient(patient.id, {
          'name': patient.fullName,
          'age': patient.age,
          'gender': patient.user.firstName.toLowerCase().contains('marie') ? 'F' : 'M',
          'status': patient.status.name,
          'doctorId': selectedDoctor,
        });

        if (mounted) {
          ScaffoldMessenger.of(context).showSnackBar(
            const SnackBar(
              content: Text('Patient réassigné avec succès'),
              backgroundColor: Colors.green,
            ),
          );
          _loadDoctorsAndPatients();
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

  Future<void> _removePatientFromDoctor(Patient patient) async {
    final confirmed = await showDialog<bool>(
      context: context,
      builder: (context) => AlertDialog(
        title: const Text('Retirer le patient'),
        content: Text('Voulez-vous retirer ${patient.fullName} de ce médecin ?'),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(context, false),
            child: const Text('Annuler'),
          ),
          TextButton(
            onPressed: () => Navigator.pop(context, true),
            style: TextButton.styleFrom(foregroundColor: Colors.red),
            child: const Text('Retirer'),
          ),
        ],
      ),
    );

    if (confirmed == true) {
      try {
        await _apiService.updatePatient(patient.id, {
          'name': patient.fullName,
          'age': patient.age,
          'gender': patient.user.firstName.toLowerCase().contains('marie') ? 'F' : 'M',
          'status': patient.status.name,
          'doctorId': null,
        });

        if (mounted) {
          ScaffoldMessenger.of(context).showSnackBar(
            const SnackBar(
              content: Text('Patient retiré avec succès'),
              backgroundColor: Colors.green,
            ),
          );
          _loadDoctorsAndPatients();
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

  Future<void> _editDoctor(Doctor doctor) async {
    final result = await Navigator.push(
      context,
      MaterialPageRoute(
        builder: (context) => AddEditDoctorScreen(doctor: doctor),
      ),
    );
    if (result == true) {
      _loadDoctorsAndPatients();
    }
  }

  Future<void> _deleteDoctor(Doctor doctor) async {
    final patientCount = _doctorPatients[doctor.id]?.length ?? 0;
    
    final confirmed = await showDialog<bool>(
      context: context,
      builder: (context) => AlertDialog(
        title: const Text('Supprimer le médecin'),
        content: Text(
          patientCount > 0
              ? 'Ce médecin a $patientCount patient(s). Voulez-vous vraiment le supprimer ?'
              : 'Voulez-vous vraiment supprimer ${doctor.name} ?',
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
        await _apiService.deleteDoctor(doctor.id);
        if (mounted) {
          ScaffoldMessenger.of(context).showSnackBar(
            const SnackBar(
              content: Text('Médecin supprimé avec succès'),
              backgroundColor: Colors.green,
            ),
          );
          _loadDoctorsAndPatients();
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
      appBar: AppBar(
        title: Row(
          children: [
            const Text('Médecins et leurs patients'),
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
        bottom: PreferredSize(
          preferredSize: const Size.fromHeight(60),
          child: Padding(
            padding: const EdgeInsets.all(12),
            child: TextField(
              controller: _searchController,
              decoration: InputDecoration(
                hintText: 'Rechercher un médecin ou patient...',
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
            onPressed: _loadDoctorsAndPatients,
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
                        onPressed: _loadDoctorsAndPatients,
                        child: const Text('Réessayer'),
                      ),
                    ],
                  ),
                )
              : _filteredDoctors.isEmpty
                  ? Center(
                      child: Column(
                        mainAxisAlignment: MainAxisAlignment.center,
                        children: [
                          const Icon(Icons.search_off, size: 64, color: Colors.grey),
                          const SizedBox(height: 16),
                          Text(
                            _searchController.text.isEmpty
                                ? 'Aucun médecin trouvé'
                                : 'Aucun résultat pour "${_searchController.text}"',
                            style: const TextStyle(fontSize: 16, color: Colors.grey),
                          ),
                        ],
                      ),
                    )
                  : RefreshIndicator(
                      onRefresh: _loadDoctorsAndPatients,
                      child: ListView.builder(
                        padding: const EdgeInsets.all(16),
                        itemCount: _filteredDoctors.length + (_unassignedPatients.isNotEmpty ? 1 : 0),
                        itemBuilder: (context, index) {
                          // Show unassigned patients section first
                          if (_unassignedPatients.isNotEmpty && index == 0) {
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
                                      'Patients sans médecin attribué',
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
                                          if (value == 'view') {
                                            Navigator.push(
                                              context,
                                              MaterialPageRoute(
                                                builder: (context) => PatientDetailScreen(
                                                  patient: patient,
                                                  onStatusChanged: _loadDoctorsAndPatients,
                                                ),
                                              ),
                                            );
                                          } else if (value == 'assign') {
                                            _assignPatientToDoctor(patient);
                                          }
                                        },
                                        itemBuilder: (context) => [
                                          const PopupMenuItem(
                                            value: 'view',
                                            child: Row(
                                              children: [
                                                Icon(Icons.visibility, size: 20),
                                                SizedBox(width: 8),
                                                Text('Voir détails'),
                                              ],
                                            ),
                                          ),
                                          const PopupMenuItem(
                                            value: 'assign',
                                            child: Row(
                                              children: [
                                                Icon(Icons.person_add, size: 20, color: Colors.blue),
                                                SizedBox(width: 8),
                                                Text('Assigner à un médecin', style: TextStyle(color: Colors.blue)),
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
                          
                          // Adjust index for doctors list
                          final doctorIndex = _unassignedPatients.isNotEmpty ? index - 1 : index;
                          final doctor = _filteredDoctors[doctorIndex];
                          final patients = _doctorPatients[doctor.id] ?? [];

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
                                    backgroundColor: const Color(0xFF0066FF).withOpacity(0.1),
                                    child: Text(
                                      doctor.getInitials(),
                                      style: const TextStyle(
                                        color: Color(0xFF0066FF),
                                        fontWeight: FontWeight.bold,
                                      ),
                                    ),
                                  ),
                                  title: Text(
                                    doctor.name,
                                    style: const TextStyle(
                                      fontWeight: FontWeight.bold,
                                      fontSize: 16,
                                    ),
                                  ),
                                  subtitle: Column(
                                    crossAxisAlignment: CrossAxisAlignment.start,
                                    children: [
                                      Text(doctor.specialty),
                                      if (doctor.email.isNotEmpty)
                                        Text(
                                          doctor.email,
                                          style: TextStyle(
                                            fontSize: 12,
                                            color: Colors.grey[600],
                                          ),
                                        ),
                                    ],
                                  ),
                                  trailing: PopupMenuButton<String>(
                                    icon: const Icon(Icons.more_vert),
                                    onSelected: (value) {
                                      if (value == 'edit') {
                                        _editDoctor(doctor);
                                      } else if (value == 'delete') {
                                        _deleteDoctor(doctor);
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
                                                  onStatusChanged: _loadDoctorsAndPatients,
                                                ),
                                              ),
                                            );
                                          } else if (value == 'reassign') {
                                            _reassignPatient(patient, doctor.id);
                                          } else if (value == 'remove') {
                                            _removePatientFromDoctor(patient);
                                          }
                                        },
                                        itemBuilder: (context) => [
                                          const PopupMenuItem(
                                            value: 'view',
                                            child: Row(
                                              children: [
                                                Icon(Icons.visibility, size: 20),
                                                SizedBox(width: 8),
                                                Text('Voir détails'),
                                              ],
                                            ),
                                          ),
                                          const PopupMenuItem(
                                            value: 'reassign',
                                            child: Row(
                                              children: [
                                                Icon(Icons.swap_horiz, size: 20),
                                                SizedBox(width: 8),
                                                Text('Réassigner'),
                                              ],
                                            ),
                                          ),
                                          const PopupMenuItem(
                                            value: 'remove',
                                            child: Row(
                                              children: [
                                                Icon(Icons.remove_circle_outline, size: 20, color: Colors.red),
                                                SizedBox(width: 8),
                                                Text('Retirer', style: TextStyle(color: Colors.red)),
                                              ],
                                            ),
                                          ),
                                        ],
                                      ),
                                    )).toList(),
                                  ),
                                ],
                              ],
                            ),
                          );
                        },
                      ),
                    ),
      floatingActionButton: FloatingActionButton(
        onPressed: () async {
          final result = await Navigator.push(
            context,
            MaterialPageRoute(
              builder: (context) => const AddEditDoctorScreen(),
            ),
          );
          if (result == true) {
            _loadDoctorsAndPatients();
          }
        },
        child: const Icon(Icons.person_add),
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
