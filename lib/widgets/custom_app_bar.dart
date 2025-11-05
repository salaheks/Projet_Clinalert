import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../models/user_model.dart';
import '../providers/auth_provider.dart';
import '../themes/app_theme.dart';
import '../providers/theme_provider.dart';
import 'global_search_delegate.dart';

class CustomAppBar extends StatelessWidget implements PreferredSizeWidget {
  final String title;
  final User? user;
  final VoidCallback? onLogout;
  final List<Widget>? actions;

  const CustomAppBar({
    super.key,
    required this.title,
    this.user,
    this.onLogout,
    this.actions,
  });

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final authProvider = context.watch<AuthProvider>();

    return AppBar(
      iconTheme: const IconThemeData(color: AppThemes.primaryBlue),
      title: Text(
        title,
        style: theme.textTheme.headlineSmall?.copyWith(
          color: AppThemes.darkGrey,
          fontWeight: FontWeight.bold,
        ),
      ),
      backgroundColor: Theme.of(context).scaffoldBackgroundColor,
      elevation: 0,
      actions: [
        IconButton(
          tooltip: 'Search',
          icon: const Icon(Icons.search, color: AppThemes.primaryBlue),
          onPressed: () => showSearch(context: context, delegate: GlobalSearchDelegate()),
        ),
        Consumer<ThemeProvider>(
          builder: (_, themeProv, __) => IconButton(
            tooltip: themeProv.isDarkMode ? 'Light Mode' : 'Dark Mode',
            icon: Icon(themeProv.isDarkMode ? Icons.light_mode : Icons.dark_mode, color: AppThemes.primaryBlue),
            onPressed: () => themeProv.toggleTheme(),
          ),
        ),
        if (actions != null) ...actions!,
        if (user != null)
          PopupMenuButton<String>(
            onSelected: (value) {
              if (value == 'logout') {
                (onLogout ?? () {}).call();
              }
            },
            itemBuilder: (BuildContext context) => <PopupMenuEntry<String>>[
              PopupMenuItem<String>(
                value: 'profile',
                child: ListTile(
                  leading: CircleAvatar(
                    backgroundColor: AppThemes.primaryBlue,
                    child: Text(
                      authProvider.getUserInitials(),
                      style: const TextStyle(color: Colors.white),
                    ),
                  ),
                  title: Text(authProvider.getUserDisplayName()),
                  subtitle: Text(user!.email),
                ),
              ),
              const PopupMenuDivider(),
              const PopupMenuItem<String>(
                value: 'settings',
                child: ListTile(
                  leading: Icon(Icons.settings),
                  title: Text('Settings'),
                ),
              ),
              const PopupMenuItem<String>(
                value: 'logout',
                child: ListTile(
                  leading: Icon(Icons.logout),
                  title: Text('Logout'),
                ),
              ),
            ],
            child: CircleAvatar(
              backgroundColor: AppThemes.primaryBlue.withOpacity(0.1),
              child: Text(
                authProvider.getUserInitials(),
                style: const TextStyle(color: AppThemes.primaryBlue, fontWeight: FontWeight.bold),
              ),
            ),
          ),
        const SizedBox(width: 16),
      ],
    );
  }

  @override
  Size get preferredSize => const Size.fromHeight(kToolbarHeight);
}