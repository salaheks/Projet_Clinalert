import 'package:clinalert/models/patient_model.dart';
import 'package:clinalert/models/user_model.dart';
import 'package:clinalert/models/vital_sign_model.dart';
import 'package:clinalert/widgets/custom_app_bar.dart';
import 'package:clinalert/widgets/vital_signs_grid.dart';
import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../providers/auth_provider.dart';
import 'patient_history_screen.dart';
import 'chat_list_screen.dart';
import '../widgets/adaptive_scaffold.dart';
import '../widgets/stat_card.dart';
import '../widgets/alert_card.dart';
import '../widgets/chart_widget.dart';
import '../widgets/chat_icon_button.dart';
import 'package:go_router/go_router.dart';

class PatientDashboardScreen extends StatelessWidget {
  const PatientDashboardScreen({super.key});

  @override
  Widget build(BuildContext context) {
    final authProvider = context.read<AuthProvider>();
    // Mock data for the current patient
    final patient = Patient(
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
      status: PatientStatus.active,
      createdAt: DateTime.now(),
    );
    // Mock data for vital signs
    final vitalSigns = [
      VitalSign(id: '1', patientId: patient.id, type: VitalType.heartRate, value: 82, unit: 'bpm', timestamp: DateTime.now()),
      VitalSign(id: '2', patientId: patient.id, type: VitalType.bloodPressure, value: 125, unit: 'mmHg', timestamp: DateTime.now()),
      VitalSign(id: '3', patientId: patient.id, type: VitalType.temperature, value: 37.1, unit: '°C', timestamp: DateTime.now()),
      VitalSign(id: '4', patientId: patient.id, type: VitalType.oxygenSaturation, value: 97, unit: '%', timestamp: DateTime.now()),
    ];

    final appBar = CustomAppBar(
        title: 'My Health Dashboard',
        user: authProvider.currentUser,
        onLogout: () => authProvider.logout(),
        actions: [
          ChatIconButton(
            onPressed: () => context.go('/chat-list'),
          ),
          IconButton(
            icon: const Icon(Icons.history),
            onPressed: () {
              Navigator.push(context, MaterialPageRoute(builder: (_) => const PatientHistoryScreen()));
            },
          ),
        ],
      );

    final body = CustomScrollView(
        slivers: [
          SliverToBoxAdapter(
            child: Padding(
              padding: const EdgeInsets.fromLTRB(16, 16, 16, 8),
              child: _PatientHero(name: patient.user.firstName),
            ),
          ),
          SliverToBoxAdapter(
            child: Padding(
              padding: const EdgeInsets.fromLTRB(16, 8, 16, 8),
              child: _PatientInsights(),
            ),
          ),
          SliverToBoxAdapter(
            child: Padding(
              padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
              child: ChartWidget(title: 'Weekly Heart Rate', data: const [78, 80, 76, 82, 79, 85, 81], color: const Color(0xFF00BCD4)),
            ),
          ),
          SliverToBoxAdapter(
            child: Padding(
              padding: const EdgeInsets.fromLTRB(16, 8, 16, 8),
              child: Text('Your Vital Signs', style: Theme.of(context).textTheme.headlineSmall),
            ),
          ),
          SliverToBoxAdapter(
            child: Padding(
              padding: const EdgeInsets.fromLTRB(16, 0, 16, 100),
              child: VitalSignsGrid(vitalSigns: vitalSigns),
            ),
          ),
        ],
      );

    return AdaptiveScaffold(
      appBar: appBar,
      body: body,
      destinations: const [
        NavigationDestination(icon: Icon(Icons.dashboard_outlined), selectedIcon: Icon(Icons.dashboard), label: 'Home'),
        NavigationDestination(icon: Icon(Icons.show_chart), selectedIcon: Icon(Icons.show_chart), label: 'History'),
        NavigationDestination(icon: Icon(Icons.person_outline), selectedIcon: Icon(Icons.person), label: 'Profile'),
      ],
      currentIndex: 0,
      onDestinationSelected: (i) {
        if (i == 1) {
          Navigator.push(context, MaterialPageRoute(builder: (_) => const PatientHistoryScreen()));
        }
      },
      floatingActionButton: FloatingActionButton.extended(
        onPressed: () {
          Navigator.push(context, MaterialPageRoute(builder: (_) => const PatientHistoryScreen()));
        },
        backgroundColor: Colors.blueAccent,
        icon: const Icon(Icons.show_chart, color: Colors.white),
        label: const Text('View History', style: TextStyle(color: Colors.white)),
      ),
    );
  }
}

class _PatientHero extends StatelessWidget {
  final String name;
  const _PatientHero({required this.name});
  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    return Container(
      decoration: BoxDecoration(
        gradient: LinearGradient(colors: [Colors.indigo, Colors.indigoAccent], begin: Alignment.topLeft, end: Alignment.bottomRight),
        borderRadius: BorderRadius.circular(16),
      ),
      padding: const EdgeInsets.all(16),
      child: Row(
        children: [
          const Icon(Icons.favorite, color: Colors.white),
          const SizedBox(width: 12),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text('Welcome, $name', style: theme.textTheme.titleMedium?.copyWith(color: Colors.white, fontWeight: FontWeight.bold)),
                const SizedBox(height: 4),
                Text('Track your health metrics in real-time', style: theme.textTheme.bodySmall?.copyWith(color: Colors.white70)),
              ],
            ),
          ),
          Container(
            padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 8),
            decoration: BoxDecoration(color: Colors.white.withOpacity(0.2), borderRadius: BorderRadius.circular(12)),
            child: const Text('Good', style: TextStyle(color: Colors.white, fontWeight: FontWeight.w600)),
          ),
        ],
      ),
    );
  }
}

class _PatientInsights extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return Row(
      children: const [
        Expanded(child: _InsightCard(icon: Icons.monitor_heart, label: 'Avg HR', value: '78 bpm', color: Colors.pinkAccent)),
        SizedBox(width: 12),
        Expanded(child: _InsightCard(icon: Icons.thermostat, label: 'Temp', value: '36.9 °C', color: Colors.orangeAccent)),
      ],
    );
  }
}

class _InsightCard extends StatelessWidget {
  final IconData icon;
  final String label;
  final String value;
  final Color color;
  const _InsightCard({required this.icon, required this.label, required this.value, required this.color});
  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    return Card(
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
      child: Padding(
        padding: const EdgeInsets.all(16.0),
        child: Row(
          children: [
            Container(
              padding: const EdgeInsets.all(10),
              decoration: BoxDecoration(color: color.withOpacity(0.15), shape: BoxShape.circle),
              child: Icon(icon, color: color),
            ),
            const SizedBox(width: 12),
            Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(label, style: theme.textTheme.bodySmall?.copyWith(color: Colors.grey[600])),
                const SizedBox(height: 4),
                Text(value, style: theme.textTheme.titleMedium?.copyWith(fontWeight: FontWeight.bold)),
              ],
            ),
          ],
        ),
      ),
    );
  }
}