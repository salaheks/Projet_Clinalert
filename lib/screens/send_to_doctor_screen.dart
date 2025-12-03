import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../services/storage_service.dart';
import '../services/api_service.dart';
import '../models/measurement.dart';

class SendToDoctorScreen extends StatefulWidget {
  const SendToDoctorScreen({super.key});

  @override
  State<SendToDoctorScreen> createState() => _SendToDoctorScreenState();
}

class _SendToDoctorScreenState extends State<SendToDoctorScreen> {
  List<Measurement> _measurements = [];
  final Set<String> _selectedIds = {};
  bool _isSending = false;

  @override
  void initState() {
    super.initState();
    _loadMeasurements();
  }

  void _loadMeasurements() {
    final storage = context.read<StorageService>();
    setState(() {
      _measurements = storage.getMeasurements().where((m) => !m.isSynced).toList();
      // Sort by timestamp desc
      _measurements.sort((a, b) => b.timestamp.compareTo(a.timestamp));
    });
  }

  Future<void> _sendSelected() async {
    if (_selectedIds.isEmpty) return;

    setState(() {
      _isSending = true;
    });

    final apiService = context.read<ApiService>();
    final storageService = context.read<StorageService>();
    
    final toSend = _measurements.where((m) => _selectedIds.contains(m.id)).toList();

    try {
      await apiService.sendMeasurements(toSend);
      
      // Mark as synced
      for (var m in toSend) {
        m.isSynced = true;
        await storageService.saveMeasurement(m);
      }

      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(content: Text('Sent ${toSend.length} measurements')),
        );
        _loadMeasurements();
        _selectedIds.clear();
      }
    } catch (e) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(content: Text('Error sending data: $e')),
        );
      }
    } finally {
      if (mounted) {
        setState(() {
          _isSending = false;
        });
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Send to Doctor'),
        actions: [
          if (_selectedIds.isNotEmpty)
            IconButton(
              icon: const Icon(Icons.send),
              onPressed: _isSending ? null : _sendSelected,
            ),
        ],
      ),
      body: _measurements.isEmpty
          ? const Center(child: Text('No unsent measurements'))
          : ListView.builder(
              itemCount: _measurements.length,
              itemBuilder: (context, index) {
                final m = _measurements[index];
                final isSelected = _selectedIds.contains(m.id);
                return CheckboxListTile(
                  value: isSelected,
                  title: Text('${m.type}: ${m.value}'),
                  subtitle: Text(m.timestamp.toString()),
                  onChanged: (val) {
                    setState(() {
                      if (val == true) {
                        _selectedIds.add(m.id);
                      } else {
                        _selectedIds.remove(m.id);
                      }
                    });
                  },
                );
              },
            ),
    );
  }
}
