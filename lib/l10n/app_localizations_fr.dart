// ignore: unused_import
import 'package:intl/intl.dart' as intl;
import 'app_localizations.dart';

// ignore_for_file: type=lint

/// The translations for French (`fr`).
class AppLocalizationsFr extends AppLocalizations {
  AppLocalizationsFr([String locale = 'fr']) : super(locale);

  @override
  String get appTitle => 'ClinAlert';

  @override
  String get dashboard => 'Tableau de bord';

  @override
  String get patients => 'Patients';

  @override
  String get alerts => 'Alertes';

  @override
  String get chat => 'Messagerie';

  @override
  String get settings => 'Paramètres';

  @override
  String get exportReport => 'Exporter le rapport';

  @override
  String get anomalyDetected => 'Anomalie détectée';

  @override
  String get connectDevice => 'Connecter un appareil';

  @override
  String get scanning => 'Recherche en cours...';

  @override
  String get connected => 'Connecté';

  @override
  String get disconnected => 'Déconnecté';
}
