import 'package:clinalert/screens/doctor_dashboard_screen.dart';
import 'package:clinalert/screens/nurse_dashboard_screen.dart';
import 'package:clinalert/screens/patient_dashboard_screen.dart';
import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:go_router/go_router.dart';
import 'services/auth_service.dart';
import 'providers/auth_provider.dart';
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

void main() {
  runApp(const MyApp());
}

class MyApp extends StatelessWidget {
  const MyApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MultiProvider(
      providers: [
        ChangeNotifierProvider<AuthService>(create: (_) => AuthService()),
        ChangeNotifierProvider(
          create: (context) => AuthProvider(context.read<AuthService>()),
        ),
        ChangeNotifierProvider(create: (_) => ThemeProvider()),
        ChangeNotifierProvider(create: (_) => MessageService()),
      ],
      child: Consumer<ThemeProvider>(
        builder: (context, themeProv, _) => MaterialApp.router(
          title: 'ClinAlert',
          theme: AppThemes.lightTheme,
          darkTheme: AppThemes.darkTheme,
          themeMode: themeProv.themeMode,
          routerConfig: _router,
          debugShowCheckedModeBanner: false,
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
        requiredRoles: const [UserRole.doctor],
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
