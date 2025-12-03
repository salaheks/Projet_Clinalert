import 'package:clinalert/models/patient_model.dart';
import 'package:clinalert/models/user_model.dart';
import 'package:clinalert/models/vital_sign_model.dart';
import 'package:clinalert/widgets/custom_app_bar.dart';
import 'package:clinalert/widgets/health_stat_card.dart';
import 'package:clinalert/widgets/modern_card.dart';
import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../providers/auth_provider.dart';
import 'patient_history_screen.dart';
import 'chat_list_screen.dart';
import '../widgets/adaptive_scaffold.dart';
import '../widgets/chart_widget.dart';
import '../widgets/chat_icon_button.dart';
import 'package:go_router/go_router.dart';
import 'package:google_fonts/google_fonts.dart';

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

    final appBar = CustomAppBar(
      title: 'My Health',
      user: authProvider.currentUser,
      onLogout: () => authProvider.logout(),
      actions: [
        ChatIconButton(
          onPressed: () => context.go('/chat-list'),
        ),
        IconButton(
          icon: const Icon(Icons.notifications_outlined),
          onPressed: () {
            // Navigate to alerts
          },
        ),
      ],
    );

    final body = CustomScrollView(
      slivers: [
        SliverToBoxAdapter(
          child: Padding(
            padding: const EdgeInsets.fromLTRB(20, 16, 20, 8),
            child: _PatientHero(name: patient.user.firstName),
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
                value: '82',
                unit: 'bpm',
                icon: Icons.favorite,
                iconColor: Colors.red,
                trend: '+2%',
                isPositiveTrend: false, // Higher HR might be bad
                onTap: () {},
              ),
              HealthStatCard(
                title: 'Blood Pressure',
                value: '125/82',
                unit: 'mmHg',
                icon: Icons.water_drop,
                iconColor: Colors.blue,
                trend: 'Stable',
                isPositiveTrend: true,
                onTap: () {},
              ),
              HealthStatCard(
                title: 'Temperature',
                value: '37.1',
                unit: '°C',
                icon: Icons.thermostat,
                iconColor: Colors.orange,
                onTap: () {},
              ),
              HealthStatCard(
                title: 'SpO2',
                value: '97',
                unit: '%',
                icon: Icons.air,
                iconColor: Colors.cyan,
                trend: '98% avg',
                isPositiveTrend: true,
                onTap: () {},
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
                  Row(
                    mainAxisAlignment: MainAxisAlignment.spaceBetween,
                    children: [
                      Text(
                        'Weekly Analysis',
                        style: Theme.of(context).textTheme.titleMedium?.copyWith(
                              fontWeight: FontWeight.bold,
                            ),
                      ),
                      TextButton(
                        onPressed: () {},
                        child: const Text('View All'),
                      ),
                    ],
                  ),
                  const SizedBox(height: 16),
                  const SizedBox(
                    height: 200,
                    child: ChartWidget(
                      title: '',
                      data: [78.0, 80.0, 76.0, 82.0, 79.0, 85.0, 81.0],
                      color: Color(0xFF0066FF),
                    ),
                  ),
                ],
              ),
            ),
          ),
        ),
        SliverToBoxAdapter(
          child: Padding(
            padding: const EdgeInsets.fromLTRB(20, 8, 20, 100),
            child: ModernCard(
              padding: EdgeInsets.zero,
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Padding(
                    padding: const EdgeInsets.all(20),
                    child: Text(
                      'Recent Actions',
                      style: Theme.of(context).textTheme.titleMedium?.copyWith(
                            fontWeight: FontWeight.bold,
                          ),
                    ),
                  ),
                  _ActionTile(
                    icon: Icons.bluetooth_searching,
                    color: Colors.indigo,
                    title: 'Scan New Device',
                    subtitle: 'Connect to BLE medical devices',
                    onTap: () async {
                      final device = await context.push('/ble-scan');
                      if (device != null) {
                        // ignore: use_build_context_synchronously
                        context.push('/measurement', extra: device);
                      }
                    },
                  ),
                  const Divider(height: 1),
                  _ActionTile(
                    icon: Icons.cloud_upload_outlined,
                    color: Colors.green,
                    title: 'Send Data to Doctor',
                    subtitle: 'Share your latest measurements',
                    onTap: () => context.push('/send-to-doctor'),
                  ),
                  const Divider(height: 1),
                  _ActionTile(
                    icon: Icons.history,
                    color: Colors.orange,
                    title: 'View History',
                    subtitle: 'Check past records',
                    onTap: () => Navigator.push(context, MaterialPageRoute(builder: (_) => const PatientHistoryScreen())),
                  ),
                ],
              ),
            ),
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
      floatingActionButton: null, // Removed FABs in favor of Action Tiles for cleaner UI
    );
  }
}

class _PatientHero extends StatelessWidget {
  final String name;
  const _PatientHero({required this.name});
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
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    'Welcome back,',
                    style: GoogleFonts.inter(
                      color: Colors.white.withOpacity(0.9),
                      fontSize: 16,
                    ),
                  ),
                  const SizedBox(height: 4),
                  Text(
                    name,
                    style: GoogleFonts.inter(
                      color: Colors.white,
                      fontSize: 28,
                      fontWeight: FontWeight.bold,
                    ),
                  ),
                ],
              ),
              Container(
                padding: const EdgeInsets.all(12),
                decoration: BoxDecoration(
                  color: Colors.white.withOpacity(0.2),
                  shape: BoxShape.circle,
                ),
                child: const Icon(Icons.person, color: Colors.white, size: 28),
              ),
            ],
          ),
          const SizedBox(height: 24),
          Container(
            padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
            decoration: BoxDecoration(
              color: Colors.white.withOpacity(0.15),
              borderRadius: BorderRadius.circular(16),
            ),
            child: Row(
              children: [
                const Icon(Icons.check_circle, color: Colors.white, size: 20),
                const SizedBox(width: 12),
                Text(
                  'Your health status is good today',
                  style: GoogleFonts.inter(
                    color: Colors.white,
                    fontWeight: FontWeight.w500,
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

class _ActionTile extends StatelessWidget {
  final IconData icon;
  final Color color;
  final String title;
  final String subtitle;
  final VoidCallback onTap;

  const _ActionTile({
    required this.icon,
    required this.color,
    required this.title,
    required this.subtitle,
    required this.onTap,
  });

  @override
  Widget build(BuildContext context) {
    return ListTile(
      onTap: onTap,
      contentPadding: const EdgeInsets.symmetric(horizontal: 20, vertical: 8),
      leading: Container(
        padding: const EdgeInsets.all(10),
        decoration: BoxDecoration(
          color: color.withOpacity(0.1),
          borderRadius: BorderRadius.circular(12),
        ),
        child: Icon(icon, color: color),
      ),
      title: Text(
        title,
        style: const TextStyle(fontWeight: FontWeight.w600),
      ),
      subtitle: Text(subtitle),
      trailing: const Icon(Icons.chevron_right, color: Colors.grey),
    );
  }
}