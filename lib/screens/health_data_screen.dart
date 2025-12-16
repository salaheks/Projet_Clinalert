import 'package:flutter/material.dart';
import 'package:google_fonts/google_fonts.dart';
import 'package:fl_chart/fl_chart.dart';
import '../models/health_data_model.dart';
import '../models/daily_health_summary_model.dart';
import '../services/api_service.dart';
import '../widgets/modern_card.dart';

class HealthDataScreen extends StatefulWidget {
  final String patientId;
  final String? patientName;

  const HealthDataScreen({
    super.key,
    required this.patientId,
    this.patientName,
  });

  @override
  State<HealthDataScreen> createState() => _HealthDataScreenState();
}

class _HealthDataScreenState extends State<HealthDataScreen> with SingleTickerProviderStateMixin {
  final ApiService _apiService = ApiService();
  late TabController _tabController;
  
  List<HealthData> _healthData = [];
  List<DailyHealthSummary> _dailySummaries = [];
  Map<String, dynamic>? _stats;
  
  bool _isLoading = true;
  String? _error;
  
  // Selected time range
  int _selectedDays = 7;

  @override
  void initState() {
    super.initState();
    _tabController = TabController(length: 4, vsync: this);
    _loadData();
  }

  @override
  void dispose() {
    _tabController.dispose();
    super.dispose();
  }

  Future<void> _loadData() async {
    setState(() {
      _isLoading = true;
      _error = null;
    });

    try {
      final results = await Future.wait([
        _apiService.getPatientHealthData(widget.patientId),
        _apiService.getRecentDailySummaries(widget.patientId),
        _apiService.getPatientHealthStats(widget.patientId),
      ]);

      setState(() {
        _healthData = (results[0] as List<Map<String, dynamic>>)
            .map((d) => HealthData.fromJson(d))
            .toList();
        _dailySummaries = (results[1] as List<Map<String, dynamic>>)
            .map((d) => DailyHealthSummary.fromJson(d))
            .toList();
        _stats = results[2] as Map<String, dynamic>?;
        _isLoading = false;
      });
    } catch (e) {
      print('Error loading health data: $e');
      setState(() {
        _error = e.toString();
        _isLoading = false;
      });
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text(widget.patientName != null && widget.patientName!.isNotEmpty
            ? '${widget.patientName}\'s Health' 
            : 'Health Data'),
        actions: [
          PopupMenuButton<int>(
            icon: const Icon(Icons.calendar_today),
            onSelected: (days) {
              setState(() => _selectedDays = days);
              _loadData();
            },
            itemBuilder: (context) => [
              const PopupMenuItem(value: 7, child: Text('Last 7 days')),
              const PopupMenuItem(value: 14, child: Text('Last 14 days')),
              const PopupMenuItem(value: 30, child: Text('Last 30 days')),
            ],
          ),
          IconButton(
            icon: const Icon(Icons.refresh),
            onPressed: _loadData,
          ),
        ],
        bottom: TabBar(
          controller: _tabController,
          tabs: const [
            Tab(icon: Icon(Icons.dashboard), text: 'Overview'),
            Tab(icon: Icon(Icons.favorite), text: 'Heart'),
            Tab(icon: Icon(Icons.directions_walk), text: 'Activity'),
            Tab(icon: Icon(Icons.nightlight), text: 'Sleep'),
          ],
        ),
      ),
      body: _isLoading
          ? const Center(child: CircularProgressIndicator())
          : _error != null
              ? _buildErrorState()
              : TabBarView(
                  controller: _tabController,
                  children: [
                    _buildOverviewTab(),
                    _buildHeartTab(),
                    _buildActivityTab(),
                    _buildSleepTab(),
                  ],
                ),
    );
  }

  Widget _buildErrorState() {
    return Center(
      child: Column(
        mainAxisAlignment: MainAxisAlignment.center,
        children: [
          Icon(Icons.error_outline, size: 64, color: Colors.grey[400]),
          const SizedBox(height: 16),
          Text(
            'Error loading data',
            style: GoogleFonts.inter(fontSize: 18, fontWeight: FontWeight.bold),
          ),
          const SizedBox(height: 8),
          Text(
            _error ?? 'Unknown error',
            style: TextStyle(color: Colors.grey[600]),
            textAlign: TextAlign.center,
          ),
          const SizedBox(height: 24),
          ElevatedButton.icon(
            onPressed: _loadData,
            icon: const Icon(Icons.refresh),
            label: const Text('Retry'),
          ),
        ],
      ),
    );
  }

  Widget _buildOverviewTab() {
    final latestData = _healthData.isNotEmpty ? _healthData.first : null;
    final todaySummary = _dailySummaries.isNotEmpty ? _dailySummaries.first : null;

    return RefreshIndicator(
      onRefresh: _loadData,
      child: SingleChildScrollView(
        physics: const AlwaysScrollableScrollPhysics(),
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            // Current Stats Header
            Text(
              'Current Stats',
              style: GoogleFonts.inter(
                fontSize: 18,
                fontWeight: FontWeight.bold,
              ),
            ),
            const SizedBox(height: 12),
            
            // Quick Stats Grid
            GridView.count(
              shrinkWrap: true,
              physics: const NeverScrollableScrollPhysics(),
              crossAxisCount: 2,
              mainAxisSpacing: 12,
              crossAxisSpacing: 12,
              childAspectRatio: 1.1, // Reduced from 1.3 for more vertical space
              children: [
                _buildStatCard(
                  icon: Icons.favorite,
                  iconColor: Colors.red,
                  title: 'Heart Rate',
                  value: latestData?.heartRateDisplay ?? '--',
                  subtitle: _stats?['avgHeartRate'] != null 
                      ? 'Avg: ${(_stats!['avgHeartRate'] as num).toStringAsFixed(0)} bpm'
                      : null,
                ),
                _buildStatCard(
                  icon: Icons.air,
                  iconColor: Colors.blue,
                  title: 'SpO2',
                  value: latestData?.spO2Display ?? '--%',
                  subtitle: 'Blood Oxygen',
                ),
                _buildStatCard(
                  icon: Icons.directions_walk,
                  iconColor: Colors.green,
                  title: 'Steps',
                  value: todaySummary?.stepsDisplay ?? '--',
                  subtitle: todaySummary != null 
                      ? '${(todaySummary.stepsProgress * 100).toStringAsFixed(0)}% of goal'
                      : null,
                  progress: todaySummary?.stepsProgress,
                ),
                _buildStatCard(
                  icon: Icons.nightlight,
                  iconColor: Colors.purple,
                  title: 'Sleep',
                  value: todaySummary?.sleepDisplay ?? '--',
                  subtitle: todaySummary != null
                      ? '${(todaySummary.sleepProgress * 100).toStringAsFixed(0)}% of goal'
                      : null,
                  progress: todaySummary?.sleepProgress,
                ),
              ],
            ),

            const SizedBox(height: 24),

            // Heart Rate Trend
            Text(
              'Heart Rate Trend',
              style: GoogleFonts.inter(
                fontSize: 18,
                fontWeight: FontWeight.bold,
              ),
            ),
            const SizedBox(height: 12),
            SizedBox(
              height: 200,
              child: _buildHeartRateChart(),
            ),

            const SizedBox(height: 24),

            // Recent Activity
            Text(
              'Recent Data',
              style: GoogleFonts.inter(
                fontSize: 18,
                fontWeight: FontWeight.bold,
              ),
            ),
            const SizedBox(height: 12),
            ..._healthData.take(5).map((data) => _buildDataTile(data)),
          ],
        ),
      ),
    );
  }

  Widget _buildStatCard({
    required IconData icon,
    required Color iconColor,
    required String title,
    required String value,
    String? subtitle,
    double? progress,
  }) {
    return ModernCard(
      padding: const EdgeInsets.all(12), // Reduced from 16
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        mainAxisSize: MainAxisSize.min, // Prevent expansion
        children: [
          Row(
            children: [
              Container(
                padding: const EdgeInsets.all(6), // Reduced from 8
                decoration: BoxDecoration(
                  color: iconColor.withOpacity(0.1),
                  borderRadius: BorderRadius.circular(8),
                ),
                child: Icon(icon, color: iconColor, size: 18), // Reduced from 20
              ),
              const Spacer(),
              if (progress != null)
                SizedBox(
                  width: 28, // Reduced from 32
                  height: 28,
                  child: CircularProgressIndicator(
                    value: progress,
                    strokeWidth: 2.5, // Reduced from 3
                    backgroundColor: Colors.grey[200],
                    valueColor: AlwaysStoppedAnimation<Color>(iconColor),
                  ),
                ),
            ],
          ),
          const SizedBox(height: 8), // Fixed spacing instead of Spacer
          Text(
            value,
            style: GoogleFonts.inter(
              fontSize: 20, // Reduced from 22
              fontWeight: FontWeight.bold,
            ),
            overflow: TextOverflow.ellipsis, // Prevent text overflow
          ),
          Text(
            title,
            style: TextStyle(
              color: Colors.grey[600],
              fontSize: 11, // Reduced from 12
            ),
            overflow: TextOverflow.ellipsis,
          ),
          if (subtitle != null)
            Padding(
              padding: const EdgeInsets.only(top: 2),
              child: Text(
                subtitle,
                style: TextStyle(
                  color: Colors.grey[400],
                  fontSize: 9, // Reduced from 10
                ),
                overflow: TextOverflow.ellipsis,
              ),
            ),
        ],
      ),
    );
  }

  Widget _buildHeartRateChart() {
    final heartRateData = _healthData
        .where((d) => d.heartRate != null)
        .take(20)
        .toList()
        .reversed
        .toList();

    if (heartRateData.isEmpty) {
      return Center(
        child: Text(
          'No heart rate data available',
          style: TextStyle(color: Colors.grey[500]),
        ),
      );
    }

    return ModernCard(
      padding: const EdgeInsets.all(16),
      child: LineChart(
        LineChartData(
          gridData: FlGridData(show: false),
          titlesData: FlTitlesData(show: false),
          borderData: FlBorderData(show: false),
          lineBarsData: [
            LineChartBarData(
              spots: heartRateData.asMap().entries.map((entry) {
                return FlSpot(entry.key.toDouble(), entry.value.heartRate!.toDouble());
              }).toList(),
              isCurved: true,
              color: Colors.red,
              barWidth: 3,
              dotData: FlDotData(show: false),
              belowBarData: BarAreaData(
                show: true,
                color: Colors.red.withOpacity(0.1),
              ),
            ),
          ],
          minY: 40,
          maxY: 140,
        ),
      ),
    );
  }

  Widget _buildDataTile(HealthData data) {
    return Container(
      margin: const EdgeInsets.only(bottom: 8),
      padding: const EdgeInsets.all(12),
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(10),
        border: Border.all(color: Colors.grey.withOpacity(0.1)),
      ),
      child: Row(
        children: [
          if (data.heartRate != null) ...[
            _buildMiniMetric(Icons.favorite, '${data.heartRate}', 'bpm', Colors.red),
            const SizedBox(width: 16),
          ],
          if (data.spO2 != null) ...[
            _buildMiniMetric(Icons.air, '${data.spO2!.toStringAsFixed(0)}', '%', Colors.blue),
            const SizedBox(width: 16),
          ],
          if (data.steps != null) ...[
            _buildMiniMetric(Icons.directions_walk, '${data.steps}', '', Colors.green),
          ],
          const Spacer(),
          Text(
            _formatTime(data.timestamp),
            style: TextStyle(color: Colors.grey[500], fontSize: 12),
          ),
        ],
      ),
    );
  }

  Widget _buildMiniMetric(IconData icon, String value, String unit, Color color) {
    return Row(
      children: [
        Icon(icon, color: color, size: 16),
        const SizedBox(width: 4),
        Text(
          '$value$unit',
          style: GoogleFonts.inter(
            fontWeight: FontWeight.w600,
            fontSize: 14,
          ),
        ),
      ],
    );
  }

  String _formatTime(DateTime dt) {
    final now = DateTime.now();
    final diff = now.difference(dt);
    
    if (diff.inMinutes < 1) return 'Just now';
    if (diff.inMinutes < 60) return '${diff.inMinutes}m ago';
    if (diff.inHours < 24) return '${diff.inHours}h ago';
    return '${diff.inDays}d ago';
  }

  Widget _buildHeartTab() {
    return RefreshIndicator(
      onRefresh: _loadData,
      child: SingleChildScrollView(
        physics: const AlwaysScrollableScrollPhysics(),
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            _buildHeartRateSummaryCard(),
            const SizedBox(height: 16),
            Text(
              'Heart Rate History',
              style: GoogleFonts.inter(fontSize: 16, fontWeight: FontWeight.bold),
            ),
            const SizedBox(height: 12),
            SizedBox(height: 250, child: _buildHeartRateChart()),
            const SizedBox(height: 24),
            Text(
              'Daily Averages',
              style: GoogleFonts.inter(fontSize: 16, fontWeight: FontWeight.bold),
            ),
            const SizedBox(height: 12),
            ..._dailySummaries.take(7).map((s) => _buildDailyHeartRateTile(s)),
          ],
        ),
      ),
    );
  }

  Widget _buildHeartRateSummaryCard() {
    final latestData = _healthData.firstWhere(
      (d) => d.heartRate != null,
      orElse: () => HealthData(id: '', patientId: '', timestamp: DateTime.now()),
    );

    return ModernCard(
      padding: const EdgeInsets.all(20),
      child: Row(
        children: [
          Container(
            padding: const EdgeInsets.all(16),
            decoration: BoxDecoration(
              color: Colors.red.withOpacity(0.1),
              borderRadius: BorderRadius.circular(16),
            ),
            child: const Icon(Icons.favorite, color: Colors.red, size: 32),
          ),
          const SizedBox(width: 20),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  latestData.heartRateDisplay,
                  style: GoogleFonts.inter(fontSize: 28, fontWeight: FontWeight.bold),
                ),
                Text(
                  'Current Heart Rate',
                  style: TextStyle(color: Colors.grey[600]),
                ),
              ],
            ),
          ),
          Column(
            crossAxisAlignment: CrossAxisAlignment.end,
            children: [
              Text(
                latestData.isHeartRateNormal ? 'Normal' : 'Abnormal',
                style: TextStyle(
                  color: latestData.isHeartRateNormal ? Colors.green : Colors.orange,
                  fontWeight: FontWeight.bold,
                ),
              ),
              Text(
                _formatTime(latestData.timestamp),
                style: TextStyle(color: Colors.grey[500], fontSize: 12),
              ),
            ],
          ),
        ],
      ),
    );
  }

  Widget _buildDailyHeartRateTile(DailyHealthSummary summary) {
    return Container(
      margin: const EdgeInsets.only(bottom: 8),
      padding: const EdgeInsets.all(12),
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(10),
        border: Border.all(color: Colors.grey.withOpacity(0.1)),
      ),
      child: Row(
        children: [
          Text(
            summary.dateFormatted,
            style: GoogleFonts.inter(fontWeight: FontWeight.w500),
          ),
          const Spacer(),
          Text(summary.avgHeartRateDisplay),
          const SizedBox(width: 16),
          Text(
            summary.heartRateRangeDisplay,
            style: TextStyle(color: Colors.grey[500], fontSize: 12),
          ),
        ],
      ),
    );
  }

  Widget _buildActivityTab() {
    final todaySummary = _dailySummaries.isNotEmpty ? _dailySummaries.first : null;

    return RefreshIndicator(
      onRefresh: _loadData,
      child: SingleChildScrollView(
        physics: const AlwaysScrollableScrollPhysics(),
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            _buildActivitySummaryCard(todaySummary),
            const SizedBox(height: 24),
            Text(
              'Weekly Steps',
              style: GoogleFonts.inter(fontSize: 16, fontWeight: FontWeight.bold),
            ),
            const SizedBox(height: 12),
            SizedBox(height: 200, child: _buildStepsBarChart()),
            const SizedBox(height: 24),
            Text(
              'Activity History',
              style: GoogleFonts.inter(fontSize: 16, fontWeight: FontWeight.bold),
            ),
            const SizedBox(height: 12),
            ..._dailySummaries.take(7).map((s) => _buildDailyActivityTile(s)),
          ],
        ),
      ),
    );
  }

  Widget _buildActivitySummaryCard(DailyHealthSummary? summary) {
    return ModernCard(
      padding: const EdgeInsets.all(20),
      child: Column(
        children: [
          Row(
            children: [
              Expanded(
                child: _buildActivityMetric(
                  icon: Icons.directions_walk,
                  color: Colors.green,
                  value: summary?.stepsDisplay ?? '--',
                  label: 'Steps',
                ),
              ),
              Expanded(
                child: _buildActivityMetric(
                  icon: Icons.local_fire_department,
                  color: Colors.orange,
                  value: summary?.caloriesDisplay ?? '--',
                  label: 'Calories',
                ),
              ),
              Expanded(
                child: _buildActivityMetric(
                  icon: Icons.straighten,
                  color: Colors.blue,
                  value: summary?.distanceDisplay ?? '--',
                  label: 'Distance',
                ),
              ),
            ],
          ),
          if (summary != null) ...[
            const SizedBox(height: 16),
            LinearProgressIndicator(
              value: summary.stepsProgress,
              backgroundColor: Colors.grey[200],
              valueColor: const AlwaysStoppedAnimation<Color>(Colors.green),
            ),
            const SizedBox(height: 8),
            Text(
              '${(summary.stepsProgress * 100).toStringAsFixed(0)}% of 10,000 steps goal',
              style: TextStyle(color: Colors.grey[600], fontSize: 12),
            ),
          ],
        ],
      ),
    );
  }

  Widget _buildActivityMetric({
    required IconData icon,
    required Color color,
    required String value,
    required String label,
  }) {
    return Column(
      children: [
        Icon(icon, color: color, size: 28),
        const SizedBox(height: 8),
        Text(
          value,
          style: GoogleFonts.inter(fontSize: 18, fontWeight: FontWeight.bold),
        ),
        Text(
          label,
          style: TextStyle(color: Colors.grey[600], fontSize: 12),
        ),
      ],
    );
  }

  Widget _buildStepsBarChart() {
    final weekData = _dailySummaries.take(7).toList().reversed.toList();
    
    if (weekData.isEmpty) {
      return Center(
        child: Text(
          'No activity data available',
          style: TextStyle(color: Colors.grey[500]),
        ),
      );
    }

    return ModernCard(
      padding: const EdgeInsets.all(16),
      child: BarChart(
        BarChartData(
          gridData: FlGridData(show: false),
          titlesData: FlTitlesData(
            show: true,
            bottomTitles: AxisTitles(
              sideTitles: SideTitles(
                showTitles: true,
                getTitlesWidget: (value, meta) {
                  final index = value.toInt();
                  if (index >= 0 && index < weekData.length) {
                    final date = weekData[index].date;
                    final days = ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun'];
                    return Text(
                      days[date.weekday - 1],
                      style: TextStyle(color: Colors.grey[600], fontSize: 10),
                    );
                  }
                  return const Text('');
                },
              ),
            ),
            leftTitles: AxisTitles(sideTitles: SideTitles(showTitles: false)),
            rightTitles: AxisTitles(sideTitles: SideTitles(showTitles: false)),
            topTitles: AxisTitles(sideTitles: SideTitles(showTitles: false)),
          ),
          borderData: FlBorderData(show: false),
          barGroups: weekData.asMap().entries.map((entry) {
            return BarChartGroupData(
              x: entry.key,
              barRods: [
                BarChartRodData(
                  toY: (entry.value.totalSteps ?? 0).toDouble(),
                  color: Colors.green,
                  width: 20,
                  borderRadius: const BorderRadius.vertical(top: Radius.circular(4)),
                ),
              ],
            );
          }).toList(),
        ),
      ),
    );
  }

  Widget _buildDailyActivityTile(DailyHealthSummary summary) {
    return Container(
      margin: const EdgeInsets.only(bottom: 8),
      padding: const EdgeInsets.all(12),
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(10),
        border: Border.all(color: Colors.grey.withOpacity(0.1)),
      ),
      child: Row(
        children: [
          Text(
            summary.dateFormatted,
            style: GoogleFonts.inter(fontWeight: FontWeight.w500),
          ),
          const Spacer(),
          Text(summary.stepsDisplay),
          const SizedBox(width: 12),
          Text(summary.caloriesDisplay, style: TextStyle(color: Colors.grey[500])),
        ],
      ),
    );
  }

  Widget _buildSleepTab() {
    final todaySummary = _dailySummaries.isNotEmpty ? _dailySummaries.first : null;

    return RefreshIndicator(
      onRefresh: _loadData,
      child: SingleChildScrollView(
        physics: const AlwaysScrollableScrollPhysics(),
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            _buildSleepSummaryCard(todaySummary),
            const SizedBox(height: 24),
            Text(
              'Sleep History',
              style: GoogleFonts.inter(fontSize: 16, fontWeight: FontWeight.bold),
            ),
            const SizedBox(height: 12),
            ..._dailySummaries.take(7).map((s) => _buildDailySleepTile(s)),
          ],
        ),
      ),
    );
  }

  Widget _buildSleepSummaryCard(DailyHealthSummary? summary) {
    return ModernCard(
      padding: const EdgeInsets.all(20),
      child: Column(
        children: [
          Row(
            children: [
              Container(
                padding: const EdgeInsets.all(16),
                decoration: BoxDecoration(
                  color: Colors.purple.withOpacity(0.1),
                  borderRadius: BorderRadius.circular(16),
                ),
                child: const Icon(Icons.nightlight, color: Colors.purple, size: 32),
              ),
              const SizedBox(width: 20),
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      summary?.sleepDisplay ?? '--h --m',
                      style: GoogleFonts.inter(fontSize: 28, fontWeight: FontWeight.bold),
                    ),
                    Text(
                      'Last Night\'s Sleep',
                      style: TextStyle(color: Colors.grey[600]),
                    ),
                  ],
                ),
              ),
            ],
          ),
          if (summary != null) ...[
            const SizedBox(height: 16),
            LinearProgressIndicator(
              value: summary.sleepProgress,
              backgroundColor: Colors.grey[200],
              valueColor: const AlwaysStoppedAnimation<Color>(Colors.purple),
            ),
            const SizedBox(height: 8),
            Text(
              '${(summary.sleepProgress * 100).toStringAsFixed(0)}% of 8 hours goal',
              style: TextStyle(color: Colors.grey[600], fontSize: 12),
            ),
          ],
        ],
      ),
    );
  }

  Widget _buildDailySleepTile(DailyHealthSummary summary) {
    return Container(
      margin: const EdgeInsets.only(bottom: 8),
      padding: const EdgeInsets.all(12),
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(10),
        border: Border.all(color: Colors.grey.withOpacity(0.1)),
      ),
      child: Row(
        children: [
          Text(
            summary.dateFormatted,
            style: GoogleFonts.inter(fontWeight: FontWeight.w500),
          ),
          const Spacer(),
          Text(summary.sleepDisplay),
          const SizedBox(width: 12),
          SizedBox(
            width: 50,
            child: LinearProgressIndicator(
              value: summary.sleepProgress,
              backgroundColor: Colors.grey[200],
              valueColor: const AlwaysStoppedAnimation<Color>(Colors.purple),
            ),
          ),
        ],
      ),
    );
  }
}
