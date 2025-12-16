import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../providers/auth_provider.dart';
import '../services/api_service.dart';
import '../themes/app_theme.dart';

class PatientHistoryScreen extends StatefulWidget {
  const PatientHistoryScreen({super.key});

  @override
  State<PatientHistoryScreen> createState() => _PatientHistoryScreenState();
}

class _PatientHistoryScreenState extends State<PatientHistoryScreen> {
  final ApiService _apiService = ApiService();
  
  bool _isLoading = true;
  String? _error;
  List<Map<String, dynamic>> _healthData = [];
  int _selectedDays = 7;

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

      final now = DateTime.now();
      final start = now.subtract(Duration(days: _selectedDays));
      
      _healthData = await _apiService.getPatientHealthDataRange(userId, start, now);
      
      // Sort by timestamp descending (most recent first)
      _healthData.sort((a, b) {
        final aTime = a['timestamp'] ?? '';
        final bTime = b['timestamp'] ?? '';
        return bTime.compareTo(aTime);
      });

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

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Health History'),
        backgroundColor: AppThemes.primaryBlue,
        foregroundColor: Colors.white,
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
        ],
      ),
      body: _isLoading
          ? const Center(child: CircularProgressIndicator())
          : _error != null
              ? _buildErrorState()
              : _healthData.isEmpty
                  ? _buildEmptyState()
                  : RefreshIndicator(
                      onRefresh: _loadData,
                      child: ListView.separated(
                        padding: const EdgeInsets.all(16),
                        itemCount: _healthData.length,
                        separatorBuilder: (_, __) => const SizedBox(height: 8),
                        itemBuilder: (context, index) {
                          final data = _healthData[index];
                          return _buildHealthDataCard(data);
                        },
                      ),
                    ),
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

  Widget _buildEmptyState() {
    return Center(
      child: Column(
        mainAxisAlignment: MainAxisAlignment.center,
        children: [
          Icon(Icons.history, size: 64, color: Colors.grey[400]),
          const SizedBox(height: 16),
          Text(
            'No health data',
            style: TextStyle(fontSize: 18, color: Colors.grey[600]),
          ),
          const SizedBox(height: 8),
          Text(
            'Connect a smartwatch to start recording',
            style: TextStyle(color: Colors.grey[500]),
          ),
        ],
      ),
    );
  }

  Widget _buildHealthDataCard(Map<String, dynamic> data) {
    final heartRate = data['heartRate'] as int?;
    final spO2 = data['spO2'] as num?;
    final steps = data['steps'] as int?;
    final temperature = data['temperature'] as num?;
    final timestamp = data['timestamp'] as String?;

    return Card(
      elevation: 2,
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                Text(
                  _formatDate(timestamp),
                  style: TextStyle(
                    color: Colors.grey[600],
                    fontSize: 12,
                    fontWeight: FontWeight.w500,
                  ),
                ),
                Text(
                  _formatTime(timestamp),
                  style: TextStyle(
                    color: Colors.grey[500],
                    fontSize: 12,
                  ),
                ),
              ],
            ),
            const SizedBox(height: 12),
            Wrap(
              spacing: 16,
              runSpacing: 8,
              children: [
                if (heartRate != null)
                  _buildMetric(Icons.favorite, Colors.red, '$heartRate', 'bpm'),
                if (spO2 != null)
                  _buildMetric(Icons.air, Colors.cyan, '${spO2.toStringAsFixed(0)}', '%'),
                if (steps != null)
                  _buildMetric(Icons.directions_walk, Colors.green, '$steps', 'steps'),
                if (temperature != null)
                  _buildMetric(Icons.thermostat, Colors.orange, '${temperature.toStringAsFixed(1)}', '°C'),
              ],
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildMetric(IconData icon, Color color, String value, String unit) {
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 6),
      decoration: BoxDecoration(
        color: color.withOpacity(0.1),
        borderRadius: BorderRadius.circular(20),
      ),
      child: Row(
        mainAxisSize: MainAxisSize.min,
        children: [
          Icon(icon, color: color, size: 16),
          const SizedBox(width: 6),
          Text(
            value,
            style: TextStyle(
              color: color,
              fontWeight: FontWeight.bold,
            ),
          ),
          const SizedBox(width: 2),
          Text(
            unit,
            style: TextStyle(
              color: color.withOpacity(0.8),
              fontSize: 12,
            ),
          ),
        ],
      ),
    );
  }

  String _formatDate(String? dateStr) {
    if (dateStr == null) return '';
    try {
      final date = DateTime.parse(dateStr);
      final now = DateTime.now();
      final diff = now.difference(date);
      
      if (diff.inDays == 0) {
        return 'Today';
      } else if (diff.inDays == 1) {
        return 'Yesterday';
      } else {
        return '${date.day}/${date.month}/${date.year}';
      }
    } catch (e) {
      return dateStr;
    }
  }

  String _formatTime(String? dateStr) {
    if (dateStr == null) return '';
    try {
      final date = DateTime.parse(dateStr);
      return '${date.hour.toString().padLeft(2, '0')}:${date.minute.toString().padLeft(2, '0')}';
    } catch (e) {
      return '';
    }
  }
}
