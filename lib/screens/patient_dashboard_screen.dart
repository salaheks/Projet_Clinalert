import 'dart:math';
import 'package:clinalert/services/api_service.dart';
import 'package:clinalert/widgets/custom_app_bar.dart';
import 'package:clinalert/widgets/health_stat_card.dart';
import 'package:clinalert/widgets/modern_card.dart';
import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../providers/auth_provider.dart';
import 'patient_history_screen.dart';
import '../widgets/adaptive_scaffold.dart';
import '../widgets/chart_widget.dart';
import '../widgets/chat_icon_button.dart';
import 'package:go_router/go_router.dart';
import 'package:google_fonts/google_fonts.dart';

class PatientDashboardScreen extends StatefulWidget {
  const PatientDashboardScreen({super.key});

  @override
  State<PatientDashboardScreen> createState() => _PatientDashboardScreenState();
}

class _PatientDashboardScreenState extends State<PatientDashboardScreen> {
  final ApiService _apiService = ApiService();
  
  bool _isLoading = true;
  bool _isRefreshing = false;
  String? _error;
  
  // Health stats
  int? _latestHeartRate;
  double? _latestSpO2;
  int? _latestSteps;
  double? _avgHeartRate;
  
  // Alerts
  List<Map<String, dynamic>> _alerts = [];
  int _unreadAlertsCount = 0;
  
  // Devices
  List<Map<String, dynamic>> _devices = [];
  
  // Weekly data for chart
  List<double> _weeklyHeartRates = [];

  @override
  void initState() {
    super.initState();
    _loadData();
  }

  Future<void> _loadData() async {
    setState(() {
      _isLoading = true;
      _error = null;
    });

    try {
      final authProvider = context.read<AuthProvider>();
      final userId = authProvider.currentUser?.id;
      
      if (userId == null) {
        setState(() {
          _error = 'User not authenticated';
          _isLoading = false;
        });
        return;
      }

      // Load health stats
      try {
        final stats = await _apiService.getPatientHealthStats(userId);
        _latestHeartRate = stats['latestHeartRate'] as int?;
        _latestSpO2 = (stats['latestSpO2'] as num?)?.toDouble();
        _latestSteps = stats['latestSteps'] as int?;
        _avgHeartRate = (stats['avgHeartRate'] as num?)?.toDouble();
      } catch (e) {
        print('Could not load health stats: $e');
      }

      // Load alerts
      try {
        _alerts = await _apiService.getPatientAlerts(userId);
        _unreadAlertsCount = _alerts.where((a) => a['read'] != true).length;
      } catch (e) {
        print('Could not load alerts: $e');
      }

      // Load devices
      try {
        _devices = await _apiService.getPatientDevices(userId);
      } catch (e) {
        print('Could not load devices: $e');
      }

      // Load weekly heart rate data for chart
      try {
        final now = DateTime.now();
        final weekAgo = now.subtract(const Duration(days: 7));
        final healthData = await _apiService.getPatientHealthDataRange(userId, weekAgo, now);
        
        _weeklyHeartRates = healthData
            .where((d) => d['heartRate'] != null)
            .map((d) => (d['heartRate'] as num).toDouble())
            .take(7)
            .toList();
        
        // Pad with zeros if less than 7 data points
        while (_weeklyHeartRates.length < 7) {
          _weeklyHeartRates.insert(0, 0);
        }
      } catch (e) {
        print('Could not load weekly data: $e');
        _weeklyHeartRates = [0, 0, 0, 0, 0, 0, 0];
      }

      setState(() {
        _isLoading = false;
      });
    } catch (e) {
      setState(() {
        _error = 'Error loading data: $e';
        _isLoading = false;
      });
    }
  }

  /// Refresh today's health data from smartwatch and send to backend
  Future<void> _refreshTodayData() async {
    if (_isRefreshing) return;
    
    final authProvider = context.read<AuthProvider>();
    final userId = authProvider.currentUser?.id;
    
    if (userId == null) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('User not authenticated'), backgroundColor: Colors.red),
      );
      return;
    }
    
    if (_devices.isEmpty) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(
          content: Text('No smartwatch connected. Please connect a device first.'),
          backgroundColor: Colors.orange,
        ),
      );
      return;
    }
    
    setState(() => _isRefreshing = true);
    
    try {
      // Simulate fetching data from smartwatch (in real app, this would come from BLE)
      final random = Random();
      final now = DateTime.now();
      int successCount = 0;
      
      // Generate 5 health readings for today (simulating smartwatch data)
      for (int i = 0; i < 5; i++) {
        final heartRate = 60 + random.nextInt(40); // 60-100 bpm
        final spO2 = 95.0 + random.nextDouble() * 4; // 95-99%
        final steps = 100 + random.nextInt(500); // 100-600 steps
        final temperature = 36.0 + random.nextDouble() * 1.5; // 36-37.5°C
        
        // Create timestamp for this reading (spread across today)
        final readingTime = now.subtract(Duration(minutes: i * 30));
        
        // POST each reading as a NEW row to the backend
        try {
          await _apiService.submitSingleHealthData({
            'patientId': userId,
            'heartRate': heartRate,
            'spO2': spO2,
            'steps': steps,
            'temperature': temperature,
            'timestamp': readingTime.toIso8601String(),
            'source': 'smartwatch',
          });
          successCount++;
        } catch (e) {
          print('Error sending reading $i: $e');
        }
        
        // Small delay between requests
        await Future.delayed(const Duration(milliseconds: 200));
      }
      
      // Reload data to show new readings
      await _loadData();
      
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(
            content: Text('✓ $successCount health readings synced successfully!'),
            backgroundColor: Colors.green,
          ),
        );
      }
    } catch (e) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(
            content: Text('Error syncing data: $e'),
            backgroundColor: Colors.red,
          ),
        );
      }
    } finally {
      if (mounted) {
        setState(() => _isRefreshing = false);
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    final authProvider = context.read<AuthProvider>();
    final userName = authProvider.currentUser?.firstName ?? 'Patient';

    final appBar = CustomAppBar(
      title: 'My Health',
      user: authProvider.currentUser,
      onLogout: () => authProvider.logout(),
      actions: [
        ChatIconButton(
          onPressed: () => context.go('/chat-list'),
        ),
        Stack(
          children: [
            IconButton(
              icon: const Icon(Icons.notifications_outlined),
              onPressed: () => _showAlertsDialog(),
            ),
            if (_unreadAlertsCount > 0)
              Positioned(
                right: 8,
                top: 8,
                child: Container(
                  padding: const EdgeInsets.all(4),
                  decoration: const BoxDecoration(
                    color: Colors.red,
                    shape: BoxShape.circle,
                  ),
                  child: Text(
                    '$_unreadAlertsCount',
                    style: const TextStyle(
                      color: Colors.white,
                      fontSize: 10,
                      fontWeight: FontWeight.bold,
                    ),
                  ),
                ),
              ),
          ],
        ),
      ],
    );

    final body = _isLoading
        ? const Center(child: CircularProgressIndicator())
        : _error != null
            ? _buildErrorState()
            : RefreshIndicator(
                onRefresh: _loadData,
                child: _buildContent(userName),
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
        } else if (i == 2) {
          context.push('/settings');
        }
      },
    );
  }

  Widget _buildErrorState() {
    return Center(
      child: Column(
        mainAxisAlignment: MainAxisAlignment.center,
        children: [
          const Icon(Icons.error_outline, size: 64, color: Colors.red),
          const SizedBox(height: 16),
          Text(_error ?? 'Unknown error'),
          const SizedBox(height: 16),
          ElevatedButton(
            onPressed: _loadData,
            child: const Text('Retry'),
          ),
        ],
      ),
    );
  }

  Widget _buildContent(String userName) {
    return CustomScrollView(
      slivers: [
        SliverToBoxAdapter(
          child: Padding(
            padding: const EdgeInsets.fromLTRB(20, 16, 20, 8),
            child: _PatientHero(
              name: userName,
              hasAlerts: _unreadAlertsCount > 0,
              deviceCount: _devices.length,
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
                value: _latestHeartRate?.toString() ?? '--',
                unit: 'bpm',
                icon: Icons.favorite,
                iconColor: Colors.red,
                trend: _avgHeartRate != null ? 'Avg: ${_avgHeartRate!.toStringAsFixed(0)}' : null,
                isPositiveTrend: true,
                onTap: () => context.push('/health-data'),
              ),
              HealthStatCard(
                title: 'SpO2',
                value: _latestSpO2?.toStringAsFixed(0) ?? '--',
                unit: '%',
                icon: Icons.air,
                iconColor: Colors.cyan,
                trend: _latestSpO2 != null && _latestSpO2! >= 95 ? 'Normal' : 'Low',
                isPositiveTrend: _latestSpO2 != null && _latestSpO2! >= 95,
                onTap: () => context.push('/health-data'),
              ),
              HealthStatCard(
                title: 'Steps Today',
                value: _latestSteps?.toString() ?? '--',
                unit: 'steps',
                icon: Icons.directions_walk,
                iconColor: Colors.green,
                onTap: () => context.push('/health-data'),
              ),
              HealthStatCard(
                title: 'Devices',
                value: _devices.length.toString(),
                unit: 'connected',
                icon: Icons.watch,
                iconColor: Colors.indigo,
                onTap: () => context.push('/smartwatch'),
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
                        'Weekly Heart Rate',
                        style: Theme.of(context).textTheme.titleMedium?.copyWith(
                              fontWeight: FontWeight.bold,
                            ),
                      ),
                      TextButton(
                        onPressed: () => context.push('/health-data'),
                        child: const Text('View All'),
                      ),
                    ],
                  ),
                  const SizedBox(height: 16),
                  SizedBox(
                    height: 200,
                    child: ChartWidget(
                      title: '',
                      data: _weeklyHeartRates.isNotEmpty ? _weeklyHeartRates : [0, 0, 0, 0, 0, 0, 0],
                      color: const Color(0xFF0066FF),
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
                      'Quick Actions',
                      style: Theme.of(context).textTheme.titleMedium?.copyWith(
                            fontWeight: FontWeight.bold,
                          ),
                    ),
                  ),
                  // Refresh Today's Data Button
                  Container(
                    margin: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
                    width: double.infinity,
                    child: ElevatedButton.icon(
                      onPressed: _isRefreshing ? null : _refreshTodayData,
                      icon: _isRefreshing 
                          ? const SizedBox(
                              width: 20,
                              height: 20,
                              child: CircularProgressIndicator(
                                strokeWidth: 2,
                                color: Colors.white,
                              ),
                            )
                          : const Icon(Icons.refresh),
                      label: Text(_isRefreshing ? 'Syncing...' : '🔄 Refresh Today\'s Data'),
                      style: ElevatedButton.styleFrom(
                        backgroundColor: const Color(0xFF0066FF),
                        foregroundColor: Colors.white,
                        padding: const EdgeInsets.symmetric(vertical: 14),
                        shape: RoundedRectangleBorder(
                          borderRadius: BorderRadius.circular(12),
                        ),
                      ),
                    ),
                  ),
                  const Divider(height: 1),
                  _ActionTile(
                    icon: Icons.watch,
                    color: Colors.indigo,
                    title: 'Connect Smartwatch',
                    subtitle: _devices.isEmpty ? 'No device connected' : '${_devices.length} device(s) connected',
                    onTap: () => context.push('/smartwatch'),
                  ),
                  const Divider(height: 1),
                  _ActionTile(
                    icon: Icons.bluetooth_searching,
                    color: Colors.blue,
                    title: 'Scan BLE Device',
                    subtitle: 'Connect to medical devices',
                    onTap: () async {
                      final device = await context.push('/ble-scan');
                      if (device != null) {
                        context.push('/measurement', extra: device);
                      }
                    },
                  ),
                  const Divider(height: 1),
                  _ActionTile(
                    icon: Icons.show_chart,
                    color: Colors.green,
                    title: 'View Health Data',
                    subtitle: 'See detailed health metrics',
                    onTap: () => context.push('/health-data'),
                  ),
                  const Divider(height: 1),
                  _ActionTile(
                    icon: Icons.history,
                    color: Colors.orange,
                    title: 'View History',
                    subtitle: 'Check past records',
                    onTap: () => Navigator.push(context, MaterialPageRoute(builder: (_) => const PatientHistoryScreen())),
                  ),
                  const Divider(height: 1),
                  _ActionTile(
                    icon: Icons.notifications_active,
                    color: Colors.red,
                    title: 'Alerts',
                    subtitle: _unreadAlertsCount > 0 
                        ? '$_unreadAlertsCount unread alert(s)' 
                        : 'No new alerts',
                    onTap: () => _showAlertsDialog(),
                  ),
                ],
              ),
            ),
          ),
        ),
      ],
    );
  }

  void _showAlertsDialog() {
    showModalBottomSheet(
      context: context,
      isScrollControlled: true,
      shape: const RoundedRectangleBorder(
        borderRadius: BorderRadius.vertical(top: Radius.circular(20)),
      ),
      builder: (context) => DraggableScrollableSheet(
        initialChildSize: 0.6,
        minChildSize: 0.3,
        maxChildSize: 0.9,
        expand: false,
        builder: (context, scrollController) => Column(
          children: [
            Container(
              padding: const EdgeInsets.all(16),
              child: Row(
                mainAxisAlignment: MainAxisAlignment.spaceBetween,
                children: [
                  Text(
                    'Health Alerts',
                    style: Theme.of(context).textTheme.titleLarge?.copyWith(
                          fontWeight: FontWeight.bold,
                        ),
                  ),
                  IconButton(
                    icon: const Icon(Icons.close),
                    onPressed: () => Navigator.pop(context),
                  ),
                ],
              ),
            ),
            const Divider(height: 1),
            Expanded(
              child: _alerts.isEmpty
                  ? const Center(
                      child: Column(
                        mainAxisAlignment: MainAxisAlignment.center,
                        children: [
                          Icon(Icons.check_circle, size: 64, color: Colors.green),
                          SizedBox(height: 16),
                          Text('No alerts', style: TextStyle(fontSize: 18)),
                          Text('Your health looks good!', style: TextStyle(color: Colors.grey)),
                        ],
                      ),
                    )
                  : ListView.builder(
                      controller: scrollController,
                      itemCount: _alerts.length,
                      itemBuilder: (context, index) {
                        final alert = _alerts[index];
                        final severity = alert['severity'] ?? 'MEDIUM';
                        final isRead = alert['read'] == true;
                        
                        return ListTile(
                          leading: Container(
                            padding: const EdgeInsets.all(8),
                            decoration: BoxDecoration(
                              color: _getSeverityColor(severity).withOpacity(0.1),
                              borderRadius: BorderRadius.circular(8),
                            ),
                            child: Icon(
                              _getSeverityIcon(severity),
                              color: _getSeverityColor(severity),
                            ),
                          ),
                          title: Text(
                            alert['message'] ?? 'Alert',
                            style: TextStyle(
                              fontWeight: isRead ? FontWeight.normal : FontWeight.bold,
                            ),
                          ),
                          subtitle: Text(
                            _formatDate(alert['createdAt']),
                            style: const TextStyle(fontSize: 12),
                          ),
                          trailing: Container(
                            padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
                            decoration: BoxDecoration(
                              color: _getSeverityColor(severity),
                              borderRadius: BorderRadius.circular(12),
                            ),
                            child: Text(
                              severity,
                              style: const TextStyle(
                                color: Colors.white,
                                fontSize: 10,
                                fontWeight: FontWeight.bold,
                              ),
                            ),
                          ),
                          onTap: () async {
                            if (!isRead) {
                              try {
                                await _apiService.markAlertAsRead(alert['id']);
                                _loadData();
                              } catch (e) {
                                print('Error marking alert as read: $e');
                              }
                            }
                          },
                        );
                      },
                    ),
            ),
          ],
        ),
      ),
    );
  }

  Color _getSeverityColor(String severity) {
    switch (severity.toUpperCase()) {
      case 'CRITICAL':
        return Colors.red;
      case 'HIGH':
        return Colors.orange;
      case 'MEDIUM':
        return Colors.amber;
      case 'LOW':
        return Colors.green;
      default:
        return Colors.grey;
    }
  }

  IconData _getSeverityIcon(String severity) {
    switch (severity.toUpperCase()) {
      case 'CRITICAL':
        return Icons.error;
      case 'HIGH':
        return Icons.warning;
      case 'MEDIUM':
        return Icons.info;
      case 'LOW':
        return Icons.check_circle;
      default:
        return Icons.notifications;
    }
  }

  String _formatDate(String? dateStr) {
    if (dateStr == null) return '';
    try {
      final date = DateTime.parse(dateStr);
      final now = DateTime.now();
      final diff = now.difference(date);
      
      if (diff.inMinutes < 60) {
        return '${diff.inMinutes} min ago';
      } else if (diff.inHours < 24) {
        return '${diff.inHours} hours ago';
      } else {
        return '${diff.inDays} days ago';
      }
    } catch (e) {
      return dateStr;
    }
  }
}

class _PatientHero extends StatelessWidget {
  final String name;
  final bool hasAlerts;
  final int deviceCount;
  
  const _PatientHero({
    required this.name,
    this.hasAlerts = false,
    this.deviceCount = 0,
  });

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
                Icon(
                  hasAlerts ? Icons.warning : Icons.check_circle,
                  color: Colors.white,
                  size: 20,
                ),
                const SizedBox(width: 12),
                Expanded(
                  child: Text(
                    hasAlerts 
                        ? 'You have new health alerts'
                        : deviceCount > 0 
                            ? 'Your health status is being monitored'
                            : 'Connect a device to monitor your health',
                    style: GoogleFonts.inter(
                      color: Colors.white,
                      fontWeight: FontWeight.w500,
                    ),
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