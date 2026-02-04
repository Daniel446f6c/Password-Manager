import 'dart:convert';
import 'dart:math';
import 'dart:typed_data';

import 'package:argon2/argon2.dart';
import 'package:encrypt/encrypt.dart';

/// Hardened CryptoService providing AES-256-GCM encryption and Argon2id hashing.
/// Uses Argon2id for both password verification and key derivation.
class CryptoService {
  static const int _ivByteLength = 12; // 96 bits for GCM
  // Hash format: [salt (16 bytes)] + [hash (32 bytes)] = 48 bytes
  static const int _saltLength = 16;
  static const int _hashLength = 32;
  static const int _hashByteLength = _saltLength + _hashLength;

  // Argon2 Configuration
  static const int _argon2Iterations = 3; // Time cost
  static const int _argon2Memory = 256 * 1024; // Memory cost (256 MB)
  static const int _argon2KeyLength = 32; // 256 bits

  /// Generate an Argon2id hash of the password for verification
  /// Returns: [salt (16 bytes)][hash (32 bytes)]
  Uint8List hashPassword(String password) {
    final salt = _generateSalt();
    final hash = _argon2id(password, salt, _hashLength);
    return Uint8List.fromList([...salt, ...hash]);
  }

  /// Verify password against stored [salt][hash] data
  bool verifyPassword(String password, Uint8List storedData) {
    if (storedData.length != _hashByteLength) return false;

    final salt = storedData.sublist(0, _saltLength);
    final storedHash = storedData.sublist(_saltLength);

    final computedHash = _argon2id(password, salt, _hashLength);

    // Constant-time comparison
    int result = 0;
    for (int i = 0; i < computedHash.length; i++) {
      result |= computedHash[i] ^ storedHash[i];
    }
    return result == 0;
  }

  /// Internal Argon2id implementation
  Uint8List _argon2id(String password, Uint8List salt, int length) {
    final parameters = Argon2Parameters(
      Argon2Parameters.ARGON2_id,
      salt,
      version: Argon2Parameters.ARGON2_VERSION_13,
      iterations: _argon2Iterations,
      memory: _argon2Memory,
    );

    final argon2 = Argon2BytesGenerator();
    argon2.init(parameters);

    final result = Uint8List(length);
    argon2.generateBytes(utf8.encode(password), result, 0, result.length);
    return result;
  }

  /// Derive a 256-bit key using Argon2id
  Uint8List _deriveKey(String password, Uint8List salt) {
    return _argon2id(password, salt, _argon2KeyLength);
  }

  /// Generate a random salt using a cryptographically secure generator
  Uint8List _generateSalt() {
    final random = Random.secure();
    return Uint8List.fromList(
      List<int>.generate(_saltLength, (_) => random.nextInt(256)),
    );
  }

  /// Generate a random IV using a cryptographically secure generator
  Uint8List _generateIV() {
    final random = Random.secure();
    return Uint8List.fromList(
      List<int>.generate(_ivByteLength, (_) => random.nextInt(256)),
    );
  }

  /// Encrypt data using AES-256-GCM with password-derived key.
  /// Format: [salt (16 bytes)][iv (12 bytes)][ciphertext + tag]
  Uint8List encrypt(String plaintext, String password) {
    final salt = _generateSalt();
    final key = _deriveKey(password, salt);
    final iv = _generateIV();

    final encrypter = Encrypter(AES(Key(key), mode: AESMode.gcm));
    final encrypted = encrypter.encrypt(plaintext, iv: IV(iv));

    // Combine: salt + iv + ciphertext using BytesBuilder for efficiency
    final builder = BytesBuilder();
    builder.add(salt);
    builder.add(iv);
    builder.add(encrypted.bytes);

    return builder.toBytes();
  }

  /// Decrypt data using AES-256-GCM with password-derived key.
  String decrypt(Uint8List cipherData, String password) {
    if (cipherData.length < _saltLength + _ivByteLength) {
      throw ArgumentError('Invalid cipher data length');
    }

    // Extract salt, iv, and ciphertext
    final salt = cipherData.sublist(0, _saltLength);
    final iv = cipherData.sublist(_saltLength, _saltLength + _ivByteLength);
    final ciphertext = cipherData.sublist(_saltLength + _ivByteLength);

    final key = _deriveKey(password, salt);
    final encrypter = Encrypter(AES(Key(key), mode: AESMode.gcm));

    return encrypter.decrypt(Encrypted(ciphertext), iv: IV(iv));
  }

  /// Get the hash byte length for file format parsing
  static int get hashByteLength => _hashByteLength;
}
