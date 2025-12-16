import 'package:clinalert/screens/ble_scan_screen.dart';
import 'package:clinalert/services/ble_service.dart';
import 'package:clinalert/services/storage_service.dart';
import 'package:clinalert/services/api_service.dart';
import 'package:clinalert/screens/send_to_doctor_screen.dart';
import 'package:clinalert/screens/measurement_screen.dart';
import 'package:clinalert/screens/doctor_dashboard_screen.dart';
import 'package:clinalert/screens/nurse_dashboard_screen.dart';
import 'package:clinalert/screens/patient_dashboard_screen.dart';
import 'package:clinalert/screens/smartwatch_connection_screen.dart';
import 'package:clinalert/screens/health_data_screen.dart';
import 'package:clinalert/screens/settings_screen.dart';
import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:go_router/go_router.dart';
import 'package:flutter_reactive_ble/flutter_reactive_ble.dart';
import 'services/auth_service.dart';
import 'providers/auth_provider.dart';
import 'providers/locale_provider.dart';
import 'themes/app_theme.dart';
import 'providers/theme_provider.dart';
import 'screens/welcome_screen.dart';
import 'screens/login_screen.dart';
import 'screens/signup_screen.dart';
import 'screens/forgot_password_screen.dart';
import 'models/user_model.dart';
import 'screens/create_profile_screen.dart';
import 'screens/chat_screen.dart';
import 'screens/chat_list_screen.dart';
import 'services/message_service.dart';
import 'package:flutter_localizations/flutter_localizations.dart';
import 'package:clinalert/l10n/app_localizations.dart';

void main() async {
  WidgetsFlutterBinding.ensureInitialized();
  final storageService = StorageService();
  await storageService.init();
  
  // Initialize locale provider
  final localeProvider = LocaleProvider();
  await localeProvider.init();
  
  runApp(MyApp(storageService: storageService, localeProvider: localeProvider));
}

class MyApp extends StatelessWidget {
  final StorageService storageService;
  final LocaleProvider localeProvider;
  
  const MyApp({super.key, required this.storageService, required this.localeProvider});

  @override
  Widget build(BuildContext context) {
    return MultiProvider(
      providers: [
        Provider<StorageService>.value(value: storageService),
        Provider<BleService>(create: (_) => BleService()),
        Provider<ApiService>(create: (_) => ApiService()),
        ChangeNotifierProvider<AuthService>(create: (_) => AuthService()),
        ChangeNotifierProvider(
          create: (context) => AuthProvider(context.read<AuthService>()),
        ),
        ChangeNotifierProvider(create: (_) => ThemeProvider()),
        ChangeNotifierProvider(create: (_) => MessageService()),
        ChangeNotifierProvider<LocaleProvider>.value(value: localeProvider),
      ],
      child: Consumer2<ThemeProvider, LocaleProvider>(
        builder: (context, themeProv, localeProv, _) => MaterialApp.router(
          title: 'ClinAlert',
          theme: AppThemes.lightTheme,
          darkTheme: AppThemes.darkTheme,
          themeMode: themeProv.themeMode,
          routerConfig: _router,
          debugShowCheckedModeBanner: false,
          locale: localeProv.locale,
          localizationsDelegates: const [
            GlobalMaterialLocalizations.delegate,
            GlobalWidgetsLocalizations.delegate,
            GlobalCupertinoLocalizations.delegate,
            AppLocalizations.delegate,
          ],
          supportedLocales: const [
            Locale('en'), // English
            Locale('fr'), // French
            Locale('ar'), // Arabic
          ],
        ),
      ),
    );
  }
}

// Authentication Guard
class AuthGuard extends StatelessWidget {
  final Widget child;
  final List<UserRole>? requiredRoles;
  
  const AuthGuard({
    super.key,
    required this.child,
    this.requiredRoles,
  });

  @override
  Widget build(BuildContext context) {
    final authProvider = context.watch<AuthProvider>();
    
    // Check authentication
    if (!authProvider.isAuthenticated) {
      WidgetsBinding.instance.addPostFrameCallback((_) {
        context.go('/login');
      });
      return const Scaffold(
        body: Center(child: CircularProgressIndicator()),
      );
    }
    
    // Check role-based access
    if (requiredRoles != null && !authProvider.hasAnyRole(requiredRoles!)) {
      WidgetsBinding.instance.addPostFrameCallback((_) {
        context.go('/unauthorized');
      });
      return const Scaffold(
        body: Center(
          child: Text('Unauthorized Access'),
        ),
      );
    }
    
    return child;
  }
}

// GoRouter configuration
final GoRouter _router = GoRouter(
  initialLocation: '/',
  redirect: (context, state) {
    final authProvider = Provider.of<AuthProvider>(context, listen: false);
    
    // Handle authentication redirects
    if (!authProvider.isAuthenticated && 
        state.matchedLocation != '/login' &&
        state.matchedLocation != '/signup' &&
        state.matchedLocation != '/forgot-password' &&
        state.matchedLocation != '/create-profile') {
      return '/login';
    }
    
    // Handle role-based redirects
    if (authProvider.isAuthenticated) {
      if (state.matchedLocation == '/login' || 
          state.matchedLocation == '/signup' ||
          state.matchedLocation == '/') {
        return authProvider.getDashboardRoute();
      }
    }
    
    return null;
  },
  routes: [
    // Public Routes
    GoRoute(
      path: '/',
      builder: (context, state) => const WelcomeScreen(),
    ),
    GoRoute(
      path: '/login',
      builder: (context, state) => const LoginScreen(),
    ),
    GoRoute(
      path: '/signup',
      builder: (context, state) => const SignUpScreen(),
    ),
    GoRoute(
      path: '/create-profile',
      builder: (context, state) => const CreateProfileScreen(),
    ),
    GoRoute(
      path: '/chat',
      builder: (context, state) => const ChatScreen(),
    ),
    GoRoute(
      path: '/chat-list',
      builder: (context, state) => AuthGuard(
        child: Builder(
          builder: (ctx) {
            final auth = Provider.of<AuthProvider>(ctx, listen: false);
            return ChatListScreen(currentRole: auth.currentUser?.role ?? UserRole.patient);
          },
        ),
      ),
    ),
    GoRoute(
      path: '/forgot-password',
      builder: (context, state) => const ForgotPasswordScreen(),
    ),
    
    // Protected Routes - Doctor Dashboard
    GoRoute(
      path: '/doctor-dashboard',
      builder: (context, state) => AuthGuard(
        requiredRoles: const [UserRole.admin, UserRole.doctor],
        child: const DoctorDashboardScreen(),
      ),
    ),
    
    // Protected Routes - Nurse Dashboard
    GoRoute(
      path: '/nurse-dashboard',
      builder: (context, state) => AuthGuard(
        requiredRoles: const [UserRole.nurse],
        child: const NurseDashboardScreen(),
      ),
    ),
    
    // Protected Routes - Patient Dashboard
    GoRoute(
      path: '/patient-dashboard',
      builder: (context, state) => AuthGuard(
        requiredRoles: const [UserRole.patient],
        child: const PatientDashboardScreen(),
      ),
    ),
    
    // BLE Routes
    GoRoute(
      path: '/ble-scan',
      builder: (context, state) => const BleScanScreen(),
    ),
    GoRoute(
      path: '/send-to-doctor',
      builder: (context, state) => const SendToDoctorScreen(),
    ),
    GoRoute(
      path: '/measurement',
      builder: (context, state) {
        final device = state.extra as DiscoveredDevice;
        final authProvider = Provider.of<AuthProvider>(context, listen: false);
        return MeasurementScreen(device: device, patientId: authProvider.currentUser?.id ?? 'unknown');
      },
    ),
    
    // Smartwatch Connection Route
    GoRoute(
      path: '/smartwatch',
      builder: (context, state) {
        final authProvider = Provider.of<AuthProvider>(context, listen: false);
        final name = authProvider.currentUser?.firstName;
        return AuthGuard(
          child: SmartWatchConnectionScreen(
            patientId: authProvider.currentUser?.id ?? '',
            patientName: name?.isNotEmpty == true ? name : null,
          ),
        );
      },
    ),
    
    // Health Data Route
    GoRoute(
      path: '/health-data',
      builder: (context, state) {
        final authProvider = Provider.of<AuthProvider>(context, listen: false);
        final name = authProvider.currentUser?.firstName;
        return AuthGuard(
          child: HealthDataScreen(
            patientId: authProvider.currentUser?.id ?? '',
            patientName: name?.isNotEmpty == true ? name : null,
          ),
        );
      },
    ),
    
    // Settings Route
    GoRoute(
      path: '/settings',
      builder: (context, state) => AuthGuard(
        child: const SettingsScreen(),
      ),
    ),

    // Unauthorized Page
    GoRoute(
      path: '/unauthorized',
      builder: (context, state) => Scaffold(
        appBar: AppBar(title: const Text('Unauthorized')),
        body: Center(
          child: Column(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              const Icon(
                Icons.block,
                size: 64,
                color: Colors.red,
              ),
              const SizedBox(height: 16),
              const Text(
                'You are not authorized to access this page.',
                style: TextStyle(fontSize: 16),
              ),
              const SizedBox(height: 16),
              ElevatedButton(
                onPressed: () => context.go('/login'),
                child: const Text('Go to Login'),
              ),
            ],
          ),
        ),
      ),
    ),

    // Shared simple routes (can be replaced by guarded wrappers if needed)
    GoRoute(
      path: '/alerts',
      builder: (context, state) => const Placeholder(),
    ),
  ],
);
