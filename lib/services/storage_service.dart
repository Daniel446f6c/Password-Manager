import 'dart:convert';
import 'dart:io';
import 'dart:typed_data';

import 'package:file_picker/file_picker.dart';
import 'package:path_provider/path_provider.dart';
import 'package:permission_handler/permission_handler.dart';
import 'package:shared_preferences/shared_preferences.dart';

import '../models/credential.dart';
import 'crypto_service.dart';

/// Handles .pVv2 file operations in shared storage.
class StorageService {
  static const String _defaultVaultFileName = 'passwords.pVv2';
  static const String _appFolderName = 'KYOWMI-X';
  static const String _prefsKeyLastVaultPath = 'last_vault_path';

  final CryptoService _crypto = CryptoService();
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
    File file;

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

      file = File('${directory.path}/$filename');
    } else {
      // Default behavior
      if (_currentVaultPath == null ||
          !await File(_currentVaultPath!).exists()) {
        _currentVaultPath = await _getDefaultVaultPath();
      }
      file = _getVaultFile();
    }

    // Store password hash
    final passwordHash = _crypto.hashPassword(password);

    // Empty encrypted credentials
    final emptyData = _crypto.encrypt('[]', password);

    // Write: [hash][data]
    final bytes = BytesBuilder();
    bytes.add(passwordHash);
    bytes.add(emptyData);

    await file.writeAsBytes(bytes.toBytes());

    // Persist this path as the active one
    await setVaultPath(file.path);
  }

  /// Validate password
  Future<bool> validatePassword(String password) async {
    if (!await vaultExists()) return false;

    final file = _getVaultFile();
    final data = await file.readAsBytes();

    if (data.length < CryptoService.hashByteLength) return false;

    final storedHash = data.sublist(0, CryptoService.hashByteLength);
    return _crypto.verifyPassword(password, storedHash);
  }

  /// Load credentials
  Future<List<Credential>> loadCredentials(String password) async {
    if (!await vaultExists()) return [];

    final file = _getVaultFile();
    final data = await file.readAsBytes();

    if (data.length <= CryptoService.hashByteLength) return [];

    final encryptedData = data.sublist(CryptoService.hashByteLength);

    try {
      final jsonString = _crypto.decrypt(encryptedData, password);
      final List<dynamic> jsonList = json.decode(jsonString);
      return jsonList.map((item) => Credential.fromJson(item)).toList();
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

    final jsonList = credentials.map((c) => c.toJson()).toList();
    final jsonString = json.encode(jsonList);

    final passwordHash = _crypto.hashPassword(password);
    final encryptedData = _crypto.encrypt(jsonString, password);

    final bytes = BytesBuilder();
    bytes.add(passwordHash);
    bytes.add(encryptedData);

    await file.writeAsBytes(bytes.toBytes());
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
