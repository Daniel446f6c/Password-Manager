import 'dart:convert';
import 'dart:io';
import 'dart:typed_data';

import 'package:path_provider/path_provider.dart';
import 'package:permission_handler/permission_handler.dart';

import '../models/credential.dart';
import 'crypto_service.dart';

/// Handles .privVaultv2 file operations in shared storage.
class StorageService {
  static const String _vaultFileName = 'passwords.privVaultv2';
  static const String _appFolderName = 'KYOWMI-X';

  final CryptoService _crypto = CryptoService();

  /// Request and check storage permissions
  Future<bool> requestStoragePermission() async {
    // For Android 11+ (API 30+), use MANAGE_EXTERNAL_STORAGE
    if (Platform.isAndroid) {
      final status = await Permission.manageExternalStorage.request();
      if (status.isGranted) return true;

      // Fallback to legacy storage permission
      final legacyStatus = await Permission.storage.request();
      return legacyStatus.isGranted;
    }
    return true;
  }

  /// Get the vault file path in Documents folder
  Future<File> _getVaultFile() async {
    Directory? directory;

    if (Platform.isAndroid) {
      // Use Documents folder for persistence across reinstalls
      directory = Directory('/storage/emulated/0/Documents/$_appFolderName');
    } else {
      // Fallback for other platforms
      directory = await getApplicationDocumentsDirectory();
    }

    // Create app folder if it doesn't exist
    if (!await directory.exists()) {
      await directory.create(recursive: true);
    }

    return File('${directory.path}/$_vaultFileName');
  }

  /// Check if a vault file already exists
  Future<bool> vaultExists() async {
    final file = await _getVaultFile();
    return file.exists();
  }

  /// Get the vault file path (for display purposes)
  Future<String> getVaultPath() async {
    final file = await _getVaultFile();
    return file.path;
  }

  /// Create a new vault with the given master password
  Future<void> createVault(String password) async {
    final file = await _getVaultFile();

    // Store password hash at the beginning of the file
    final passwordHash = _crypto.hashPassword(password);

    // Empty credentials list encrypted
    final emptyData = _crypto.encrypt('[]', password);

    // Write: [hash (32 bytes)][encrypted data]
    final fullData = Uint8List(passwordHash.length + emptyData.length);
    fullData.setRange(0, passwordHash.length, passwordHash);
    fullData.setRange(passwordHash.length, fullData.length, emptyData);

    await file.writeAsBytes(fullData);
  }

  /// Validate the master password against the stored hash
  Future<bool> validatePassword(String password) async {
    final file = await _getVaultFile();

    if (!await file.exists()) return false;

    final data = await file.readAsBytes();
    if (data.length < CryptoService.hashByteLength) return false;

    final storedHash = data.sublist(0, CryptoService.hashByteLength);
    return _crypto.verifyPassword(password, storedHash);
  }

  /// Load and decrypt credentials from the vault
  Future<List<Credential>> loadCredentials(String password) async {
    final file = await _getVaultFile();

    if (!await file.exists()) return [];

    final data = await file.readAsBytes();
    if (data.length <= CryptoService.hashByteLength) return [];

    // Extract encrypted data (skip hash)
    final encryptedData = data.sublist(CryptoService.hashByteLength);

    try {
      final jsonString = _crypto.decrypt(encryptedData, password);
      final List<dynamic> jsonList = json.decode(jsonString);
      return jsonList.map((item) => Credential.fromJson(item)).toList();
    } catch (e) {
      // Decryption failed - wrong password or corrupted data
      return [];
    }
  }

  /// Encrypt and save credentials to the vault
  Future<void> saveCredentials(
    List<Credential> credentials,
    String password,
  ) async {
    final file = await _getVaultFile();

    // Serialize credentials to JSON
    final jsonList = credentials.map((c) => c.toJson()).toList();
    final jsonString = json.encode(jsonList);

    // Get password hash
    final passwordHash = _crypto.hashPassword(password);

    // Encrypt credentials
    final encryptedData = _crypto.encrypt(jsonString, password);

    // Write: [hash (32 bytes)][encrypted data]
    final fullData = Uint8List(passwordHash.length + encryptedData.length);
    fullData.setRange(0, passwordHash.length, passwordHash);
    fullData.setRange(passwordHash.length, fullData.length, encryptedData);

    await file.writeAsBytes(fullData);
  }

  /// Delete the vault file
  Future<void> deleteVault() async {
    final file = await _getVaultFile();
    if (await file.exists()) {
      await file.delete();
    }
  }
}
