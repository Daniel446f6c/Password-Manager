import 'package:flutter/services.dart';
import 'package:local_auth/local_auth.dart';

/// Provides fingerprint authentication for quick vault unlocking.
class BiometricService {
  final LocalAuthentication _auth = LocalAuthentication();

  /// Cached master password after successful biometric unlock
  String? _cachedPassword;

  /// Check if device supports biometric authentication
  Future<bool> isAvailable() async {
    try {
      final canCheck = await _auth.canCheckBiometrics;
      final isSupported = await _auth.isDeviceSupported();
      return canCheck && isSupported;
    } on PlatformException {
      return false;
    }
  }

  /// Check if fingerprint is specifically available
  Future<bool> isFingerprintAvailable() async {
    try {
      final biometrics = await _auth.getAvailableBiometrics();
      return biometrics.contains(BiometricType.fingerprint) ||
          biometrics.contains(BiometricType.strong);
    } on PlatformException {
      return false;
    }
  }

  /// Authenticate using fingerprint
  Future<bool> authenticate() async {
    try {
      return await _auth.authenticate(
        localizedReason: 'Unlock your password vault',
        options: const AuthenticationOptions(
          stickyAuth: true,
          biometricOnly: true,
        ),
      );
    } on PlatformException {
      return false;
    }
  }

  /// Store master password for biometric unlock session
  void cachePassword(String password) {
    _cachedPassword = password;
  }

  /// Get cached password after biometric authentication
  String? getCachedPassword() {
    return _cachedPassword;
  }

  /// Check if password is cached
  bool hasPasswordCached() {
    return _cachedPassword != null && _cachedPassword!.isNotEmpty;
  }

  /// Clear cached password (on logout/lock)
  void clearCache() {
    _cachedPassword = null;
  }
}
