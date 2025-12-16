import 'package:clinalert/models/patient_model.dart';
import 'package:clinalert/models/patient_api_model.dart';
import 'package:clinalert/models/user_model.dart';
import 'package:clinalert/screens/patient_detail_screen.dart';
import 'package:clinalert/screens/add_edit_patient_screen.dart';
import 'package:clinalert/screens/clinics_screen.dart';
import 'package:clinalert/widgets/custom_app_bar.dart';
import 'package:clinalert/widgets/patient_card.dart';
import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../providers/auth_provider.dart';
import '../services/api_service.dart';
import 'alerts_screen.dart';
import 'chat_list_screen.dart';
import '../widgets/adaptive_scaffold.dart';
import '../widgets/health_stat_card.dart';
import '../widgets/modern_card.dart';
import '../widgets/chart_widget.dart';
import '../widgets/chat_icon_button.dart';
import 'package:go_router/go_router.dart';
import 'patients_screen.dart';
import 'package:google_fonts/google_fonts.dart';
import 'settings_screen.dart';

class DoctorDashboardScreen extends StatefulWidget {
  const DoctorDashboardScreen({super.key});

  @override
  State<DoctorDashboardScreen> createState() => _DoctorDashboardScreenState();
}

class _DoctorDashboardScreenState extends State<DoctorDashboardScreen> {
  final ApiService _apiService = ApiService();
  List<Patient> _patients = [];
  bool _isLoading = true;
  String? _errorMessage;

  @override
  void initState() {
    super.initState();
    _loadPatients();
  }

  Future<void> _loadPatients() async {
    setState(() {
      _isLoading = true;
      _errorMessage = null;
    });

    try {
      final patientsData = await _apiService.getAllPatients();
      final patientApiModels = patientsData
          .map((json) => PatientApiModel.fromJson(json))
          .toList();
      
      setState(() {
        _patients = patientApiModels.map((api) => api.toPatient()).toList();
        _isLoading = false;
      });
    } catch (e) {
      setState(() {
        _errorMessage = 'Erreur de chargement des patients: $e';
        _isLoading = false;
      });
      print('Error loading patients: $e');
    }
  }

  @override
  Widget build(BuildContext context) {
    final authProvider = context.read<AuthProvider>();

    final appBar = CustomAppBar(
      title: 'Doctor Dashboard',
      user: authProvider.currentUser,
      onLogout: () => authProvider.logout(),
      actions: [
        ChatIconButton(
          onPressed: () => context.go('/chat-list'),
        ),
        IconButton(
          icon: const Icon(Icons.notifications_active_outlined),
          onPressed: () => Navigator.push(
            context,
            MaterialPageRoute(builder: (_) => const AlertsScreen()),
          ),
        ),
        IconButton(
          icon: const Icon(Icons.settings_outlined),
          onPressed: () => Navigator.push(
            context,
            MaterialPageRoute(builder: (_) => const SettingsScreen()),
          ),
          tooltip: 'Paramètres',
        ),
      ],
    );

    final body = CustomScrollView(
      slivers: [
        SliverToBoxAdapter(
          child: Padding(
            padding: const EdgeInsets.fromLTRB(20, 16, 20, 8),
            child: _DoctorHeader(),
          ),
        ),

        SliverPadding(
          padding: const EdgeInsets.symmetric(horizontal: 20, vertical: 12),
          sliver: SliverGrid.count(
            crossAxisCount: 2,
            mainAxisSpacing: 16,
            crossAxisSpacing: 16,
            childAspectRatio: 1.1,
            children: [
              HealthStatCard(
                title: 'Assigned Patients',
                value: _isLoading ? '...' : _patients.length.toString(),
                unit: 'Active',
                icon: Icons.people,
                iconColor: Colors.blue,
                onTap: () {},
              ),
              HealthStatCard(
                title: 'Critical Alerts',
                value: '3',
                unit: 'New',
                icon: Icons.warning_amber_rounded,
                iconColor: Colors.red,
                trend: '+1',
                isPositiveTrend: false,
                onTap: () => Navigator.push(context, MaterialPageRoute(builder: (_) => const AlertsScreen())),
              ),
            ],
          ),
        ),
        SliverToBoxAdapter(
          child: Padding(
            padding: const EdgeInsets.fromLTRB(20, 8, 20, 8),
            child: ModernCard(
              padding: const EdgeInsets.all(20),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    'Patient Analytics',
                    style: Theme.of(context).textTheme.titleMedium?.copyWith(
                          fontWeight: FontWeight.bold,
                        ),
                  ),
                  const SizedBox(height: 16),
                  const SizedBox(
                    height: 200,
                    child: ChartWidget(
                      data: [70, 82, 76, 88, 90, 79, 85],
                      color: Color(0xFF0066FF),
                      title: '',
                    ),
                  ),
                ],
              ),
            ),
          ),
        ),
        SliverToBoxAdapter(
          child: Padding(
            padding: const EdgeInsets.fromLTRB(20, 16, 20, 8),
            child: Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                Text(
                  'Recent Patients',
                  style: Theme.of(context).textTheme.titleMedium?.copyWith(
                        fontWeight: FontWeight.bold,
                      ),
                ),
                TextButton(
                  onPressed: () => Navigator.push(context, MaterialPageRoute(builder: (_) => const PatientsScreen())),
                  child: const Text('View All'),
                ),
              ],
            ),
          ),
        ),
        // Loading, error, or patient list
        if (_isLoading)
          const SliverToBoxAdapter(
            child: Center(
              child: Padding(
                padding: EdgeInsets.all(40),
                child: CircularProgressIndicator(),
              ),
            ),
          )
        else if (_errorMessage != null)
          SliverToBoxAdapter(
            child: Padding(
              padding: const EdgeInsets.all(20),
              child: ModernCard(
                padding: const EdgeInsets.all(20),
                child: Column(
                  children: [
                    const Icon(Icons.error_outline, size: 48, color: Colors.red),
                    const SizedBox(height: 16),
                    Text(
                      _errorMessage!,
                      style: const TextStyle(color: Colors.red),
                      textAlign: TextAlign.center,
                    ),
                    const SizedBox(height: 16),
                    ElevatedButton.icon(
                      onPressed: _loadPatients,
                      icon: const Icon(Icons.refresh),
                      label: const Text('Réessayer'),
                    ),
                  ],
                ),
              ),
            ),
          )
        else if (_patients.isEmpty)
          const SliverToBoxAdapter(
            child: Padding(
              padding: EdgeInsets.all(20),
              child: Center(
                child: Text('Aucun patient trouvé'),
              ),
            ),
          )
        else
          SliverPadding(
            padding: const EdgeInsets.fromLTRB(20, 0, 20, 80),
            sliver: SliverList.builder(
              itemCount: _patients.length,
              itemBuilder: (context, index) {
                final patient = _patients[index];
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
      ],
    );

    return AdaptiveScaffold(
      appBar: appBar,
      body: body,
      destinations: const [
        NavigationDestination(icon: Icon(Icons.dashboard_outlined), selectedIcon: Icon(Icons.dashboard), label: 'Dashboard'),
        NavigationDestination(icon: Icon(Icons.people_outline), selectedIcon: Icon(Icons.people), label: 'Patients'),
        NavigationDestination(icon: Icon(Icons.local_hospital_outlined), selectedIcon: Icon(Icons.local_hospital), label: 'Cliniques'),
        NavigationDestination(icon: Icon(Icons.notifications_outlined), selectedIcon: Icon(Icons.notifications), label: 'Alerts'),
      ],
      currentIndex: 0,
      onDestinationSelected: (i) {
        if (i == 1) {
          Navigator.push(context, MaterialPageRoute(builder: (_) => const PatientsScreen()));
          return;
        }
        if (i == 2) {
          Navigator.push(context, MaterialPageRoute(builder: (_) => const ClinicsScreen()));
          return;
        }
        if (i == 3) {
          Navigator.push(context, MaterialPageRoute(builder: (_) => const AlertsScreen()));
          return;
        }
      },
      floatingActionButton: FloatingActionButton.extended(
        onPressed: () async {
          final result = await Navigator.push(
            context,
            MaterialPageRoute(
              builder: (context) => const AddEditPatientScreen(),
            ),
          );
          if (result == true) {
            _loadPatients(); // Refresh list after adding
          }
        },
        icon: const Icon(Icons.add),
        label: const Text('Ajouter patient'),
        backgroundColor: const Color(0xFF0066FF),
      ),
    );
  }
}

class _DoctorHeader extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return Container(
      decoration: BoxDecoration(
        gradient: const LinearGradient(
          colors: [Color(0xFF0066FF), Color(0xFF00C4B4)],
          begin: Alignment.topLeft,
          end: Alignment.bottomRight,
        ),
        borderRadius: BorderRadius.circular(24),
        boxShadow: [
          BoxShadow(
            color: const Color(0xFF0066FF).withOpacity(0.3),
            blurRadius: 20,
            offset: const Offset(0, 10),
          ),
        ],
      ),
      padding: const EdgeInsets.all(24),
      child: Row(
        children: [
          Container(
            padding: const EdgeInsets.all(12),
            decoration: BoxDecoration(
              color: Colors.white.withOpacity(0.2),
              shape: BoxShape.circle,
            ),
            child: const Icon(Icons.medical_services, color: Colors.white, size: 28),
          ),
          const SizedBox(width: 16),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  'Good Morning, Dr.',
                  style: GoogleFonts.inter(
                    color: Colors.white.withOpacity(0.9),
                    fontSize: 16,
                  ),
                ),
                const SizedBox(height: 4),
                Text(
                  'Overview',
                  style: GoogleFonts.inter(
                    color: Colors.white,
                    fontSize: 24,
                    fontWeight: FontWeight.bold,
                  ),
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }
}