import 'dart:convert';
import 'dart:io';
import 'dart:typed_data';

import 'package:flutter/foundation.dart'; // for compute
import 'package:file_picker/file_picker.dart';
import 'package:path_provider/path_provider.dart';
import 'package:permission_handler/permission_handler.dart';
import 'package:shared_preferences/shared_preferences.dart';

import '../models/credential.dart';
import 'crypto_service.dart';

/// Handles .pVv2 file operations in shared storage.
/// Uses [compute] to offload heavy encryption and JSON parsing to background isolates.
class StorageService {
  static const String _defaultVaultFileName = 'passwords.pVv2';
  static const String _appFolderName = 'KYOWMI-X';
  static const String _prefsKeyLastVaultPath = 'last_vault_path';

  String? _currentVaultPath;

  /// Get the currently active vault path
  String? get currentVaultPath => _currentVaultPath;

  /// Initialize service: load last used vault path
  Future<void> init() async {
    final prefs = await SharedPreferences.getInstance();
    final lastPath = prefs.getString(_prefsKeyLastVaultPath);

    if (lastPath != null && await File(lastPath).exists()) {
      _currentVaultPath = lastPath;
    } else {
      // If last path invalid or missing, prep default path but don't create yet
      _currentVaultPath = await _getDefaultVaultPath();
    }
  }

  /// Check if a vault with this name already exists at a specific directory path
  Future<bool> checkVaultExistsAtPath(
    String directoryPath,
    String filename,
  ) async {
    if (filename.trim().isEmpty) return false;

    String cleanName = filename.trim();
    if (!cleanName.endsWith('.pVv2')) {
      cleanName += '.pVv2';
    }

    final file = File('$directoryPath/$cleanName');
    return await file.exists();
  }

  /// Helper to get the default directory path used by the app
  Future<String> getAppDirectoryPath() async {
    if (Platform.isAndroid) {
      return '/storage/emulated/0/Documents/$_appFolderName';
    } else {
      final directory = await getApplicationDocumentsDirectory();
      return directory.path;
    }
  }

  /// Request and check storage permissions
  Future<bool> requestStoragePermission() async {
    if (Platform.isAndroid) {
      final status = await Permission.manageExternalStorage.request();
      if (status.isGranted) return true;

      // Fallback to legacy storage permission
      final legacyStatus = await Permission.storage.request();
      return legacyStatus.isGranted;
    }
    return true;
  }

  /// Get default path in Documents folder
  Future<String> _getDefaultVaultPath() async {
    Directory? directory;
    if (Platform.isAndroid) {
      directory = Directory('/storage/emulated/0/Documents/$_appFolderName');
    } else {
      directory = await getApplicationDocumentsDirectory();
    }

    if (!await directory.exists()) {
      await directory.create(recursive: true);
    }

    return '${directory.path}/$_defaultVaultFileName';
  }

  /// Get current working file
  File _getVaultFile() {
    if (_currentVaultPath == null) {
      throw Exception('Vault path not initialized');
    }
    return File(_currentVaultPath!);
  }

  /// Pick a vault file from system storage
  Future<bool> pickVaultFile() async {
    try {
      // On Android, filtering by custom extension .pVv2 often fails
      // because the OS doesn't know the MIME type. Using FileType.any is safer.
      FilePickerResult? result = await FilePicker.platform.pickFiles(
        type: FileType.any,
      );

      if (result != null && result.files.single.path != null) {
        final path = result.files.single.path!;

        // Manual extension check
        if (!path.endsWith('.pVv2')) {
          print('Error: Selected file is not a .pVv2 file');
          return false;
        }

        await setVaultPath(path);
        return true;
      }
      return false;
    } catch (e) {
      print('Error picking file: $e');
      return false;
    }
  }

  /// Set the current vault path and persist valid ones
  Future<void> setVaultPath(String path) async {
    if (await File(path).exists()) {
      _currentVaultPath = path;
      final prefs = await SharedPreferences.getInstance();
      await prefs.setString(_prefsKeyLastVaultPath, path);
    }
  }

  /// Check if a vault exists at current path
  Future<bool> vaultExists() async {
    if (_currentVaultPath == null) return false;
    return File(_currentVaultPath!).exists();
  }

  /// Create a new vault at default location (or update current if set)
  /// [customFilename] allows specifying a custom name (without extension)
  Future<void> createVault(String password, String customFilename) async {
    String path;

    if (customFilename.trim().isNotEmpty) {
      // Ensure file extension
      String filename = customFilename.trim();
      if (!filename.endsWith('.pVv2')) {
        filename += '.pVv2';
      }

      // Get directory
      Directory directory;
      if (Platform.isAndroid) {
        directory = Directory('/storage/emulated/0/Documents/$_appFolderName');
      } else {
        directory = await getApplicationDocumentsDirectory();
      }

      if (!await directory.exists()) {
        await directory.create(recursive: true);
      }

      path = '${directory.path}/$filename';
    } else {
      // Default behavior
      if (_currentVaultPath == null ||
          !await File(_currentVaultPath!).exists()) {
        _currentVaultPath = await _getDefaultVaultPath();
      }
      path = _currentVaultPath!;
    }

    // Run encryption logic in background isolate
    // We pass empty credentials list to initialize
    await compute(_isolatedSave, _SaveArgs(path, password, []));

    // Persist this path as the active one
    await setVaultPath(path);
  }

  /// Validate password
  Future<bool> validatePassword(String password) async {
    if (!await vaultExists()) return false;
    // Attempt load in background - if it returns null or throws, invalid
    try {
      final file = _getVaultFile();
      // Optimization: Read header only first?
      // For now, simpler to just attempt full load check or specialized check
      // Let's implement a specialized isolated check
      return await compute(
        _isolatedValidate,
        _ValidateArgs(file.path, password),
      );
    } catch (e) {
      return false;
    }
  }

  /// Load credentials
  Future<List<Credential>> loadCredentials(String password) async {
    if (!await vaultExists()) return [];
    final file = _getVaultFile();

    try {
      // Run heavy task in background isolate
      return await compute(_isolatedLoad, _LoadArgs(file.path, password));
    } catch (e) {
      return [];
    }
  }

  /// Save credentials
  Future<void> saveCredentials(
    List<Credential> credentials,
    String password,
  ) async {
    final file = _getVaultFile();
    // Run heavy task in background isolate
    // We convert Credential list to basic maps if needed, but Credentials assume JSON safe
    // Actually, passing complex objects to isolates works if they are immutable/simple.
    // But passing primitive lists is safest.
    // _isolatedSave handles the serialization.
    await compute(_isolatedSave, _SaveArgs(file.path, password, credentials));
  }

  /// Delete current vault
  Future<void> deleteVault() async {
    if (await vaultExists()) {
      await _getVaultFile().delete();
      _currentVaultPath = null;
      final prefs = await SharedPreferences.getInstance();
      await prefs.remove(_prefsKeyLastVaultPath);
    }
  }
}

// --- Isolated Functions (must be top-level) ---

class _SaveArgs {
  final String path;
  final String password;
  final List<Credential> credentials;
  _SaveArgs(this.path, this.password, this.credentials);
}

class _LoadArgs {
  final String path;
  final String password;
  _LoadArgs(this.path, this.password);
}

class _ValidateArgs {
  final String path;
  final String password;
  _ValidateArgs(this.path, this.password);
}

Future<void> _isolatedSave(_SaveArgs args) async {
  final crypto = CryptoService();
  final file = File(args.path);

  // 1. Serialize
  final jsonList = args.credentials.map((c) => c.toJson()).toList();
  final jsonString = json.encode(jsonList);

  // 2. Encrypt
  final passwordHash = crypto.hashPassword(args.password);
  final encryptedData = crypto.encrypt(jsonString, args.password);

  // 3. Write
  final bytes = BytesBuilder();
  bytes.add(passwordHash);
  bytes.add(encryptedData);

  // Using synchronous write for atomicity within the isolate (still async to OS but blocking THIS flow)
  // or await writeAsBytes. Since we are in an isolate, we can blocking write or async write.
  await file.writeAsBytes(bytes.toBytes(), flush: true);
}

Future<List<Credential>> _isolatedLoad(_LoadArgs args) async {
  final crypto = CryptoService();
  final file = File(args.path);

  if (!file.existsSync()) return [];

  final data = await file.readAsBytes();

  if (data.length <= CryptoService.hashByteLength) return [];

  final encryptedData = data.sublist(CryptoService.hashByteLength);

  try {
    final jsonString = crypto.decrypt(encryptedData, args.password);
    final List<dynamic> jsonList = json.decode(jsonString);
    return jsonList.map((item) => Credential.fromJson(item)).toList();
  } catch (e) {
    throw Exception('Failed to decrypt or parse');
  }
}

Future<bool> _isolatedValidate(_ValidateArgs args) async {
  final crypto = CryptoService();
  final file = File(args.path);

  if (!file.existsSync()) return false;

  final data = await file.readAsBytes();
  if (data.length < CryptoService.hashByteLength) return false;

  final storedHash = data.sublist(0, CryptoService.hashByteLength);
  return crypto.verifyPassword(args.password, storedHash);
}
