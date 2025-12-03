import 'package:clinalert/models/patient_model.dart';
import 'package:clinalert/models/vital_sign_model.dart';
import 'package:clinalert/models/alert_model.dart';
import 'package:clinalert/widgets/custom_app_bar.dart';
import 'package:clinalert/widgets/health_stat_card.dart';
import 'package:clinalert/widgets/modern_card.dart';
import 'package:clinalert/widgets/modern_badge.dart';
import 'package:clinalert/widgets/chart_widget.dart';
import 'package:go_router/go_router.dart';
import 'package:flutter/material.dart';
import 'package:google_fonts/google_fonts.dart';

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
        title: 'Patient Profile',
      ),
      body: CustomScrollView(
        slivers: [
          SliverToBoxAdapter(
            child: Padding(
              padding: const EdgeInsets.fromLTRB(20, 16, 20, 8),
              child: _HeaderSection(patient: patient),
            ),
          ),
          SliverToBoxAdapter(
            child: Padding(
              padding: const EdgeInsets.symmetric(horizontal: 20, vertical: 8),
              child: Row(
                children: [
                  Expanded(
                    child: _InfoCard(
                      label: 'Age',
                      value: '${patient.age} yrs',
                      icon: Icons.cake,
                      color: Colors.indigo,
                    ),
                  ),
                  const SizedBox(width: 12),
                  Expanded(
                    child: _InfoCard(
                      label: 'Blood Type',
                      value: patient.bloodType,
                      icon: Icons.bloodtype,
                      color: Colors.red,
                    ),
                  ),
                  const SizedBox(width: 12),
                  Expanded(
                    child: _InfoCard(
                      label: 'Status',
                      value: 'Stable',
                      icon: Icons.health_and_safety,
                      color: Colors.green,
                    ),
                  ),
                ],
              ),
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
                  title: 'Heart Rate',
                  value: '78',
                  unit: 'bpm',
                  icon: Icons.favorite,
                  iconColor: Colors.red,
                  trend: 'Normal',
                  isPositiveTrend: true,
                ),
                HealthStatCard(
                  title: 'Blood Pressure',
                  value: '120/80',
                  unit: 'mmHg',
                  icon: Icons.water_drop,
                  iconColor: Colors.blue,
                  trend: 'Optimal',
                  isPositiveTrend: true,
                ),
                HealthStatCard(
                  title: 'Temperature',
                  value: '36.8',
                  unit: '°C',
                  icon: Icons.thermostat,
                  iconColor: Colors.orange,
                ),
                HealthStatCard(
                  title: 'SpO2',
                  value: '98',
                  unit: '%',
                  icon: Icons.air,
                  iconColor: Colors.cyan,
                  trend: 'Excellent',
                  isPositiveTrend: true,
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
                      'Health Trends',
                      style: Theme.of(context).textTheme.titleMedium?.copyWith(
                            fontWeight: FontWeight.bold,
                          ),
                    ),
                    const SizedBox(height: 16),
                    const SizedBox(
                      height: 200,
                      child: ChartWidget(
                        title: '',
                        data: [76, 78, 79, 75, 80, 77, 78],
                        color: Color(0xFF00C4B4),
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
              child: Text(
                'Recent Alerts',
                style: Theme.of(context).textTheme.titleMedium?.copyWith(
                      fontWeight: FontWeight.bold,
                    ),
              ),
            ),
          ),
          SliverList.builder(
            itemCount: alerts.length,
            itemBuilder: (context, index) => Padding(
              padding: const EdgeInsets.symmetric(horizontal: 20, vertical: 6),
              child: ModernCard(
                padding: const EdgeInsets.all(16),
                child: Row(
                  children: [
                    Container(
                      padding: const EdgeInsets.all(10),
                      decoration: BoxDecoration(
                        color: (alerts[index].level == AlertLevel.high ? Colors.red : Colors.orange).withOpacity(0.1),
                        shape: BoxShape.circle,
                      ),
                      child: Icon(
                        Icons.warning_amber_rounded,
                        color: alerts[index].level == AlertLevel.high ? Colors.red : Colors.orange,
                      ),
                    ),
                    const SizedBox(width: 16),
                    Expanded(
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Text(
                            alerts[index].title,
                            style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 16),
                          ),
                          const SizedBox(height: 4),
                          Text(
                            alerts[index].description,
                            style: TextStyle(color: Colors.grey[600], fontSize: 14),
                          ),
                        ],
                      ),
                    ),
                    Column(
                      crossAxisAlignment: CrossAxisAlignment.end,
                      children: [
                        ModernBadge(
                          text: alerts[index].level == AlertLevel.high ? 'High' : 'Medium',
                          color: alerts[index].level == AlertLevel.high ? Colors.red : Colors.orange,
                        ),
                        const SizedBox(height: 8),
                        Text(
                          '20m ago',
                          style: TextStyle(color: Colors.grey[400], fontSize: 12),
                        ),
                      ],
                    ),
                  ],
                ),
              ),
            ),
          ),
          const SliverToBoxAdapter(child: SizedBox(height: 24)),
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
    return ModernCard(
      padding: const EdgeInsets.all(24),
      child: Column(
        children: [
          Row(
            children: [
              CircleAvatar(
                radius: 32,
                backgroundColor: const Color(0xFF0066FF).withOpacity(0.1),
                child: Text(
                  patient.firstName[0] + patient.lastName[0],
                  style: GoogleFonts.inter(
                    fontSize: 24,
                    fontWeight: FontWeight.bold,
                    color: const Color(0xFF0066FF),
                  ),
                ),
              ),
              const SizedBox(width: 16),
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      patient.fullName,
                      style: GoogleFonts.inter(
                        fontSize: 20,
                        fontWeight: FontWeight.bold,
                      ),
                    ),
                    const SizedBox(height: 4),
                    Text(
                      'ID: ${patient.patientId}',
                      style: GoogleFonts.inter(
                        color: Colors.grey[600],
                        fontSize: 14,
                      ),
                    ),
                  ],
                ),
              ),
            ],
          ),
          const SizedBox(height: 24),
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceEvenly,
            children: [
              _ActionButton(
                icon: Icons.call,
                label: 'Call',
                onTap: () {},
              ),
              _ActionButton(
                icon: Icons.email,
                label: 'Email',
                onTap: () {},
              ),
              _ActionButton(
                icon: Icons.chat_bubble_outline,
                label: 'Chat',
                onTap: () {
                  context.go('/chat');
                },
              ),
            ],
          ),
        ],
      ),
    );
  }
}

class _InfoCard extends StatelessWidget {
  final String label;
  final String value;
  final IconData icon;
  final Color color;

  const _InfoCard({
    required this.label,
    required this.value,
    required this.icon,
    required this.color,
  });

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.all(12),
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(12),
        border: Border.all(color: Colors.grey.withOpacity(0.1)),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Icon(icon, color: color, size: 20),
          const SizedBox(height: 8),
          Text(
            label,
            style: TextStyle(
              color: Colors.grey[600],
              fontSize: 12,
            ),
          ),
          const SizedBox(height: 4),
          Text(
            value,
            style: const TextStyle(
              fontWeight: FontWeight.bold,
              fontSize: 14,
            ),
          ),
        ],
      ),
    );
  }
}

class _ActionButton extends StatelessWidget {
  final IconData icon;
  final String label;
  final VoidCallback onTap;

  const _ActionButton({
    required this.icon,
    required this.label,
    required this.onTap,
  });

  @override
  Widget build(BuildContext context) {
    return InkWell(
      onTap: onTap,
      borderRadius: BorderRadius.circular(12),
      child: Container(
        padding: const EdgeInsets.symmetric(horizontal: 24, vertical: 12),
        decoration: BoxDecoration(
          color: const Color(0xFFF7FAFF),
          borderRadius: BorderRadius.circular(12),
          border: Border.all(color: const Color(0xFF0066FF).withOpacity(0.1)),
        ),
        child: Column(
          children: [
            Icon(icon, color: const Color(0xFF0066FF), size: 24),
            const SizedBox(height: 4),
            Text(
              label,
              style: GoogleFonts.inter(
                color: const Color(0xFF0066FF),
                fontSize: 12,
                fontWeight: FontWeight.w500,
              ),
            ),
          ],
        ),
      ),
    );
  }
}