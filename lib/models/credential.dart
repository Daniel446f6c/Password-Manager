import 'package:uuid/uuid.dart';

/// Represents a stored credential with application, username, and password.
class Credential {
  final String id;
  String application;
  String username;
  String password;

  Credential({
    String? id,
    required this.application,
    required this.username,
    required this.password,
  }) : id = id ?? const Uuid().v4();

  /// Create from JSON map
  factory Credential.fromJson(Map<String, dynamic> json) {
    return Credential(
      id: json['id'] as String,
      application: json['application'] as String,
      username: json['username'] as String,
      password: json['password'] as String,
    );
  }

  /// Convert to JSON map
  Map<String, dynamic> toJson() {
    return {
      'id': id,
      'application': application,
      'username': username,
      'password': password,
    };
  }

  /// Create a copy with optional modified fields
  Credential copyWith({
    String? application,
    String? username,
    String? password,
  }) {
    return Credential(
      id: id,
      application: application ?? this.application,
      username: username ?? this.username,
      password: password ?? this.password,
    );
  }
}
