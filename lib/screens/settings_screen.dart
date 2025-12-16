import 'package:clinalert/models/user_model.dart';
import 'package:clinalert/providers/auth_provider.dart';
import 'package:clinalert/providers/locale_provider.dart';
import 'package:clinalert/screens/users_management_screen.dart';
import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:google_fonts/google_fonts.dart';
import '../widgets/modern_card.dart';

class SettingsScreen extends StatefulWidget {
  const SettingsScreen({super.key});

  @override
  State<SettingsScreen> createState() => _SettingsScreenState();
}

class _SettingsScreenState extends State<SettingsScreen> {
  final _formKey = GlobalKey<FormState>();
  late TextEditingController _firstNameController;
  late TextEditingController _lastNameController;
  late TextEditingController _emailController;
  late TextEditingController _phoneController;
  UserRole? _selectedRole;
  bool _isEditing = false;
  bool _isSaving = false;

  @override
  void initState() {
    super.initState();
    final user = context.read<AuthProvider>().currentUser;
    _firstNameController = TextEditingController(text: user?.firstName ?? '');
    _lastNameController = TextEditingController(text: user?.lastName ?? '');
    _emailController = TextEditingController(text: user?.email ?? '');
    _phoneController = TextEditingController(text: user?.phone ?? '');
    _selectedRole = user?.role ?? UserRole.doctor;
  }

  @override
  void dispose() {
    _firstNameController.dispose();
    _lastNameController.dispose();
    _emailController.dispose();
    _phoneController.dispose();
    super.dispose();
  }

  void _toggleEdit() {
    setState(() {
      _isEditing = !_isEditing;
      if (!_isEditing) {
        // Reset to original values
        final user = context.read<AuthProvider>().currentUser;
        _firstNameController.text = user?.firstName ?? '';
        _lastNameController.text = user?.lastName ?? '';
        _emailController.text = user?.email ?? '';
        _phoneController.text = user?.phone ?? '';
        _selectedRole = user?.role ?? UserRole.doctor;
      }
    });
  }

  Future<void> _saveChanges() async {
    if (!_formKey.currentState!.validate()) return;

    setState(() => _isSaving = true);

    try {
      final authProvider = context.read<AuthProvider>();
      
      // Call the real API to update user profile
      await authProvider.updateProfile(
        firstName: _firstNameController.text.trim(),
        lastName: _lastNameController.text.trim(),
        phone: _phoneController.text.trim(),
        email: _emailController.text.trim(),
      );

      setState(() {
        _isEditing = false;
        _isSaving = false;
      });

      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(
            content: const Text('Profil mis à jour avec succès'),
            backgroundColor: Colors.green.shade600,
            behavior: SnackBarBehavior.floating,
            shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(10)),
          ),
        );
      }
    } catch (e) {
      setState(() => _isSaving = false);
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(
            content: Text('Erreur: $e'),
            backgroundColor: Colors.red.shade600,
            behavior: SnackBarBehavior.floating,
          ),
        );
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    final authProvider = context.watch<AuthProvider>();
    final user = authProvider.currentUser;

    return Scaffold(
      backgroundColor: const Color(0xFFF8FAFC),
      appBar: AppBar(
        title: Text(
          'Paramètres',
          style: GoogleFonts.inter(fontWeight: FontWeight.bold),
        ),
        centerTitle: true,
        backgroundColor: Colors.transparent,
        elevation: 0,
        foregroundColor: Colors.black87,
        leading: IconButton(
          icon: const Icon(Icons.arrow_back),
          onPressed: () => Navigator.of(context).pop(),
          tooltip: 'Retour',
        ),
        actions: [
          if (!_isEditing)
            IconButton(
              icon: const Icon(Icons.edit_outlined),
              onPressed: _toggleEdit,
              tooltip: 'Modifier',
            )
          else
            IconButton(
              icon: const Icon(Icons.close),
              onPressed: _toggleEdit,
              tooltip: 'Annuler',
            ),
        ],
      ),
      body: SingleChildScrollView(
        padding: const EdgeInsets.all(20),
        child: Form(
          key: _formKey,
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              // Profile Header
              Center(
                child: Column(
                  children: [
                    Container(
                      width: 100,
                      height: 100,
                      decoration: BoxDecoration(
                        gradient: const LinearGradient(
                          colors: [Color(0xFF0066FF), Color(0xFF00C4B4)],
                          begin: Alignment.topLeft,
                          end: Alignment.bottomRight,
                        ),
                        shape: BoxShape.circle,
                        boxShadow: [
                          BoxShadow(
                            color: const Color(0xFF0066FF).withOpacity(0.3),
                            blurRadius: 20,
                            offset: const Offset(0, 10),
                          ),
                        ],
                      ),
                      child: Center(
                        child: Text(
                          authProvider.getUserInitials(),
                          style: GoogleFonts.inter(
                            color: Colors.white,
                            fontSize: 32,
                            fontWeight: FontWeight.bold,
                          ),
                        ),
                      ),
                    ),
                    const SizedBox(height: 16),
                    Text(
                      user?.fullName ?? 'Utilisateur',
                      style: GoogleFonts.inter(
                        fontSize: 24,
                        fontWeight: FontWeight.bold,
                        color: Colors.black87,
                      ),
                    ),
                    const SizedBox(height: 4),
                    Container(
                      padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 6),
                      decoration: BoxDecoration(
                        color: _getRoleColor(user?.role).withOpacity(0.1),
                        borderRadius: BorderRadius.circular(20),
                        border: Border.all(
                          color: _getRoleColor(user?.role).withOpacity(0.3),
                        ),
                      ),
                      child: Text(
                        user?.roleDisplayName ?? 'N/A',
                        style: GoogleFonts.inter(
                          color: _getRoleColor(user?.role),
                          fontWeight: FontWeight.w600,
                        ),
                      ),
                    ),
                  ],
                ),
              ),
              
              const SizedBox(height: 32),
              
              // Personal Information Section
              Text(
                'Informations Personnelles',
                style: GoogleFonts.inter(
                  fontSize: 18,
                  fontWeight: FontWeight.bold,
                  color: Colors.black87,
                ),
              ),
              const SizedBox(height: 16),
              
              ModernCard(
                padding: const EdgeInsets.all(20),
                child: Column(
                  children: [
                    _buildTextField(
                      controller: _firstNameController,
                      label: 'Prénom',
                      icon: Icons.person_outline,
                      enabled: _isEditing,
                      validator: (v) => v?.isEmpty == true ? 'Requis' : null,
                    ),
                    const SizedBox(height: 16),
                    _buildTextField(
                      controller: _lastNameController,
                      label: 'Nom',
                      icon: Icons.person_outline,
                      enabled: _isEditing,
                      validator: (v) => v?.isEmpty == true ? 'Requis' : null,
                    ),
                    const SizedBox(height: 16),
                    _buildTextField(
                      controller: _emailController,
                      label: 'Email',
                      icon: Icons.email_outlined,
                      enabled: _isEditing,
                      keyboardType: TextInputType.emailAddress,
                      validator: (v) {
                        if (v?.isEmpty == true) return 'Requis';
                        if (!v!.contains('@')) return 'Email invalide';
                        return null;
                      },
                    ),
                    const SizedBox(height: 16),
                    _buildTextField(
                      controller: _phoneController,
                      label: 'Téléphone',
                      icon: Icons.phone_outlined,
                      enabled: _isEditing,
                      keyboardType: TextInputType.phone,
                    ),
                  ],
                ),
              ),
              
              const SizedBox(height: 24),
              
              // Language Selection Section
              Text(
                'Langue / Language',
                style: GoogleFonts.inter(
                  fontSize: 18,
                  fontWeight: FontWeight.bold,
                  color: Colors.black87,
                ),
              ),
              const SizedBox(height: 16),
              
              ModernCard(
                padding: const EdgeInsets.all(16),
                child: Column(
                  children: [
                    _buildLanguageOption('ar', 'العربية 🇲🇦', 'Arabic'),
                    const Divider(height: 1),
                    _buildLanguageOption('fr', 'Français 🇫🇷', 'French'),
                    const Divider(height: 1),
                    _buildLanguageOption('en', 'English 🇬🇧', 'English'),
                  ],
                ),
              ),
              
              const SizedBox(height: 24),
              
              // Admin Section - User Management (only for admins)
              if (authProvider.isAdmin) ...[
                Text(
                  'Administration',
                  style: GoogleFonts.inter(
                    fontSize: 18,
                    fontWeight: FontWeight.bold,
                    color: Colors.black87,
                  ),
                ),
                const SizedBox(height: 16),
                
                ModernCard(
                  padding: const EdgeInsets.all(16),
                  child: ListTile(
                    leading: Container(
                      width: 44,
                      height: 44,
                      decoration: BoxDecoration(
                        color: Colors.purple.withOpacity(0.1),
                        shape: BoxShape.circle,
                      ),
                      child: const Icon(
                        Icons.people,
                        color: Colors.purple,
                      ),
                    ),
                    title: Text(
                      'Gestion des utilisateurs',
                      style: GoogleFonts.inter(
                        fontWeight: FontWeight.w600,
                      ),
                    ),
                    subtitle: Text(
                      'Ajouter, modifier, supprimer des comptes',
                      style: GoogleFonts.inter(
                        fontSize: 12,
                        color: Colors.grey.shade600,
                      ),
                    ),
                    trailing: const Icon(Icons.chevron_right),
                    onTap: () {
                      Navigator.push(
                        context,
                        MaterialPageRoute(
                          builder: (context) => const UsersManagementScreen(),
                        ),
                      );
                    },
                  ),
                ),
                
                const SizedBox(height: 24),
              ],
              
              // Role Selection Section
              Text(
                'Rôle Utilisateur',
                style: GoogleFonts.inter(
                  fontSize: 18,
                  fontWeight: FontWeight.bold,
                  color: Colors.black87,
                ),
              ),
              const SizedBox(height: 16),
              
              ModernCard(
                padding: const EdgeInsets.all(20),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      'Sélectionnez votre rôle dans l\'application',
                      style: GoogleFonts.inter(
                        color: Colors.grey.shade600,
                        fontSize: 14,
                      ),
                    ),
                    const SizedBox(height: 16),
                    ...UserRole.values.map((role) => _buildRoleOption(role)),
                  ],
                ),
              ),
              
              const SizedBox(height: 32),
              
              // Save Button
              if (_isEditing)
                SizedBox(
                  width: double.infinity,
                  child: ElevatedButton(
                    onPressed: _isSaving ? null : _saveChanges,
                    style: ElevatedButton.styleFrom(
                      backgroundColor: const Color(0xFF0066FF),
                      foregroundColor: Colors.white,
                      padding: const EdgeInsets.symmetric(vertical: 16),
                      shape: RoundedRectangleBorder(
                        borderRadius: BorderRadius.circular(12),
                      ),
                      elevation: 0,
                    ),
                    child: _isSaving
                        ? const SizedBox(
                            width: 24,
                            height: 24,
                            child: CircularProgressIndicator(
                              color: Colors.white,
                              strokeWidth: 2,
                            ),
                          )
                        : Text(
                            'Enregistrer les modifications',
                            style: GoogleFonts.inter(
                              fontSize: 16,
                              fontWeight: FontWeight.w600,
                            ),
                          ),
                  ),
                ),
              
              const SizedBox(height: 24),
              
              // Logout Button
              SizedBox(
                width: double.infinity,
                child: OutlinedButton.icon(
                  onPressed: () {
                    showDialog(
                      context: context,
                      builder: (ctx) => AlertDialog(
                        title: const Text('Déconnexion'),
                        content: const Text('Voulez-vous vraiment vous déconnecter ?'),
                        actions: [
                          TextButton(
                            onPressed: () => Navigator.pop(ctx),
                            child: const Text('Annuler'),
                          ),
                          ElevatedButton(
                            onPressed: () {
                              Navigator.pop(ctx);
                              context.read<AuthProvider>().logout();
                              Navigator.of(context).popUntil((route) => route.isFirst);
                            },
                            style: ElevatedButton.styleFrom(
                              backgroundColor: Colors.red,
                              foregroundColor: Colors.white,
                            ),
                            child: const Text('Déconnexion'),
                          ),
                        ],
                      ),
                    );
                  },
                  icon: const Icon(Icons.logout, color: Colors.red),
                  label: Text(
                    'Se déconnecter',
                    style: GoogleFonts.inter(
                      color: Colors.red,
                      fontWeight: FontWeight.w600,
                    ),
                  ),
                  style: OutlinedButton.styleFrom(
                    padding: const EdgeInsets.symmetric(vertical: 16),
                    side: const BorderSide(color: Colors.red),
                    shape: RoundedRectangleBorder(
                      borderRadius: BorderRadius.circular(12),
                    ),
                  ),
                ),
              ),
              
              const SizedBox(height: 40),
            ],
          ),
        ),
      ),
    );
  }

  Widget _buildTextField({
    required TextEditingController controller,
    required String label,
    required IconData icon,
    bool enabled = true,
    TextInputType? keyboardType,
    String? Function(String?)? validator,
  }) {
    return TextFormField(
      controller: controller,
      enabled: enabled,
      keyboardType: keyboardType,
      validator: validator,
      decoration: InputDecoration(
        labelText: label,
        prefixIcon: Icon(icon, color: enabled ? const Color(0xFF0066FF) : Colors.grey),
        filled: true,
        fillColor: enabled ? Colors.white : Colors.grey.shade100,
        border: OutlineInputBorder(
          borderRadius: BorderRadius.circular(12),
          borderSide: BorderSide(color: Colors.grey.shade300),
        ),
        enabledBorder: OutlineInputBorder(
          borderRadius: BorderRadius.circular(12),
          borderSide: BorderSide(color: Colors.grey.shade300),
        ),
        focusedBorder: OutlineInputBorder(
          borderRadius: BorderRadius.circular(12),
          borderSide: const BorderSide(color: Color(0xFF0066FF), width: 2),
        ),
        disabledBorder: OutlineInputBorder(
          borderRadius: BorderRadius.circular(12),
          borderSide: BorderSide(color: Colors.grey.shade200),
        ),
      ),
    );
  }

  Widget _buildRoleOption(UserRole role) {
    final isSelected = _selectedRole == role;
    final color = _getRoleColor(role);
    
    return Padding(
      padding: const EdgeInsets.only(bottom: 12),
      child: InkWell(
        onTap: _isEditing
            ? () => setState(() => _selectedRole = role)
            : null,
        borderRadius: BorderRadius.circular(12),
        child: AnimatedContainer(
          duration: const Duration(milliseconds: 200),
          padding: const EdgeInsets.all(16),
          decoration: BoxDecoration(
            color: isSelected ? color.withOpacity(0.1) : Colors.grey.shade50,
            borderRadius: BorderRadius.circular(12),
            border: Border.all(
              color: isSelected ? color : Colors.grey.shade200,
              width: isSelected ? 2 : 1,
            ),
          ),
          child: Row(
            children: [
              Container(
                width: 44,
                height: 44,
                decoration: BoxDecoration(
                  color: isSelected ? color.withOpacity(0.2) : Colors.grey.shade100,
                  shape: BoxShape.circle,
                ),
                child: Icon(
                  _getRoleIcon(role),
                  color: isSelected ? color : Colors.grey.shade600,
                ),
              ),
              const SizedBox(width: 16),
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      _getRoleDisplayName(role),
                      style: GoogleFonts.inter(
                        fontWeight: FontWeight.w600,
                        color: isSelected ? color : Colors.black87,
                      ),
                    ),
                    const SizedBox(height: 2),
                    Text(
                      _getRoleDescription(role),
                      style: GoogleFonts.inter(
                        fontSize: 12,
                        color: Colors.grey.shade600,
                      ),
                    ),
                  ],
                ),
              ),
              if (isSelected)
                Container(
                  width: 24,
                  height: 24,
                  decoration: BoxDecoration(
                    color: color,
                    shape: BoxShape.circle,
                  ),
                  child: const Icon(
                    Icons.check,
                    color: Colors.white,
                    size: 16,
                  ),
                )
              else if (_isEditing)
                Container(
                  width: 24,
                  height: 24,
                  decoration: BoxDecoration(
                    border: Border.all(color: Colors.grey.shade300),
                    shape: BoxShape.circle,
                  ),
                ),
            ],
          ),
        ),
      ),
    );
  }

  Color _getRoleColor(UserRole? role) {
    switch (role) {
      case UserRole.admin:
        return Colors.purple;
      case UserRole.doctor:
        return const Color(0xFF0066FF);
      case UserRole.nurse:
        return Colors.teal;
      case UserRole.patient:
        return Colors.orange;
      default:
        return Colors.grey;
    }
  }

  IconData _getRoleIcon(UserRole role) {
    switch (role) {
      case UserRole.admin:
        return Icons.admin_panel_settings;
      case UserRole.doctor:
        return Icons.medical_services;
      case UserRole.nurse:
        return Icons.health_and_safety;
      case UserRole.patient:
        return Icons.person;
    }
  }

  String _getRoleDisplayName(UserRole role) {
    switch (role) {
      case UserRole.admin:
        return 'Administrateur';
      case UserRole.doctor:
        return 'Médecin';
      case UserRole.nurse:
        return 'Infirmier(e)';
      case UserRole.patient:
        return 'Patient';
    }
  }

  String _getRoleDescription(UserRole role) {
    switch (role) {
      case UserRole.admin:
        return 'Accès complet à toutes les fonctionnalités';
      case UserRole.doctor:
        return 'Gérer les patients et consulter les dossiers';
      case UserRole.nurse:
        return 'Enregistrer les mesures et suivre les patients';
      case UserRole.patient:
        return 'Consulter son dossier médical';
    }
  }

  Widget _buildLanguageOption(String langCode, String displayName, String englishName) {
    final localeProvider = context.watch<LocaleProvider>();
    final isSelected = localeProvider.locale.languageCode == langCode;
    
    return ListTile(
      contentPadding: const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
      leading: Container(
        width: 40,
        height: 40,
        decoration: BoxDecoration(
          color: isSelected ? const Color(0xFF0066FF).withOpacity(0.1) : Colors.grey.shade100,
          shape: BoxShape.circle,
        ),
        child: Icon(
          Icons.language,
          color: isSelected ? const Color(0xFF0066FF) : Colors.grey.shade600,
        ),
      ),
      title: Text(
        displayName,
        style: GoogleFonts.inter(
          fontWeight: isSelected ? FontWeight.bold : FontWeight.normal,
          color: isSelected ? const Color(0xFF0066FF) : Colors.black87,
        ),
      ),
      trailing: isSelected
          ? Container(
              width: 24,
              height: 24,
              decoration: const BoxDecoration(
                color: Color(0xFF0066FF),
                shape: BoxShape.circle,
              ),
              child: const Icon(Icons.check, color: Colors.white, size: 16),
            )
          : null,
      onTap: () {
        localeProvider.setLanguageCode(langCode);
      },
    );
  }
}
