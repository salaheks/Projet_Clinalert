import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';
import 'package:provider/provider.dart';
import '../themes/app_theme.dart';
import '../models/user_model.dart';
import '../models/message_model.dart';
import '../services/message_service.dart';
import '../providers/auth_provider.dart';
import 'chat_screen.dart';

// Liste des conversations selon le rôle de l'utilisateur
class ChatListScreen extends StatefulWidget {
  final UserRole currentRole;
  const ChatListScreen({super.key, required this.currentRole});

  @override
  State<ChatListScreen> createState() => _ChatListScreenState();
}

class _ChatListScreenState extends State<ChatListScreen> {
  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) {
      final auth = context.read<AuthProvider>();
      final msgService = context.read<MessageService>();
      if (auth.currentUser != null) {
        msgService.setCurrentUser(auth.currentUser!.id);
        if (msgService.conversations.isEmpty) {
          msgService.initializeMockConversations(auth.currentUser!.id, widget.currentRole);
        }
      }
    });
  }

  @override
  Widget build(BuildContext context) {
    final msgService = context.watch<MessageService>();
    final conversations = msgService.getConversationsForRole(widget.currentRole);

    return Scaffold(
      appBar: AppBar(
        title: const Text('Messages'),
        backgroundColor: AppThemes.primaryBlue,
        foregroundColor: Colors.white,
        leading: IconButton(
          icon: const Icon(Icons.arrow_back),
          onPressed: () {
            if (Navigator.of(context).canPop()) {
              Navigator.of(context).pop();
            } else {
              context.go('/login');
            }
          },
        ),
      ),
      body: conversations.isEmpty
          ? const Center(child: Text('Aucune conversation'))
          : ListView.builder(
              padding: const EdgeInsets.all(12),
              itemCount: conversations.length,
              itemBuilder: (context, index) {
                final conv = conversations[index];
                return Card(
                  margin: const EdgeInsets.symmetric(vertical: 6),
                  child: ListTile(
                    leading: Stack(
                      children: [
                        CircleAvatar(
                          backgroundColor: AppThemes.primaryBlue.withOpacity(0.1),
                          child: Icon(_getRoleIcon(conv.participantRole), color: AppThemes.primaryBlue),
                        ),
                        if (conv.unreadCount > 0)
                          Positioned(
                            right: 0,
                            top: 0,
                            child: Container(
                              padding: const EdgeInsets.all(4),
                              decoration: const BoxDecoration(color: Colors.red, shape: BoxShape.circle),
                              constraints: const BoxConstraints(minWidth: 16, minHeight: 16),
                              child: Text(
                                conv.unreadCount > 9 ? '9+' : '${conv.unreadCount}',
                                style: const TextStyle(color: Colors.white, fontSize: 10, fontWeight: FontWeight.bold),
                                textAlign: TextAlign.center,
                              ),
                            ),
                          ),
                      ],
                    ),
                    title: Text(conv.participantName, style: const TextStyle(fontWeight: FontWeight.w600)),
                    subtitle: Text(conv.lastMessage ?? 'Aucun message', maxLines: 1, overflow: TextOverflow.ellipsis),
                    trailing: Column(
                      mainAxisAlignment: MainAxisAlignment.center,
                      crossAxisAlignment: CrossAxisAlignment.end,
                      children: [
                        Text(_formatTime(conv.lastMessageTime), style: const TextStyle(fontSize: 12, color: Colors.grey)),
                        if (conv.unreadCount > 0) const SizedBox(height: 4),
                      ],
                    ),
                    onTap: () {
                      Navigator.push(
                        context,
                        MaterialPageRoute(
                          builder: (_) => ChatScreen(
                            title: conv.participantName,
                            conversationId: conv.id,
                            contactId: conv.participantId,
                            contactName: conv.participantName,
                            contactRole: conv.participantRole,
                          ),
                        ),
                      ).then((_) {
                        // Marquer comme lu après retour
                        final auth = context.read<AuthProvider>();
                        if (auth.currentUser != null) {
                          msgService.markAsRead(conv.id, auth.currentUser!.id);
                        }
                      });
                    },
                  ),
                );
              },
            ),
    );
  }

  IconData _getRoleIcon(UserRole role) {
    switch (role) {
      case UserRole.doctor:
        return Icons.local_hospital;
      case UserRole.nurse:
        return Icons.healing;
      case UserRole.patient:
        return Icons.person;
    }
  }

  String _formatTime(DateTime? time) {
    if (time == null) return '';
    final now = DateTime.now();
    final diff = now.difference(time);
    if (diff.inMinutes < 1) return 'Maintenant';
    if (diff.inMinutes < 60) return '${diff.inMinutes}m';
    if (diff.inHours < 24) return '${diff.inHours}h';
    if (diff.inDays < 7) return '${diff.inDays}j';
    return '${time.day}/${time.month}';
  }
}

