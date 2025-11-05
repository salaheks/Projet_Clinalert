import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../models/patient_model.dart';
import '../models/alert_model.dart';
import '../models/vital_sign_model.dart';
import '../providers/auth_provider.dart';

// Mocked global search delegate that can search patients, alerts and measurements.
class GlobalSearchDelegate extends SearchDelegate<String> {
  GlobalSearchDelegate();

  @override
  String get searchFieldLabel => 'Search patients, alerts, measurements';

  @override
  List<Widget>? buildActions(BuildContext context) {
    return [
      if (query.isNotEmpty)
        IconButton(
          icon: const Icon(Icons.clear),
          onPressed: () => query = '',
        ),
    ];
  }

  @override
  Widget? buildLeading(BuildContext context) {
    return IconButton(
      icon: const Icon(Icons.arrow_back),
      onPressed: () => close(context, ''),
    );
  }

  @override
  Widget buildResults(BuildContext context) {
    return _buildContent(context);
  }

  @override
  Widget buildSuggestions(BuildContext context) {
    return _buildContent(context);
  }

  Widget _buildContent(BuildContext context) {
    final theme = Theme.of(context);

    // Mock data: in a real app fetch via providers/services
    final patients = <Patient>[]; // Could read from a PatientsProvider
    final alerts = <Alert>[]; // Could read from an AlertsProvider
    final measurements = <String>['Heart Rate', 'Blood Pressure', 'Temperature', 'SpO2'];

    final q = query.toLowerCase();
    final matchedPatients = patients.where((p) => p.fullName.toLowerCase().contains(q) || p.patientId.toLowerCase().contains(q)).toList();
    final matchedAlerts = alerts.where((a) => a.title.toLowerCase().contains(q) || a.description.toLowerCase().contains(q)).toList();
    final matchedMeasures = measurements.where((m) => m.toLowerCase().contains(q)).toList();

    if (q.isEmpty) {
      return Center(
        child: Text('Type to search patients, alerts, or measurements', style: theme.textTheme.bodyMedium),
      );
    }

    return ListView(
      padding: const EdgeInsets.all(16),
      children: [
        if (matchedPatients.isNotEmpty) ...[
          Text('Patients', style: theme.textTheme.titleSmall),
          const SizedBox(height: 8),
          ...matchedPatients.map((p) => ListTile(
                leading: const Icon(Icons.person),
                title: Text(p.fullName),
                subtitle: Text('ID: ${p.patientId}'),
                onTap: () => close(context, p.id),
              )),
          const SizedBox(height: 16),
        ],
        if (matchedAlerts.isNotEmpty) ...[
          Text('Alerts', style: theme.textTheme.titleSmall),
          const SizedBox(height: 8),
          ...matchedAlerts.map((a) => ListTile(
                leading: const Icon(Icons.warning_amber_rounded, color: Colors.orange),
                title: Text(a.title),
                subtitle: Text(a.description),
                onTap: () => close(context, a.id),
              )),
          const SizedBox(height: 16),
        ],
        if (matchedMeasures.isNotEmpty) ...[
          Text('Measurements', style: theme.textTheme.titleSmall),
          const SizedBox(height: 8),
          ...matchedMeasures.map((m) => ListTile(
                leading: const Icon(Icons.monitor_heart),
                title: Text(m),
                onTap: () => close(context, m),
              )),
        ],
        if (matchedPatients.isEmpty && matchedAlerts.isEmpty && matchedMeasures.isEmpty)
          Center(
            child: Padding(
              padding: const EdgeInsets.only(top: 32.0),
              child: Text('No results', style: theme.textTheme.bodyMedium),
            ),
          ),
      ],
    );
  }
}


