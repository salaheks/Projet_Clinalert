import 'package:clinalert/services/api_service.dart';
import 'package:flutter/material.dart';
import 'package:google_fonts/google_fonts.dart';

class UsersManagementScreen extends StatefulWidget {
  const UsersManagementScreen({super.key});

  @override
  State<UsersManagementScreen> createState() => _UsersManagementScreenState();
}

class _UsersManagementScreenState extends State<UsersManagementScreen> {
  final ApiService _apiService = ApiService();
  List<Map<String, dynamic>> _users = [];
  bool _isLoading = true;
  String? _errorMessage;

  @override
  void initState() {
    super.initState();
    _loadUsers();
  }

  Future<void> _loadUsers() async {
    setState(() {
      _isLoading = true;
      _errorMessage = null;
    });

    try {
      final users = await _apiService.getAllUsers();
      setState(() {
        _users = users;
        _isLoading = false;
      });
    } catch (e) {
      setState(() {
        _errorMessage = 'Erreur de chargement: $e';
        _isLoading = false;
      });
    }
  }

  Future<void> _showAddUserDialog() async {
    final emailController = TextEditingController();
    final passwordController = TextEditingController();
    String selectedRole = 'DOCTOR';

    await showDialog(
      context: context,
      builder: (context) => StatefulBuilder(
        builder: (context, setDialogState) => AlertDialog(
          title: const Text('Ajouter un utilisateur'),
          content: SingleChildScrollView(
            child: Column(
              mainAxisSize: MainAxisSize.min,
              children: [
                TextField(
                  controller: emailController,
                  decoration: const InputDecoration(
                    labelText: 'Email',
                    prefixIcon: Icon(Icons.email),
                  ),
                  keyboardType: TextInputType.emailAddress,
                ),
                const SizedBox(height: 16),
                TextField(
                  controller: passwordController,
                  decoration: const InputDecoration(
                    labelText: 'Mot de passe',
                    prefixIcon: Icon(Icons.lock),
                  ),
                  obscureText: true,
                ),
                const SizedBox(height: 16),
                DropdownButtonFormField<String>(
                  value: selectedRole,
                  decoration: const InputDecoration(
                    labelText: 'Rôle',
                    prefixIcon: Icon(Icons.badge),
                  ),
                  items: const [
                    DropdownMenuItem(value: 'ADMIN', child: Text('Admin')),
                    DropdownMenuItem(value: 'DOCTOR', child: Text('Docteur')),
                    DropdownMenuItem(value: 'PATIENT', child: Text('Patient')),
                  ],
                  onChanged: (value) {
                    setDialogState(() => selectedRole = value!);
                  },
                ),
              ],
            ),
          ),
          actions: [
            TextButton(
              onPressed: () => Navigator.pop(context),
              child: const Text('Annuler'),
            ),
            ElevatedButton(
              onPressed: () async {
                if (emailController.text.isEmpty || passwordController.text.isEmpty) {
                  ScaffoldMessenger.of(context).showSnackBar(
                    const SnackBar(content: Text('Veuillez remplir tous les champs')),
                  );
                  return;
                }

                Navigator.pop(context);
                try {
                  await _apiService.createUser({
                    'email': emailController.text,
                    'password': passwordController.text,
                    'role': selectedRole,
                  });
                  _loadUsers();
                  if (mounted) {
                    ScaffoldMessenger.of(context).showSnackBar(
                      const SnackBar(
                        content: Text('Utilisateur créé avec succès'),
                        backgroundColor: Colors.green,
                      ),
                    );
                  }
                } catch (e) {
                  if (mounted) {
                    ScaffoldMessenger.of(context).showSnackBar(
                      SnackBar(content: Text('Erreur: $e'), backgroundColor: Colors.red),
                    );
                  }
                }
              },
              child: const Text('Créer'),
            ),
          ],
        ),
      ),
    );
  }

  Future<void> _showEditRoleDialog(Map<String, dynamic> user) async {
    String selectedRole = user['role'] ?? 'DOCTOR';
    bool isEnabled = user['enabled'] ?? true;

    await showDialog(
      context: context,
      builder: (context) => StatefulBuilder(
        builder: (context, setDialogState) => AlertDialog(
          title: Text('Modifier ${user['email']}'),
          content: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              DropdownButtonFormField<String>(
                value: selectedRole,
                decoration: const InputDecoration(
                  labelText: 'Rôle',
                  prefixIcon: Icon(Icons.badge),
                ),
                items: const [
                  DropdownMenuItem(value: 'ADMIN', child: Text('Admin')),
                  DropdownMenuItem(value: 'DOCTOR', child: Text('Docteur')),
                  DropdownMenuItem(value: 'PATIENT', child: Text('Patient')),
                ],
                onChanged: (value) {
                  setDialogState(() => selectedRole = value!);
                },
              ),
              const SizedBox(height: 16),
              SwitchListTile(
                title: const Text('Compte actif'),
                value: isEnabled,
                onChanged: (value) {
                  setDialogState(() => isEnabled = value);
                },
              ),
            ],
          ),
          actions: [
            TextButton(
              onPressed: () => Navigator.pop(context),
              child: const Text('Annuler'),
            ),
            ElevatedButton(
              onPressed: () async {
                Navigator.pop(context);
                try {
                  await _apiService.updateUser(user['id'], {
                    'role': selectedRole,
                    'enabled': isEnabled,
                  });
                  _loadUsers();
                  if (mounted) {
                    ScaffoldMessenger.of(context).showSnackBar(
                      const SnackBar(
                        content: Text('Utilisateur modifié avec succès'),
                        backgroundColor: Colors.green,
                      ),
                    );
                  }
                } catch (e) {
                  if (mounted) {
                    ScaffoldMessenger.of(context).showSnackBar(
                      SnackBar(content: Text('Erreur: $e'), backgroundColor: Colors.red),
                    );
                  }
                }
              },
              child: const Text('Enregistrer'),
            ),
          ],
        ),
      ),
    );
  }

  Future<void> _deleteUser(Map<String, dynamic> user) async {
    final confirmed = await showDialog<bool>(
      context: context,
      builder: (context) => AlertDialog(
        title: const Text('Supprimer l\'utilisateur'),
        content: Text('Voulez-vous vraiment supprimer ${user['email']} ?'),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(context, false),
            child: const Text('Annuler'),
          ),
          ElevatedButton(
            onPressed: () => Navigator.pop(context, true),
            style: ElevatedButton.styleFrom(backgroundColor: Colors.red),
            child: const Text('Supprimer'),
          ),
        ],
      ),
    );

    if (confirmed == true) {
      try {
        await _apiService.deleteUser(user['id']);
        _loadUsers();
        if (mounted) {
          ScaffoldMessenger.of(context).showSnackBar(
            const SnackBar(
              content: Text('Utilisateur supprimé'),
              backgroundColor: Colors.green,
            ),
          );
        }
      } catch (e) {
        if (mounted) {
          ScaffoldMessenger.of(context).showSnackBar(
            SnackBar(content: Text('Erreur: $e'), backgroundColor: Colors.red),
          );
        }
      }
    }
  }

  Future<void> _showEditEmailDialog(Map<String, dynamic> user) async {
    final emailController = TextEditingController(text: user['email'] ?? '');

    await showDialog(
      context: context,
      builder: (context) => AlertDialog(
        title: const Text('Modifier l\'email'),
        content: TextField(
          controller: emailController,
          decoration: const InputDecoration(
            labelText: 'Nouvel email',
            prefixIcon: Icon(Icons.email),
          ),
          keyboardType: TextInputType.emailAddress,
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(context),
            child: const Text('Annuler'),
          ),
          ElevatedButton(
            onPressed: () async {
              if (emailController.text.isEmpty) {
                ScaffoldMessenger.of(context).showSnackBar(
                  const SnackBar(content: Text('L\'email ne peut pas être vide')),
                );
                return;
              }

              Navigator.pop(context);
              try {
                await _apiService.updateUserEmail(user['id'], emailController.text);
                _loadUsers();
                if (mounted) {
                  ScaffoldMessenger.of(context).showSnackBar(
                    const SnackBar(
                      content: Text('Email modifié avec succès'),
                      backgroundColor: Colors.green,
                    ),
                  );
                }
              } catch (e) {
                if (mounted) {
                  ScaffoldMessenger.of(context).showSnackBar(
                    SnackBar(content: Text('Erreur: $e'), backgroundColor: Colors.red),
                  );
                }
              }
            },
            child: const Text('Enregistrer'),
          ),
        ],
      ),
    );
  }

  Future<void> _showEditPasswordDialog(Map<String, dynamic> user) async {
    final passwordController = TextEditingController();
    final confirmPasswordController = TextEditingController();

    await showDialog(
      context: context,
      builder: (context) => AlertDialog(
        title: Text('Réinitialiser mot de passe\n${user['email']}'),
        content: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            TextField(
              controller: passwordController,
              decoration: const InputDecoration(
                labelText: 'Nouveau mot de passe',
                prefixIcon: Icon(Icons.lock),
              ),
              obscureText: true,
            ),
            const SizedBox(height: 16),
            TextField(
              controller: confirmPasswordController,
              decoration: const InputDecoration(
                labelText: 'Confirmer mot de passe',
                prefixIcon: Icon(Icons.lock_outline),
              ),
              obscureText: true,
            ),
          ],
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(context),
            child: const Text('Annuler'),
          ),
          ElevatedButton(
            onPressed: () async {
              if (passwordController.text.isEmpty) {
                ScaffoldMessenger.of(context).showSnackBar(
                  const SnackBar(content: Text('Le mot de passe ne peut pas être vide')),
                );
                return;
              }
              if (passwordController.text != confirmPasswordController.text) {
                ScaffoldMessenger.of(context).showSnackBar(
                  const SnackBar(content: Text('Les mots de passe ne correspondent pas')),
                );
                return;
              }

              Navigator.pop(context);
              try {
                await _apiService.updateUserPassword(user['id'], passwordController.text);
                if (mounted) {
                  ScaffoldMessenger.of(context).showSnackBar(
                    const SnackBar(
                      content: Text('Mot de passe modifié avec succès'),
                      backgroundColor: Colors.green,
                    ),
                  );
                }
              } catch (e) {
                if (mounted) {
                  ScaffoldMessenger.of(context).showSnackBar(
                    SnackBar(content: Text('Erreur: $e'), backgroundColor: Colors.red),
                  );
                }
              }
            },
            child: const Text('Réinitialiser'),
          ),
        ],
      ),
    );
  }

  Color _getRoleColor(String? role) {
    switch (role?.toUpperCase()) {
      case 'ADMIN':
        return Colors.purple;
      case 'DOCTOR':
        return Colors.blue;
      case 'PATIENT':
        return Colors.orange;
      default:
        return Colors.grey;
    }
  }

  IconData _getRoleIcon(String? role) {
    switch (role?.toUpperCase()) {
      case 'ADMIN':
        return Icons.admin_panel_settings;
      case 'DOCTOR':
        return Icons.medical_services;
      case 'PATIENT':
        return Icons.person;
      default:
        return Icons.help;
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: const Color(0xFFF8FAFC),
      appBar: AppBar(
        title: Text(
          'Gestion des utilisateurs',
          style: GoogleFonts.inter(fontWeight: FontWeight.bold),
        ),
        centerTitle: true,
        backgroundColor: Colors.transparent,
        elevation: 0,
        foregroundColor: Colors.black87,
        actions: [
          IconButton(
            icon: const Icon(Icons.refresh),
            onPressed: _loadUsers,
          ),
        ],
      ),
      body: _isLoading
          ? const Center(child: CircularProgressIndicator())
          : _errorMessage != null
              ? Center(
                  child: Column(
                    mainAxisAlignment: MainAxisAlignment.center,
                    children: [
                      const Icon(Icons.error_outline, size: 64, color: Colors.red),
                      const SizedBox(height: 16),
                      Text(_errorMessage!),
                      const SizedBox(height: 16),
                      ElevatedButton(
                        onPressed: _loadUsers,
                        child: const Text('Réessayer'),
                      ),
                    ],
                  ),
                )
              : RefreshIndicator(
                  onRefresh: _loadUsers,
                  child: ListView.builder(
                    padding: const EdgeInsets.all(16),
                    itemCount: _users.length,
                    itemBuilder: (context, index) {
                      final user = _users[index];
                      final role = user['role'] as String?;
                      final isEnabled = user['enabled'] as bool? ?? true;

                      return Card(
                        margin: const EdgeInsets.only(bottom: 12),
                        shape: RoundedRectangleBorder(
                          borderRadius: BorderRadius.circular(12),
                        ),
                        child: ListTile(
                          leading: CircleAvatar(
                            backgroundColor: _getRoleColor(role).withOpacity(0.1),
                            child: Icon(
                              _getRoleIcon(role),
                              color: _getRoleColor(role),
                            ),
                          ),
                          title: Text(
                            user['email'] ?? 'N/A',
                            style: TextStyle(
                              fontWeight: FontWeight.w600,
                              color: isEnabled ? Colors.black87 : Colors.grey,
                              decoration: isEnabled ? null : TextDecoration.lineThrough,
                            ),
                          ),
                          subtitle: Row(
                            children: [
                              Container(
                                padding: const EdgeInsets.symmetric(
                                  horizontal: 8,
                                  vertical: 2,
                                ),
                                decoration: BoxDecoration(
                                  color: _getRoleColor(role).withOpacity(0.1),
                                  borderRadius: BorderRadius.circular(12),
                                ),
                                child: Text(
                                  role ?? 'N/A',
                                  style: TextStyle(
                                    fontSize: 12,
                                    color: _getRoleColor(role),
                                    fontWeight: FontWeight.w600,
                                  ),
                                ),
                              ),
                              const SizedBox(width: 8),
                              if (!isEnabled)
                                Container(
                                  padding: const EdgeInsets.symmetric(
                                    horizontal: 8,
                                    vertical: 2,
                                  ),
                                  decoration: BoxDecoration(
                                    color: Colors.red.withOpacity(0.1),
                                    borderRadius: BorderRadius.circular(12),
                                  ),
                                  child: const Text(
                                    'Désactivé',
                                    style: TextStyle(
                                      fontSize: 12,
                                      color: Colors.red,
                                      fontWeight: FontWeight.w600,
                                    ),
                                  ),
                                ),
                            ],
                          ),
                          trailing: PopupMenuButton<String>(
                            onSelected: (value) {
                              if (value == 'edit') {
                                _showEditRoleDialog(user);
                              } else if (value == 'email') {
                                _showEditEmailDialog(user);
                              } else if (value == 'password') {
                                _showEditPasswordDialog(user);
                              } else if (value == 'delete') {
                                _deleteUser(user);
                              }
                            },
                            itemBuilder: (context) => [
                              const PopupMenuItem(
                                value: 'edit',
                                child: Row(
                                  children: [
                                    Icon(Icons.badge, size: 20),
                                    SizedBox(width: 8),
                                    Text('Modifier rôle'),
                                  ],
                                ),
                              ),
                              const PopupMenuItem(
                                value: 'email',
                                child: Row(
                                  children: [
                                    Icon(Icons.email, size: 20, color: Colors.blue),
                                    SizedBox(width: 8),
                                    Text('Modifier email'),
                                  ],
                                ),
                              ),
                              const PopupMenuItem(
                                value: 'password',
                                child: Row(
                                  children: [
                                    Icon(Icons.lock, size: 20, color: Colors.orange),
                                    SizedBox(width: 8),
                                    Text('Réinitialiser mot de passe'),
                                  ],
                                ),
                              ),
                              const PopupMenuItem(
                                value: 'delete',
                                child: Row(
                                  children: [
                                    Icon(Icons.delete, size: 20, color: Colors.red),
                                    SizedBox(width: 8),
                                    Text('Supprimer', style: TextStyle(color: Colors.red)),
                                  ],
                                ),
                              ),
                            ],
                          ),
                        ),
                      );
                    },
                  ),
                ),
      floatingActionButton: FloatingActionButton.extended(
        onPressed: _showAddUserDialog,
        icon: const Icon(Icons.person_add),
        label: const Text('Ajouter'),
        backgroundColor: const Color(0xFF0066FF),
      ),
    );
  }
}
