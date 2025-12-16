import 'package:clinalert/models/patient_model.dart';
import 'package:clinalert/models/patient_api_model.dart';
import 'package:clinalert/services/api_service.dart';
import 'package:clinalert/screens/patient_detail_screen.dart';
import 'package:clinalert/screens/add_edit_patient_screen.dart';
import 'package:clinalert/widgets/patient_card.dart';
import 'package:clinalert/providers/auth_provider.dart';
import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

class PatientsScreen extends StatefulWidget {
  const PatientsScreen({super.key});

  @override
  State<PatientsScreen> createState() => _PatientsScreenState();
}

class _PatientsScreenState extends State<PatientsScreen> {
  final ApiService _apiService = ApiService();
  final TextEditingController _searchController = TextEditingController();
  
  List<Patient> _allPatients = [];
  List<Patient> _filteredPatients = [];
  bool _isLoading = true;
  String? _errorMessage;

  @override
  void initState() {
    super.initState();
    _loadPatients();
    _searchController.addListener(_filterPatients);
  }

  @override
  void dispose() {
    _searchController.dispose();
    super.dispose();
  }

  Future<void> _loadPatients() async {
    setState(() {
      _isLoading = true;
      _errorMessage = null;
    });

    try {
      final authProvider = Provider.of<AuthProvider>(context, listen: false);
      
      final patientsData = await _apiService.getAllPatients();
      final patientApiModels = patientsData
          .map((json) => PatientApiModel.fromJson(json))
          .toList();
      
      var patients = patientApiModels.map((api) => api.toPatient()).toList();
      
      // Filter based on role
      if (authProvider.isDoctor && authProvider.currentDoctorEmail != null) {
        // Doctor: show only their own patients
        // First, get all doctors to find the current doctor's ID
        final doctorsData = await _apiService.getAllDoctors();
        final currentDoctor = doctorsData.firstWhere(
          (d) => d['email'] == authProvider.currentDoctorEmail,
          orElse: () => <String, dynamic>{},
        );
        
        if (currentDoctor.isNotEmpty) {
          final doctorId = currentDoctor['id'] as String;
          patients = patients.where((p) => p.primaryDoctorId == doctorId).toList();
        } else {
          patients = [];
        }
      }
      // Admin sees all (no filtering needed)
      
      setState(() {
        _allPatients = patients;
        _filteredPatients = patients;
        _isLoading = false;
      });
    } catch (e) {
      setState(() {
        _errorMessage = 'Erreur de chargement: $e';
        _isLoading = false;
      });
    }
  }

  void _filterPatients() {
    final query = _searchController.text.toLowerCase();
    setState(() {
      if (query.isEmpty) {
        _filteredPatients = _allPatients;
      } else {
        _filteredPatients = _allPatients.where((patient) {
          final name = patient.fullName.toLowerCase();
          final age = patient.age.toString();
          final status = patient.status.name.toLowerCase();
          return name.contains(query) || age.contains(query) || status.contains(query);
        }).toList();
      }
    });
  }

  @override
  Widget build(BuildContext context) {
    final authProvider = Provider.of<AuthProvider>(context);
    
    return Scaffold(
      appBar: AppBar(
        title: Row(
          children: [
            const Text('Tous les patients'),
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
                hintText: 'Rechercher par nom, âge ou statut...',
                prefixIcon: const Icon(Icons.search),
                suffixIcon: _searchController.text.isNotEmpty
                    ? IconButton(
                        icon: const Icon(Icons.clear),
                        onPressed: () {
                          _searchController.clear();
                        },
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
                        onPressed: _loadPatients,
                        child: const Text('Réessayer'),
                      ),
                    ],
                  ),
                )
              : _filteredPatients.isEmpty
                  ? Center(
                      child: Column(
                        mainAxisAlignment: MainAxisAlignment.center,
                        children: [
                          const Icon(Icons.search_off, size: 64, color: Colors.grey),
                          const SizedBox(height: 16),
                          Text(
                            _searchController.text.isEmpty
                                ? 'Aucun patient trouvé'
                                : 'Aucun résultat pour "${_searchController.text}"',
                            style: const TextStyle(fontSize: 16, color: Colors.grey),
                          ),
                        ],
                      ),
                    )
                  : RefreshIndicator(
                      onRefresh: _loadPatients,
                      child: ListView.builder(
                        padding: const EdgeInsets.all(16),
                        itemCount: _filteredPatients.length,
                        itemBuilder: (context, index) {
                          final patient = _filteredPatients[index];
                          return Padding(
                            padding: const EdgeInsets.only(bottom: 12),
                            child: PatientCard(
                              patient: patient,
                              onTap: () async {
                                await Navigator.push(
                                  context,
                                  MaterialPageRoute(
                                    builder: (context) => PatientDetailScreen(
                                      patient: patient,
                                      onStatusChanged: _loadPatients,
                                    ),
                                  ),
                                );
                              },
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
              builder: (context) => const AddEditPatientScreen(),
            ),
          );
          if (result == true) {
            _loadPatients();
          }
        },
        child: const Icon(Icons.add),
      ),
    );
  }
}
