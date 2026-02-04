import 'dart:convert';
import 'dart:math';
import 'dart:typed_data';

import 'package:crypto/crypto.dart';
import 'package:encrypt/encrypt.dart';
import 'package:pointycastle/export.dart' as pc;

/// Hardened CryptoService providing AES-256-GCM encryption and SHA-256 hashing.
/// Uses PointyCastle for verified PBKDF2 key derivation.
class CryptoService {
  static const int _keyBitLength = 256;
  static const int _ivByteLength = 12; // 96 bits for GCM
  static const int _hashByteLength = 32; // SHA-256 output
  static const int _pbkdfIterations = 580000;
  static const int _saltLength = 16;

  /// Generate a SHA-256 hash of the password for verification
  Uint8List hashPassword(String password) {
    final bytes = utf8.encode(password);
    final digest = sha256.convert(bytes);
    return Uint8List.fromList(digest.bytes);
  }

  /// Verify password against stored hash using constant-time comparison
  bool verifyPassword(String password, Uint8List storedHash) {
    final computedHash = hashPassword(password);
    if (computedHash.length != storedHash.length) return false;

    int result = 0;
    for (int i = 0; i < computedHash.length; i++) {
      result |= computedHash[i] ^ storedHash[i];
    }
    return result == 0;
  }

  /// Derive a 256-bit key using verified PointyCastle PBKDF2 implementation
  Uint8List _deriveKey(String password, Uint8List salt) {
    // Set up PBKDF2 with HMAC-SHA256
    final derivator = pc.PBKDF2KeyDerivator(pc.HMac(pc.SHA256Digest(), 64));

    // Initialize with salt, iterations, and desired key length (32 bytes = 256 bits)
    derivator.init(
      pc.Pbkdf2Parameters(salt, _pbkdfIterations, _keyBitLength ~/ 8),
    );

    return derivator.process(Uint8List.fromList(utf8.encode(password)));
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
