import 'package:clinalert/models/patient_model.dart';
import 'package:clinalert/models/vital_sign_model.dart';
import 'package:clinalert/models/alert_model.dart';
import 'package:clinalert/models/health_data_model.dart';
import 'package:clinalert/models/smartwatch_device_model.dart';
import 'package:clinalert/widgets/custom_app_bar.dart';
import 'package:clinalert/widgets/health_stat_card.dart';
import 'package:clinalert/widgets/modern_card.dart';
import 'package:clinalert/widgets/modern_badge.dart';
import 'package:clinalert/widgets/chart_widget.dart';
import 'package:clinalert/services/api_service.dart';
import 'package:clinalert/screens/add_edit_patient_screen.dart';
import 'package:clinalert/screens/smartwatch_connection_screen.dart';
import 'package:clinalert/screens/health_data_screen.dart';
import 'package:go_router/go_router.dart';
import 'package:flutter/material.dart';
import 'package:google_fonts/google_fonts.dart';
import 'dart:io';
import 'package:path_provider/path_provider.dart';
import 'package:file_selector/file_selector.dart';
import 'package:share_plus/share_plus.dart';



class PatientDetailScreen extends StatefulWidget {
  final Patient patient;
  final VoidCallback? onStatusChanged;

  const PatientDetailScreen({
    super.key, 
    required this.patient,
    this.onStatusChanged,
  });

  @override
  State<PatientDetailScreen> createState() => _PatientDetailScreenState();
}

class _PatientDetailScreenState extends State<PatientDetailScreen> {
  final ApiService _apiService = ApiService();
  late PatientStatus _currentStatus;
  bool _isUpdatingStatus = false;
  bool _isDeleting = false;
  
  // Smartwatch data
  List<SmartWatchDevice> _connectedDevices = [];
  List<HealthData> _recentHealthData = [];
  bool _isLoadingHealthData = true;

  @override
  void initState() {
    super.initState();
    _currentStatus = widget.patient.status;
    _loadSmartWatchData();
  }

  Future<void> _loadSmartWatchData() async {
    try {
      final devicesData = await _apiService.getPatientDevices(widget.patient.id);
      final healthData = await _apiService.getPatientHealthData(widget.patient.id);
      
      if (mounted) {
        setState(() {
          _connectedDevices = devicesData.map((d) => SmartWatchDevice.fromJson(d)).toList();
          _recentHealthData = healthData.map((d) => HealthData.fromJson(d)).take(10).toList();
          _isLoadingHealthData = false;
        });
      }
    } catch (e) {
      print('Error loading smartwatch data: $e');
      if (mounted) {
        setState(() => _isLoadingHealthData = false);
      }
    }
  }

  void _navigateToSmartWatchConnection() {
    Navigator.push(
      context,
      MaterialPageRoute(
        builder: (context) => SmartWatchConnectionScreen(
          patientId: widget.patient.id,
          patientName: widget.patient.fullName,
        ),
      ),
    ).then((_) => _loadSmartWatchData());
  }

  void _navigateToHealthData() {
    Navigator.push(
      context,
      MaterialPageRoute(
        builder: (context) => HealthDataScreen(
          patientId: widget.patient.id,
          patientName: widget.patient.fullName,
        ),
      ),
    );
  }

  Future<void> _deletePatient() async {
    final confirmed = await showDialog<bool>(
      context: context,
      builder: (context) => AlertDialog(
        title: const Text('Confirmer la suppression'),
        content: Text('Êtes-vous sûr de vouloir supprimer ${widget.patient.fullName} ?'),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(context, false),
            child: const Text('Annuler'),
          ),
          TextButton(
            onPressed: () => Navigator.pop(context, true),
            style: TextButton.styleFrom(foregroundColor: Colors.red),
            child: const Text('Supprimer'),
          ),
        ],
      ),
    );

    if (confirmed != true) return;

    setState(() => _isDeleting = true);

    try {
      await _apiService.deletePatient(widget.patient.id);
      
      if (mounted) {
        widget.onStatusChanged?.call(); // Refresh the list
        Navigator.pop(context); // Go back to dashboard
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(
            content: Text('Patient supprimé avec succès'),
            backgroundColor: Colors.green,
          ),
        );
      }
    } catch (e) {
      setState(() => _isDeleting = false);
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(
            content: Text('Erreur: $e'),
            backgroundColor: Colors.red,
          ),
        );
      }
    }
  }

  Future<void> _editPatient() async {
    final result = await Navigator.push(
      context,
      MaterialPageRoute(
        builder: (context) => AddEditPatientScreen(patient: widget.patient),
      ),
    );

    if (result == true && mounted) {
      widget.onStatusChanged?.call(); // Refresh the list
      Navigator.pop(context); // Go back to dashboard
    }
  }

  Future<void> _updatePatientStatus(PatientStatus newStatus) async {
    setState(() => _isUpdatingStatus = true);
    
    try {
      // Convert PatientStatus enum to string
      final statusString = newStatus.name;
      await _apiService.updatePatientStatus(widget.patient.id, statusString);
      
      setState(() {
        _currentStatus = newStatus;
        _isUpdatingStatus = false;
      });
      
      // Notify parent widget to refresh the list
      widget.onStatusChanged?.call();
      
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(
            content: Text('Statut du patient mis à jour avec succès'),
            backgroundColor: Colors.green,
          ),
        );
      }
    } catch (e) {
      setState(() => _isUpdatingStatus = false);
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(
            content: Text('Erreur: $e'),
            backgroundColor: Colors.red,
          ),
        );
      }
    }
  }

  void _showStatusDialog() {
    showDialog(
      context: context,
      builder: (context) => AlertDialog(
        title: const Text('Modifier le statut'),
        content: Column(
          mainAxisSize: MainAxisSize.min,
          children: PatientStatus.values.map((status) {
            return RadioListTile<PatientStatus>(
              title: Text(_getStatusLabel(status)),
              value: status,
              groupValue: _currentStatus,
              onChanged: (value) {
                if (value != null) {
                  Navigator.pop(context);
                  _updatePatientStatus(value);
                }
              },
            );
          }).toList(),
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(context),
            child: const Text('Annuler'),
          ),
        ],
      ),
    );
  }

  String _getStatusLabel(PatientStatus status) {
    switch (status) {
      case PatientStatus.active:
        return 'Actif';
      case PatientStatus.discharged:
        return 'Sorti';
      case PatientStatus.transferred:
        return 'Transféré';
    }
  }

  Color _getStatusColor(PatientStatus status) {
    switch (status) {
      case PatientStatus.active:
        return Colors.green;
      case PatientStatus.discharged:
        return Colors.grey;
      case PatientStatus.transferred:
        return Colors.orange;
    }
  }

  Future<void> _downloadReport() async {
    try {
      String? savePath;

      if (Platform.isWindows || Platform.isLinux || Platform.isMacOS) {
        final FileSaveLocation? result = await getSaveLocation(
          suggestedName: 'patient_report_${widget.patient.id}.pdf',
          acceptedTypeGroups: [
            const XTypeGroup(
              label: 'PDFs',
              extensions: <String>['pdf'],
            ),
          ],
        );
        
        if (result == null) {
          // User canceled
          return;
        }
        savePath = result.path;
      } else {
        // Mobile (Android / iOS)
        // Save to temporary or documents directory, then Share
        final directory = await getApplicationDocumentsDirectory();
        savePath = '${directory.path}/report_${widget.patient.id}.pdf';
        
        // We will download it first, and then share it below
      }
      
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('Génération du rapport en cours...')),
      );

      await _apiService.downloadPatientReport(widget.patient.id, savePath!);

      if (Platform.isAndroid || Platform.isIOS) {
        // Share the file on mobile
        final result = await Share.shareXFiles(
          [XFile(savePath)],
          text: 'Rapport médical - ${widget.patient.fullName}',
          subject: 'Rapport médical', // For email
        );
        
        if (result.status == ShareResultStatus.dismissed) {
           print('Share dismissed');
        }
      } else {
        // Desktop notification
        if (mounted) {
          ScaffoldMessenger.of(context).showSnackBar(
            SnackBar(
              content: Text('Rapport enregistré : $savePath'),
              backgroundColor: Colors.green,
              duration: const Duration(seconds: 5),
              action: SnackBarAction(
                label: 'Ouvrir',
                textColor: Colors.white,
                onPressed: () {
                  // Optional: Open the file or folder
                },
              ),
            ),
          );
        }
      }
    } catch (e) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(
            content: Text('Erreur lors du téléchargement : $e'),
            backgroundColor: Colors.red,
          ),
        );
        print('Download error: $e');
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    // Mock data for vital signs
    final vitalSigns = [
      VitalSign(id: '1', patientId: widget.patient.id, type: VitalType.heartRate, value: 78, unit: 'bpm', timestamp: DateTime.now()),
      VitalSign(id: '2', patientId: widget.patient.id, type: VitalType.bloodPressure, value: 120, unit: 'mmHg', timestamp: DateTime.now()),
      VitalSign(id: '3', patientId: widget.patient.id, type: VitalType.temperature, value: 36.8, unit: '°C', timestamp: DateTime.now()),
      VitalSign(id: '4', patientId: widget.patient.id, type: VitalType.oxygenSaturation, value: 98, unit: '%', timestamp: DateTime.now()),
    ];
    // Mock alerts for patient
    final alerts = [
      Alert(id: 'a1', patientId: widget.patient.patientId, title: 'Blood Pressure High', description: 'Systolic over threshold.', level: AlertLevel.high, createdAt: DateTime.now().subtract(const Duration(minutes: 20))),
      Alert(id: 'a2', patientId: widget.patient.patientId, title: 'Temperature Elevated', description: 'Slight fever detected.', level: AlertLevel.medium, createdAt: DateTime.now().subtract(const Duration(hours: 1))),
    ];

    return Scaffold(
      appBar: AppBar(
        title: const Text('Patient Profile'),
        actions: [
          IconButton(
            icon: const Icon(Icons.edit),
            onPressed: _isDeleting ? null : _editPatient,
            tooltip: 'Modifier',
          ),
          IconButton(
            icon: _isDeleting 
                ? const SizedBox(
                    width: 20,
                    height: 20,
                    child: CircularProgressIndicator(strokeWidth: 2),
                  )
                : const Icon(Icons.delete),
            onPressed: _isDeleting ? null : _deletePatient,
            tooltip: 'Supprimer',
          ),
          IconButton(
             icon: const Icon(Icons.picture_as_pdf),
             onPressed: _downloadReport,
             tooltip: 'Télécharger le rapport',
           ),
        ],
      ),
      body: CustomScrollView(
        slivers: [
          SliverToBoxAdapter(
            child: Padding(
              padding: const EdgeInsets.fromLTRB(20, 16, 20, 8),
              child: _HeaderSection(patient: widget.patient),
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
                      value: '${widget.patient.age} yrs',
                      icon: Icons.cake,
                      color: Colors.indigo,
                    ),
                  ),
                  const SizedBox(width: 12),
                  Expanded(
                    child: _InfoCard(
                      label: 'Blood Type',
                      value: widget.patient.bloodType,
                      icon: Icons.bloodtype,
                      color: Colors.red,
                    ),
                  ),
                  const SizedBox(width: 12),
                  Expanded(
                    child: GestureDetector(
                      onTap: _isUpdatingStatus ? null : _showStatusDialog,
                      child: _InfoCard(
                        label: 'Status',
                        value: _isUpdatingStatus ? '...' : _getStatusLabel(_currentStatus),
                        icon: Icons.health_and_safety,
                        color: _getStatusColor(_currentStatus),
                      ),
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
          // SmartWatch Section
          SliverToBoxAdapter(
            child: Padding(
              padding: const EdgeInsets.fromLTRB(20, 16, 20, 8),
              child: _buildSmartWatchSection(),
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

  String _getInitials(Patient patient) {
    final first = patient.firstName.isNotEmpty ? patient.firstName[0].toUpperCase() : '';
    final last = patient.lastName.isNotEmpty ? patient.lastName[0].toUpperCase() : '';
    return first + last;
  }

  Widget _buildSmartWatchSection() {
    return ModernCard(
      padding: const EdgeInsets.all(20),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            children: [
              Container(
                padding: const EdgeInsets.all(10),
                decoration: BoxDecoration(
                  color: const Color(0xFF4A90E2).withOpacity(0.1),
                  borderRadius: BorderRadius.circular(12),
                ),
                child: const Icon(Icons.watch, color: Color(0xFF4A90E2), size: 24),
              ),
              const SizedBox(width: 12),
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      'SmartWatch Data',
                      style: GoogleFonts.inter(
                        fontSize: 16,
                        fontWeight: FontWeight.bold,
                      ),
                    ),
                    Text(
                      _connectedDevices.isEmpty 
                          ? 'No device connected' 
                          : '${_connectedDevices.length} device(s) connected',
                      style: TextStyle(color: Colors.grey[600], fontSize: 12),
                    ),
                  ],
                ),
              ),
              if (_connectedDevices.isNotEmpty)
                IconButton(
                  icon: const Icon(Icons.analytics_outlined),
                  onPressed: _navigateToHealthData,
                  tooltip: 'View Health Data',
                ),
            ],
          ),
          const SizedBox(height: 16),
          
          if (_isLoadingHealthData)
            const Center(child: CircularProgressIndicator())
          else if (_recentHealthData.isEmpty)
            _buildNoDataState()
          else
            _buildRecentHealthDataPreview(),
          
          const SizedBox(height: 16),
          
          Row(
            children: [
              Expanded(
                child: OutlinedButton.icon(
                  onPressed: _navigateToSmartWatchConnection,
                  icon: const Icon(Icons.bluetooth),
                  label: Text(_connectedDevices.isEmpty ? 'Connect Device' : 'Manage Devices'),
                  style: OutlinedButton.styleFrom(
                    padding: const EdgeInsets.symmetric(vertical: 12),
                  ),
                ),
              ),
              if (_recentHealthData.isNotEmpty) ...[
                const SizedBox(width: 12),
                Expanded(
                  child: ElevatedButton.icon(
                    onPressed: _navigateToHealthData,
                    icon: const Icon(Icons.bar_chart),
                    label: const Text('View All Data'),
                    style: ElevatedButton.styleFrom(
                      padding: const EdgeInsets.symmetric(vertical: 12),
                    ),
                  ),
                ),
              ],
            ],
          ),
        ],
      ),
    );
  }

  Widget _buildNoDataState() {
    return Container(
      padding: const EdgeInsets.symmetric(vertical: 24),
      child: Column(
        children: [
          Icon(Icons.watch_off, size: 48, color: Colors.grey[300]),
          const SizedBox(height: 12),
          Text(
            'No health data yet',
            style: GoogleFonts.inter(
              fontSize: 14,
              fontWeight: FontWeight.w500,
              color: Colors.grey[600],
            ),
          ),
          const SizedBox(height: 4),
          Text(
            'Connect a smartwatch to start tracking',
            style: TextStyle(color: Colors.grey[400], fontSize: 12),
          ),
        ],
      ),
    );
  }

  Widget _buildRecentHealthDataPreview() {
    final latestData = _recentHealthData.first;
    
    return Row(
      mainAxisAlignment: MainAxisAlignment.spaceAround,
      children: [
        _buildDataPreviewItem(
          icon: Icons.favorite,
          color: Colors.red,
          value: latestData.heartRateDisplay,
          label: 'Heart Rate',
        ),
        _buildDataPreviewItem(
          icon: Icons.air,
          color: Colors.blue,
          value: latestData.spO2Display,
          label: 'SpO2',
        ),
        _buildDataPreviewItem(
          icon: Icons.directions_walk,
          color: Colors.green,
          value: latestData.stepsDisplay,
          label: 'Steps',
        ),
      ],
    );
  }

  Widget _buildDataPreviewItem({
    required IconData icon,
    required Color color,
    required String value,
    required String label,
  }) {
    return Column(
      children: [
        Container(
          padding: const EdgeInsets.all(10),
          decoration: BoxDecoration(
            color: color.withOpacity(0.1),
            borderRadius: BorderRadius.circular(10),
          ),
          child: Icon(icon, color: color, size: 20),
        ),
        const SizedBox(height: 8),
        Text(
          value,
          style: GoogleFonts.inter(
            fontSize: 16,
            fontWeight: FontWeight.bold,
          ),
        ),
        Text(
          label,
          style: TextStyle(color: Colors.grey[500], fontSize: 10),
        ),
      ],
    );
  }
}


class _HeaderSection extends StatelessWidget {
  final Patient patient;
  const _HeaderSection({required this.patient});

  String _getInitials() {
    final first = patient.firstName.isNotEmpty ? patient.firstName[0].toUpperCase() : '';
    final last = patient.lastName.isNotEmpty ? patient.lastName[0].toUpperCase() : '';
    return first + last;
  }

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
                  _getInitials(),
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