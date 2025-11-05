import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../services/message_service.dart';

class ChatIconButton extends StatelessWidget {
  final VoidCallback onPressed;
  const ChatIconButton({super.key, required this.onPressed});

  @override
  Widget build(BuildContext context) {
    final msgService = context.watch<MessageService>();
    final unreadCount = msgService.getUnreadCount();

    return Stack(
      children: [
        IconButton(
          icon: const Icon(Icons.chat_bubble_outline),
          onPressed: onPressed,
        ),
        if (unreadCount > 0)
          Positioned(
            right: 8,
            top: 8,
            child: Container(
              padding: const EdgeInsets.all(4),
              decoration: const BoxDecoration(color: Colors.red, shape: BoxShape.circle),
              constraints: const BoxConstraints(minWidth: 16, minHeight: 16),
              child: Text(
                unreadCount > 9 ? '9+' : '$unreadCount',
                style: const TextStyle(color: Colors.white, fontSize: 10, fontWeight: FontWeight.bold),
                textAlign: TextAlign.center,
              ),
            ),
          ),
      ],
    );
  }
}

