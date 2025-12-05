// ignore: unused_import
import 'package:intl/intl.dart' as intl;
import 'app_localizations.dart';

// ignore_for_file: type=lint

/// The translations for English (`en`).
class AppLocalizationsEn extends AppLocalizations {
  AppLocalizationsEn([String locale = 'en']) : super(locale);

  @override
  String get appTitle => 'ClinAlert';

  @override
  String get dashboard => 'Dashboard';

  @override
  String get patients => 'Patients';

  @override
  String get alerts => 'Alerts';

  @override
  String get chat => 'Chat';

  @override
  String get settings => 'Settings';

  @override
  String get exportReport => 'Export Report';

  @override
  String get anomalyDetected => 'Anomaly Detected';

  @override
  String get connectDevice => 'Connect Device';

  @override
  String get scanning => 'Scanning...';

  @override
  String get connected => 'Connected';

  @override
  String get disconnected => 'Disconnected';
}
