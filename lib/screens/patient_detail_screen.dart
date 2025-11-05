import 'package:clinalert/models/patient_model.dart';
import 'package:clinalert/models/vital_sign_model.dart';
import 'package:clinalert/models/alert_model.dart';
import 'package:clinalert/widgets/custom_app_bar.dart';
import 'package:clinalert/widgets/vital_signs_grid.dart';
import 'package:clinalert/widgets/alert_card.dart';
import 'package:clinalert/widgets/stat_card.dart';
import 'package:clinalert/widgets/health_data_chart.dart';
import 'package:go_router/go_router.dart';
import 'package:flutter/material.dart';

class PatientDetailScreen extends StatelessWidget {
  final Patient patient;

  const PatientDetailScreen({super.key, required this.patient});

  @override
  Widget build(BuildContext context) {
    // Mock data for vital signs
    final vitalSigns = [
      VitalSign(id: '1', patientId: patient.id, type: VitalType.heartRate, value: 78, unit: 'bpm', timestamp: DateTime.now()),
      VitalSign(id: '2', patientId: patient.id, type: VitalType.bloodPressure, value: 120, unit: 'mmHg', timestamp: DateTime.now()),
      VitalSign(id: '3', patientId: patient.id, type: VitalType.temperature, value: 36.8, unit: '°C', timestamp: DateTime.now()),
      VitalSign(id: '4', patientId: patient.id, type: VitalType.oxygenSaturation, value: 98, unit: '%', timestamp: DateTime.now()),
    ];
    // Mock alerts for patient
    final alerts = [
      Alert(id: 'a1', patientId: patient.patientId, title: 'Blood Pressure High', description: 'Systolic over threshold.', level: AlertLevel.high, createdAt: DateTime.now().subtract(const Duration(minutes: 20))),
      Alert(id: 'a2', patientId: patient.patientId, title: 'Temperature Elevated', description: 'Slight fever detected.', level: AlertLevel.medium, createdAt: DateTime.now().subtract(const Duration(hours: 1))),
    ];

    return Scaffold(
      appBar: CustomAppBar(
        title: patient.fullName,
      ),
      body: CustomScrollView(
        slivers: [
          SliverToBoxAdapter(
            child: Padding(
              padding: const EdgeInsets.fromLTRB(16, 16, 16, 8),
              child: _HeaderSection(patient: patient),
            ),
          ),
          SliverToBoxAdapter(
            child: Padding(
              padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
              child: LayoutBuilder(builder: (context, c) {
                final isWide = MediaQuery.of(context).size.width >= 900;
                return Row(
                  children: [
                    Expanded(child: StatCard(icon: Icons.cake, label: 'Age', value: '${patient.age}', color: Colors.indigo)),
                    const SizedBox(width: 12),
                    Expanded(child: StatCard(icon: Icons.bloodtype, label: 'Blood', value: patient.bloodType, color: Colors.redAccent)),
                    if (isWide) ...[
                      const SizedBox(width: 12),
                      Expanded(child: StatCard(icon: Icons.monitor_heart, label: 'Avg HR', value: '79 bpm', color: Colors.pinkAccent)),
                    ],
                  ],
                );
              }),
            ),
          ),
          SliverToBoxAdapter(
            child: Padding(
              padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
              child: HealthDataChart(
                title: 'Heart Rate',
                day: const [76, 78, 79, 75, 80, 77, 78],
                week: const [78, 80, 76, 82, 79, 85, 81],
                month: const [80, 78, 77, 79, 81, 82, 83, 80, 78, 79, 77, 76],
                color: Colors.teal,
              ),
            ),
          ),
          SliverToBoxAdapter(
            child: Padding(
              padding: const EdgeInsets.fromLTRB(16, 8, 16, 8),
              child: Text('Vital Signs', style: Theme.of(context).textTheme.headlineSmall),
            ),
          ),
          SliverToBoxAdapter(
            child: Padding(
              padding: const EdgeInsets.fromLTRB(16, 0, 16, 8),
              child: VitalSignsGrid(vitalSigns: vitalSigns),
            ),
          ),
          SliverToBoxAdapter(
            child: Padding(
              padding: const EdgeInsets.fromLTRB(16, 8, 16, 8),
              child: Text('Recent Alerts', style: Theme.of(context).textTheme.headlineSmall),
            ),
          ),
          SliverList.builder(
            itemCount: alerts.length,
            itemBuilder: (context, index) => Padding(
              padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 6),
              child: AlertCard(alert: alerts[index], onTap: () {}),
            ),
          ),
          const SliverToBoxAdapter(child: SizedBox(height: 24)),
        ],
      ),
      floatingActionButton: FloatingActionButton.extended(
        onPressed: () {},
        icon: const Icon(Icons.email, color: Colors.white),
        label: const Text('Contact'),
      ),
    );
  }

  Widget _buildDetailRow(String label, String value) {
    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 8.0),
      child: Row(
        mainAxisAlignment: MainAxisAlignment.spaceBetween,
        children: [
          Text(label, style: const TextStyle(fontWeight: FontWeight.bold)),
          Text(value),
        ],
      ),
    );
  }
}

class _HeaderSection extends StatelessWidget {
  final Patient patient;
  const _HeaderSection({required this.patient});

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
          CircleAvatar(radius: 28, backgroundColor: Colors.white.withOpacity(0.2), child: const Icon(Icons.person, color: Colors.white)),
          const SizedBox(width: 12),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(patient.fullName, style: theme.textTheme.titleMedium?.copyWith(color: Colors.white, fontWeight: FontWeight.bold)),
                const SizedBox(height: 4),
                Text(patient.email, style: theme.textTheme.bodySmall?.copyWith(color: Colors.white70)),
              ],
            ),
          ),
          _ContactButton(icon: Icons.call, onTap: () {}),
          const SizedBox(width: 8),
          _ContactButton(icon: Icons.email, onTap: () {}),
          const SizedBox(width: 8),
          _ContactButton(icon: Icons.chat_bubble_outline, onTap: () { context.go('/chat'); }),
        ],
      ),
    );
  }
}

class _ContactButton extends StatelessWidget {
  final IconData icon;
  final VoidCallback onTap;
  const _ContactButton({required this.icon, required this.onTap});
  @override
  Widget build(BuildContext context) {
    return InkWell(
      onTap: onTap,
      borderRadius: BorderRadius.circular(10),
      child: Container(
        padding: const EdgeInsets.all(8),
        decoration: BoxDecoration(color: Colors.white.withOpacity(0.2), borderRadius: BorderRadius.circular(10)),
        child: Icon(icon, color: Colors.white),
      ),
    );
  }
}