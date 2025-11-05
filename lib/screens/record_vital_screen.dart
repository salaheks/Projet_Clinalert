import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../models/vital_sign_model.dart';
import '../themes/app_theme.dart';
import '../providers/auth_provider.dart';

class RecordVitalScreen extends StatefulWidget {
  const RecordVitalScreen({super.key});

  @override
  State<RecordVitalScreen> createState() => _RecordVitalScreenState();
}

class _RecordVitalScreenState extends State<RecordVitalScreen> {
  final _formKey = GlobalKey<FormState>();
  VitalType _type = VitalType.heartRate;
  final _valueController = TextEditingController();
  final _unitController = TextEditingController();
  final _notesController = TextEditingController();

  @override
  void dispose() {
    _valueController.dispose();
    _unitController.dispose();
    _notesController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);

    return Scaffold(
      appBar: AppBar(
        title: const Text('Record Vital'),
        backgroundColor: AppThemes.primaryBlue,
        foregroundColor: Colors.white,
      ),
      body: SingleChildScrollView(
        padding: const EdgeInsets.all(16),
        child: Form(
          key: _formKey,
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.stretch,
            children: [
              DropdownButtonFormField<VitalType>(
                value: _type,
                decoration: const InputDecoration(labelText: 'Type'),
                items: VitalType.values.map((v) {
                  return DropdownMenuItem(value: v, child: Text(v.name));
                }).toList(),
                onChanged: (v) => setState(() => _type = v ?? _type),
              ),
              const SizedBox(height: 12),
              TextFormField(
                controller: _valueController,
                decoration: const InputDecoration(labelText: 'Value'),
                keyboardType: TextInputType.number,
                validator: (v) {
                  if (v == null || v.trim().isEmpty) return 'Enter a value';
                  final d = double.tryParse(v);
                  if (d == null) return 'Must be a number';
                  return null;
                },
              ),
              const SizedBox(height: 12),
              TextFormField(
                controller: _unitController,
                decoration: const InputDecoration(labelText: 'Unit (e.g., bpm, mmHg, °C, %)'),
                validator: (v) => v == null || v.isEmpty ? 'Enter a unit' : null,
              ),
              const SizedBox(height: 12),
              TextFormField(
                controller: _notesController,
                decoration: const InputDecoration(labelText: 'Notes (optional)'),
                maxLines: 3,
              ),
              const SizedBox(height: 20),
              ElevatedButton(
                onPressed: _submit,
                style: ElevatedButton.styleFrom(
                  backgroundColor: AppThemes.primaryGreen,
                  foregroundColor: Colors.white,
                  padding: const EdgeInsets.symmetric(vertical: 14),
                ),
                child: const Text('Save Vital'),
              ),
            ],
          ),
        ),
      ),
    );
  }

  void _submit() {
    if (_formKey.currentState!.validate()) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('Vital recorded (mock).')),
      );
      Navigator.pop(context);
    }
  }
}


