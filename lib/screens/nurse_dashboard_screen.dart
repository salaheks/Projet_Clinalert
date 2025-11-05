import 'package:clinalert/models/patient_model.dart';
import 'package:clinalert/models/user_model.dart';
import 'package:clinalert/screens/patient_detail_screen.dart';
import 'package:clinalert/widgets/custom_app_bar.dart';
import 'package:clinalert/widgets/patient_card.dart';
import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../providers/auth_provider.dart';
import 'record_vital_screen.dart';
import 'alerts_screen.dart';
import 'chat_list_screen.dart';
import '../widgets/adaptive_scaffold.dart';
import '../widgets/stat_card.dart';
import '../widgets/alert_card.dart';
import '../widgets/chart_widget.dart';
import '../widgets/chat_icon_button.dart';
import 'package:go_router/go_router.dart';

class NurseDashboardScreen extends StatelessWidget {
  const NurseDashboardScreen({super.key});

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
        title: 'Nurse Dashboard',
        user: authProvider.currentUser,
        onLogout: () => authProvider.logout(),
        actions: [
          ChatIconButton(
            onPressed: () => context.go('/chat-list'),
          ),
          IconButton(
            icon: const Icon(Icons.warning_amber_outlined),
            onPressed: () => Navigator.push(context, MaterialPageRoute(builder: (_) => const AlertsScreen())),
          ),
        ],
      );

    final body = CustomScrollView(
        slivers: [
          SliverToBoxAdapter(
            child: Padding(
              padding: const EdgeInsets.fromLTRB(16, 16, 16, 8),
              child: _NurseHeader(),
            ),
          ),
          SliverToBoxAdapter(
            child: Padding(
              padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
              child: _NurseActions(
                onRecord: () => Navigator.push(context, MaterialPageRoute(builder: (_) => const RecordVitalScreen())),
                onAlerts: () => Navigator.push(context, MaterialPageRoute(builder: (_) => const AlertsScreen())),
              ),
            ),
          ),
          SliverToBoxAdapter(
            child: Padding(
              padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
              child: Row(
                children: const [
                  Expanded(child: StatCard(icon: Icons.favorite, label: 'Today Records', value: '12', color: Color(0xFF4CAF50))),
                  SizedBox(width: 12),
                  Expanded(child: StatCard(icon: Icons.timelapse, label: 'Pending Tasks', value: '5', color: Color(0xFF00BCD4))),
                ],
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
        NavigationDestination(icon: Icon(Icons.add), selectedIcon: Icon(Icons.add_circle), label: 'Record'),
        NavigationDestination(icon: Icon(Icons.notifications_outlined), selectedIcon: Icon(Icons.notifications), label: 'Alerts'),
      ],
      currentIndex: 0,
      onDestinationSelected: (i) {
        if (i == 1) {
          Navigator.push(context, MaterialPageRoute(builder: (_) => const RecordVitalScreen()));
        } else if (i == 2) {
          Navigator.push(context, MaterialPageRoute(builder: (_) => const AlertsScreen()));
        }
      },
      floatingActionButton: FloatingActionButton.extended(
        onPressed: () => Navigator.push(context, MaterialPageRoute(builder: (_) => const RecordVitalScreen())),
        backgroundColor: Colors.green,
        icon: const Icon(Icons.add, color: Colors.white),
        label: const Text('Record Vital', style: TextStyle(color: Colors.white)),
      ),
    );
  }
}

class _NurseHeader extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    return Container(
      decoration: BoxDecoration(
        gradient: LinearGradient(
          colors: [Colors.teal.shade600, Colors.teal.shade400],
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
            decoration: BoxDecoration(color: Colors.white.withOpacity(0.2), shape: BoxShape.circle),
            child: const Icon(Icons.healing, color: Colors.white),
          ),
          const SizedBox(width: 12),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text('Care Overview', style: theme.textTheme.titleMedium?.copyWith(color: Colors.white, fontWeight: FontWeight.bold)),
                const SizedBox(height: 4),
                Text('Record vitals and track alerts', style: theme.textTheme.bodySmall?.copyWith(color: Colors.white70)),
              ],
            ),
          ),
          _NurseStat(label: 'Assigned', value: '8'),
          const SizedBox(width: 8),
          _NurseStat(label: 'Due Now', value: '2'),
        ],
      ),
    );
  }
}

class _NurseStat extends StatelessWidget {
  final String label;
  final String value;
  const _NurseStat({required this.label, required this.value});
  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 8),
      decoration: BoxDecoration(color: Colors.white.withOpacity(0.2), borderRadius: BorderRadius.circular(12)),
      child: Column(
        children: [
          Text(value, style: const TextStyle(color: Colors.white, fontWeight: FontWeight.bold)),
          Text(label, style: const TextStyle(color: Colors.white70, fontSize: 12)),
        ],
      ),
    );
  }
}

class _NurseActions extends StatelessWidget {
  final VoidCallback onRecord;
  final VoidCallback onAlerts;
  const _NurseActions({required this.onRecord, required this.onAlerts});
  @override
  Widget build(BuildContext context) {
    return Wrap(
      spacing: 10,
      runSpacing: 10,
      children: [
        _ActionTile(icon: Icons.add, label: 'Record Vital', color: Colors.green, onTap: onRecord),
        _ActionTile(icon: Icons.warning_amber_rounded, label: 'Alerts', color: Colors.redAccent, onTap: onAlerts),
        _ActionTile(icon: Icons.assignment_turned_in, label: 'Tasks', color: Colors.blueGrey, onTap: () {}),
      ],
    );
  }
}

class _ActionTile extends StatelessWidget {
  final IconData icon;
  final String label;
  final Color color;
  final VoidCallback onTap;
  const _ActionTile({required this.icon, required this.label, required this.color, required this.onTap});
  @override
  Widget build(BuildContext context) {
    return InkWell(
      onTap: onTap,
      borderRadius: BorderRadius.circular(12),
      child: Container(
        width: 160,
        padding: const EdgeInsets.all(14),
        decoration: BoxDecoration(
          color: color.withOpacity(0.1),
          borderRadius: BorderRadius.circular(12),
          border: Border.all(color: color.withOpacity(0.2)),
        ),
        child: Row(
          mainAxisSize: MainAxisSize.min,
          children: [
            Icon(icon, color: color),
            const SizedBox(width: 10),
            Flexible(child: Text(label, style: TextStyle(color: color, fontWeight: FontWeight.w600))),
          ],
        ),
      ),
    );
  }
}