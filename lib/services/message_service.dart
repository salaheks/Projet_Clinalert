import 'dart:convert';
import 'package:flutter/foundation.dart';
import 'package:shared_preferences/shared_preferences.dart';
import '../models/message_model.dart';
import '../models/user_model.dart';

class MessageService extends ChangeNotifier {
  final List<Conversation> _conversations = [];
  final Map<String, List<Message>> _messages = {};
  String? _currentUserId;

  List<Conversation> get conversations => _conversations;
  Map<String, List<Message>> get messages => _messages;

  void setCurrentUser(String userId) {
    _currentUserId = userId;
    _loadConversations();
    _loadMessages();
  }

  // Récupérer les conversations pour un rôle
  List<Conversation> getConversationsForRole(UserRole role) {
    if (_currentUserId == null) return [];
    return _conversations.where((c) => _isRelevantForRole(c, role)).toList()
      ..sort((a, b) => (b.lastMessageTime ?? b.createdAt).compareTo(a.lastMessageTime ?? a.createdAt));
  }

  bool _isRelevantForRole(Conversation conv, UserRole role) {
    // Mock: retourne les conversations pertinentes selon le rôle
    return true; // Dans un vrai système, filtrer selon les relations
  }

  // Récupérer les messages d'une conversation
  List<Message> getMessages(String conversationId) {
    return _messages[conversationId] ?? []
      ..sort((a, b) => a.timestamp.compareTo(b.timestamp));
  }

  // Envoyer un message
  Future<void> sendMessage({
    required String conversationId,
    required String senderId,
    required String senderName,
    required UserRole senderRole,
    required String text,
  }) async {
    final message = Message(
      id: DateTime.now().millisecondsSinceEpoch.toString(),
      conversationId: conversationId,
      senderId: senderId,
      senderName: senderName,
      senderRole: senderRole,
      text: text,
      timestamp: DateTime.now(),
      isRead: false,
    );

    if (!_messages.containsKey(conversationId)) {
      _messages[conversationId] = [];
    }
    _messages[conversationId]!.add(message);

    // Mettre à jour la conversation
    final convIndex = _conversations.indexWhere((c) => c.id == conversationId);
    if (convIndex >= 0) {
      _conversations[convIndex] = _conversations[convIndex].copyWith(
        lastMessage: text,
        lastMessageTime: DateTime.now(),
      );
    }

    await _saveMessages();
    await _saveConversations();
    notifyListeners();
  }

  // Marquer les messages comme lus
  Future<void> markAsRead(String conversationId, String currentUserId) async {
    if (!_messages.containsKey(conversationId)) return;

    bool updated = false;
    _messages[conversationId] = _messages[conversationId]!.map((msg) {
      if (!msg.isRead && msg.senderId != currentUserId) {
        updated = true;
        return msg.copyWith(isRead: true);
      }
      return msg;
    }).toList();

    if (updated) {
      final convIndex = _conversations.indexWhere((c) => c.id == conversationId);
      if (convIndex >= 0) {
        _conversations[convIndex] = _conversations[convIndex].copyWith(unreadCount: 0);
      }
      await _saveMessages();
      await _saveConversations();
      notifyListeners();
    }
  }

  // Compter les messages non lus
  int getUnreadCount() {
    return _conversations.fold(0, (sum, conv) => sum + conv.unreadCount);
  }

  // Initialiser les conversations mock pour un utilisateur
  void initializeMockConversations(String userId, UserRole role) {
    _conversations.clear();
    _messages.clear();

    switch (role) {
      case UserRole.doctor:
        _conversations.addAll([
          Conversation(
            id: 'conv1',
            participantId: 'p1',
            participantName: 'Patient: John Doe',
            participantRole: UserRole.patient,
            lastMessage: 'Je me sens mieux aujourd\'hui',
            lastMessageTime: DateTime.now().subtract(const Duration(minutes: 30)),
            unreadCount: 2,
            createdAt: DateTime.now().subtract(const Duration(days: 5)),
          ),
          Conversation(
            id: 'conv2',
            participantId: 'n1',
            participantName: 'Infirmière: Marie Chen',
            participantRole: UserRole.nurse,
            lastMessage: 'Alertes traitées pour patient P-001',
            lastMessageTime: DateTime.now().subtract(const Duration(hours: 2)),
            unreadCount: 0,
            createdAt: DateTime.now().subtract(const Duration(days: 3)),
          ),
        ]);
        _messages['conv1'] = [
          Message(
            id: 'm1',
            conversationId: 'conv1',
            senderId: 'p1',
            senderName: 'John Doe',
            senderRole: UserRole.patient,
            text: 'Bonjour Docteur, j\'ai une question concernant mon traitement.',
            timestamp: DateTime.now().subtract(const Duration(hours: 3)),
            isRead: true,
          ),
          Message(
            id: 'm2',
            conversationId: 'conv1',
            senderId: userId,
            senderName: 'Dr. Sarah',
            senderRole: UserRole.doctor,
            text: 'Bonjour, je suis là pour vous aider. Quelle est votre question ?',
            timestamp: DateTime.now().subtract(const Duration(hours: 2, minutes: 50)),
            isRead: true,
          ),
          Message(
            id: 'm3',
            conversationId: 'conv1',
            senderId: 'p1',
            senderName: 'John Doe',
            senderRole: UserRole.patient,
            text: 'Je me sens mieux aujourd\'hui',
            timestamp: DateTime.now().subtract(const Duration(minutes: 30)),
            isRead: false,
          ),
        ];
        break;
      case UserRole.nurse:
        _conversations.addAll([
          Conversation(
            id: 'conv3',
            participantId: 'd1',
            participantName: 'Dr. Sarah Johnson',
            participantRole: UserRole.doctor,
            lastMessage: 'Merci pour le rapport',
            lastMessageTime: DateTime.now().subtract(const Duration(hours: 1)),
            unreadCount: 0,
            createdAt: DateTime.now().subtract(const Duration(days: 4)),
          ),
          Conversation(
            id: 'conv4',
            participantId: 'p1',
            participantName: 'Patient: John Doe',
            participantRole: UserRole.patient,
            lastMessage: 'Besoin d\'aide pour les signes vitaux',
            lastMessageTime: DateTime.now().subtract(const Duration(minutes: 45)),
            unreadCount: 1,
            createdAt: DateTime.now().subtract(const Duration(days: 2)),
          ),
        ]);
        _messages['conv4'] = [
          Message(
            id: 'm4',
            conversationId: 'conv4',
            senderId: 'p1',
            senderName: 'John Doe',
            senderRole: UserRole.patient,
            text: 'Bonjour, pourriez-vous vérifier mes signes vitaux ?',
            timestamp: DateTime.now().subtract(const Duration(hours: 1)),
            isRead: true,
          ),
          Message(
            id: 'm5',
            conversationId: 'conv4',
            senderId: userId,
            senderName: 'Marie',
            senderRole: UserRole.nurse,
            text: 'Bien sûr, je vais passer dans quelques minutes.',
            timestamp: DateTime.now().subtract(const Duration(minutes: 50)),
            isRead: true,
          ),
          Message(
            id: 'm6',
            conversationId: 'conv4',
            senderId: 'p1',
            senderName: 'John Doe',
            senderRole: UserRole.patient,
            text: 'Besoin d\'aide pour les signes vitaux',
            timestamp: DateTime.now().subtract(const Duration(minutes: 45)),
            isRead: false,
          ),
        ];
        break;
      case UserRole.patient:
        _conversations.addAll([
          Conversation(
            id: 'conv5',
            participantId: 'd1',
            participantName: 'Dr. Sarah Johnson',
            participantRole: UserRole.doctor,
            lastMessage: 'Pensez à prendre vos médicaments',
            lastMessageTime: DateTime.now().subtract(const Duration(minutes: 20)),
            unreadCount: 0,
            createdAt: DateTime.now().subtract(const Duration(days: 7)),
          ),
          Conversation(
            id: 'conv6',
            participantId: 'n1',
            participantName: 'Infirmière: Marie Chen',
            participantRole: UserRole.nurse,
            lastMessage: 'Vos signes vitaux sont normaux',
            lastMessageTime: DateTime.now().subtract(const Duration(hours: 2)),
            unreadCount: 1,
            createdAt: DateTime.now().subtract(const Duration(days: 5)),
          ),
        ]);
        _messages['conv5'] = [
          Message(
            id: 'm7',
            conversationId: 'conv5',
            senderId: 'd1',
            senderName: 'Dr. Sarah',
            senderRole: UserRole.doctor,
            text: 'Comment vous sentez-vous aujourd\'hui ?',
            timestamp: DateTime.now().subtract(const Duration(hours: 3)),
            isRead: true,
          ),
          Message(
            id: 'm8',
            conversationId: 'conv5',
            senderId: userId,
            senderName: 'Patient',
            senderRole: UserRole.patient,
            text: 'Je me sens mieux, merci !',
            timestamp: DateTime.now().subtract(const Duration(hours: 2, minutes: 30)),
            isRead: true,
          ),
          Message(
            id: 'm9',
            conversationId: 'conv5',
            senderId: 'd1',
            senderName: 'Dr. Sarah',
            senderRole: UserRole.doctor,
            text: 'Pensez à prendre vos médicaments',
            timestamp: DateTime.now().subtract(const Duration(minutes: 20)),
            isRead: true,
          ),
        ];
        break;
    }

    _saveConversations();
    _saveMessages();
    notifyListeners();
  }

  Future<void> _saveConversations() async {
    final prefs = await SharedPreferences.getInstance();
    final json = jsonEncode(_conversations.map((c) => c.toJson()).toList());
    await prefs.setString('conversations_$_currentUserId', json);
  }

  Future<void> _saveMessages() async {
    final prefs = await SharedPreferences.getInstance();
    final json = jsonEncode(_messages.map((k, v) => MapEntry(k, v.map((m) => m.toJson()).toList())));
    await prefs.setString('messages_$_currentUserId', json);
  }

  Future<void> _loadConversations() async {
    if (_currentUserId == null) return;
    final prefs = await SharedPreferences.getInstance();
    final json = prefs.getString('conversations_$_currentUserId');
    if (json != null) {
      final list = jsonDecode(json) as List;
      _conversations.clear();
      _conversations.addAll(list.map((e) => Conversation.fromJson(e as Map<String, dynamic>)));
      notifyListeners();
    }
  }

  Future<void> _loadMessages() async {
    if (_currentUserId == null) return;
    final prefs = await SharedPreferences.getInstance();
    final json = prefs.getString('messages_$_currentUserId');
    if (json != null) {
      final map = jsonDecode(json) as Map<String, dynamic>;
      _messages.clear();
      map.forEach((key, value) {
        final list = value as List;
        _messages[key] = list.map((e) => Message.fromJson(e as Map<String, dynamic>)).toList();
      });
      notifyListeners();
    }
  }
}

