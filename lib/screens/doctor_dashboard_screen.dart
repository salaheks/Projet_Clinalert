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
import '../widgets/stat_card.dart';
import '../widgets/alert_card.dart';
import '../widgets/chart_widget.dart';
import '../widgets/chat_icon_button.dart';
import 'package:go_router/go_router.dart';
import 'patients_screen.dart';

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
            icon: const Icon(Icons.notifications_active),
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
              padding: const EdgeInsets.fromLTRB(16, 16, 16, 8),
              child: _DoctorHeader(),
            ),
          ),
          SliverToBoxAdapter(
            child: Padding(
              padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
              child: _QuickActions(
                onAlerts: () => Navigator.push(context, MaterialPageRoute(builder: (_) => const AlertsScreen())),
              ),
            ),
          ),
          SliverToBoxAdapter(
            child: Padding(
              padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
              child: LayoutBuilder(builder: (context, c) {
                final isWide = MediaQuery.of(context).size.width >= 900;
                return Row(
                  children: [
                    Expanded(child: StatCard(icon: Icons.people, label: 'Assigned Patients', value: patients.length.toString(), color: const Color(0xFF00BCD4))),
                    const SizedBox(width: 12),
                    Expanded(child: StatCard(icon: Icons.warning_amber_rounded, label: 'Active Alerts', value: '3', color: const Color(0xFFE53935))),
                    if (isWide) ...[
                      const SizedBox(width: 12),
                      Expanded(child: StatCard(icon: Icons.analytics, label: 'Avg HR', value: '79 bpm', color: const Color(0xFF4CAF50))),
                    ],
                  ],
                );
              }),
            ),
          ),
          SliverToBoxAdapter(
            child: Padding(
              padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
              child: ChartWidget(
                data: const [70, 82, 76, 88, 90, 79, 85],
                color: const Color(0xFF00BCD4),
                title: 'Patient Analytics (HR trend)',
              ),
            ),
          ),
          SliverPadding(
            padding: const EdgeInsets.fromLTRB(16, 8, 16, 80),
            sliver: SliverList.builder(
              itemCount: patients.length,
              itemBuilder: (context, index) {
                final patient = patients[index];
                return PatientCard(
                  patient: patient,
                  onTap: () {
                    Navigator.push(
                      context,
                      MaterialPageRoute(
                        builder: (context) => PatientDetailScreen(patient: patient),
                      ),
                    );
                  },
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
      floatingActionButton: FloatingActionButton.extended(
        onPressed: () => Navigator.push(
          context,
          MaterialPageRoute(builder: (_) => const AlertsScreen()),
        ),
        backgroundColor: Colors.redAccent,
        icon: const Icon(Icons.warning_amber_rounded, color: Colors.white),
        label: const Text('Alerts', style: TextStyle(color: Colors.white)),
      ),
    );
  }
}

class _DoctorHeader extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    return Container(
      decoration: BoxDecoration(
        gradient: LinearGradient(
          colors: [Colors.blue.shade600, Colors.blue.shade400],
          begin: Alignment.topLeft,
          end: Alignment.bottomRight,
        ),
        borderRadius: BorderRadius.circular(16),
      ),
      padding: const EdgeInsets.all(16),
      child: Row(
        children: [
          Container(
            padding: const EdgeInsets.all(10),
            decoration: BoxDecoration(
              color: Colors.white.withOpacity(0.2),
              shape: BoxShape.circle,
            ),
            child: const Icon(Icons.local_hospital, color: Colors.white),
          ),
          const SizedBox(width: 12),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text('Today Overview', style: theme.textTheme.titleMedium?.copyWith(color: Colors.white, fontWeight: FontWeight.bold)),
                const SizedBox(height: 4),
                Text('Monitor patients and review alerts', style: theme.textTheme.bodySmall?.copyWith(color: Colors.white70)),
              ],
            ),
          ),
          _StatChip(label: 'Patients', value: '12'),
          const SizedBox(width: 8),
          _StatChip(label: 'Alerts', value: '3'),
        ],
      ),
    );
  }
}

class _StatChip extends StatelessWidget {
  final String label;
  final String value;
  const _StatChip({required this.label, required this.value});
  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 8),
      decoration: BoxDecoration(
        color: Colors.white.withOpacity(0.2),
        borderRadius: BorderRadius.circular(12),
      ),
      child: Column(
        children: [
          Text(value, style: const TextStyle(color: Colors.white, fontWeight: FontWeight.bold)),
          Text(label, style: const TextStyle(color: Colors.white70, fontSize: 12)),
        ],
      ),
    );
  }
}

class _QuickActions extends StatelessWidget {
  final VoidCallback onAlerts;
  const _QuickActions({required this.onAlerts});
  @override
  Widget build(BuildContext context) {
    return Wrap(
      spacing: 10,
      runSpacing: 10,
      children: [
        _ActionChip(icon: Icons.warning_amber_rounded, label: 'Alerts', color: Colors.redAccent, onTap: onAlerts),
        _ActionChip(icon: Icons.search, label: 'Search', color: Colors.blueGrey, onTap: () {}),
        _ActionChip(icon: Icons.calendar_today, label: 'Schedule', color: Colors.indigo, onTap: () {}),
      ],
    );
  }
}

class _ActionChip extends StatelessWidget {
  final IconData icon;
  final String label;
  final Color color;
  final VoidCallback onTap;
  const _ActionChip({required this.icon, required this.label, required this.color, required this.onTap});
  @override
  Widget build(BuildContext context) {
    return InkWell(
      onTap: onTap,
      borderRadius: BorderRadius.circular(12),
      child: Container(
        padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 12),
        decoration: BoxDecoration(
          color: color.withOpacity(0.1),
          borderRadius: BorderRadius.circular(12),
          border: Border.all(color: color.withOpacity(0.2)),
        ),
        child: Row(
          mainAxisSize: MainAxisSize.min,
          children: [
            Icon(icon, color: color),
            const SizedBox(width: 8),
            Text(label, style: TextStyle(color: color, fontWeight: FontWeight.w600)),
          ],
        ),
      ),
    );
  }
}