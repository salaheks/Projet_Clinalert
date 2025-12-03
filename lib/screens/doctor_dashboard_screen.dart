import 'package:clinalert/models/patient_model.dart';
import 'package:clinalert/models/user_model.dart';
import 'package:clinalert/screens/patient_detail_screen.dart';
import 'package:clinalert/widgets/custom_app_bar.dart';
import 'package:clinalert/widgets/patient_card.dart';
import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../providers/auth_provider.dart';
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

class DoctorDashboardScreen extends StatelessWidget {
  const DoctorDashboardScreen({super.key});

  @override
  Widget build(BuildContext context) {
    final authProvider = context.read<AuthProvider>();
    // Mock data for patients
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
                value: patients.length.toString(),
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
        SliverPadding(
          padding: const EdgeInsets.fromLTRB(20, 0, 20, 80),
          sliver: SliverList.builder(
            itemCount: patients.length,
            itemBuilder: (context, index) {
              final patient = patients[index];
              return Padding(
                padding: const EdgeInsets.only(bottom: 12),
                child: PatientCard(
                  patient: patient,
                  onTap: () {
                    Navigator.push(
                      context,
                      MaterialPageRoute(
                        builder: (context) => PatientDetailScreen(patient: patient),
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
        NavigationDestination(icon: Icon(Icons.notifications_outlined), selectedIcon: Icon(Icons.notifications), label: 'Alerts'),
      ],
      currentIndex: 0,
      onDestinationSelected: (i) {
        if (i == 1) {
          Navigator.push(context, MaterialPageRoute(builder: (_) => const PatientsScreen()));
          return;
        }
        if (i == 2) {
          Navigator.push(context, MaterialPageRoute(builder: (_) => const AlertsScreen()));
          return;
        }
      },
      floatingActionButton: null,
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