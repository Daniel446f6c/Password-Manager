import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

import '../models/credential.dart';
import '../services/biometric_service.dart';
import '../services/password_generator.dart';
import '../services/storage_service.dart';
import '../theme/app_theme.dart';
import 'login_screen.dart';

/// Home screen displaying the list of credentials with CRUD operations.
class HomeScreen extends StatefulWidget {
  final String masterPassword;
  final String vaultPath;

  const HomeScreen({
    super.key,
    required this.masterPassword,
    required this.vaultPath,
  });

  @override
  State<HomeScreen> createState() => _HomeScreenState();
}

class _HomeScreenState extends State<HomeScreen> {
  final _storageService = StorageService();
  final _biometricService = BiometricService();
  final _passwordGenerator = PasswordGenerator();

  List<Credential> _credentials = [];
  bool _isLoading = true;
  String? _revealedPasswordId;

  @override
  void initState() {
    super.initState();
    _initStorageAndLoad();
  }

  Future<void> _initStorageAndLoad() async {
    // Initialize storage with the correct path passed from Login
    await _storageService.setVaultPath(widget.vaultPath);
    await _loadCredentials();
  }

  Future<void> _loadCredentials() async {
    setState(() => _isLoading = true);

    try {
      final credentials = await _storageService.loadCredentials(
        widget.masterPassword,
      );
      if (mounted) {
        setState(() {
          _credentials = credentials;
          _isLoading = false;
        });
      }
    } catch (e) {
      if (mounted) {
        setState(() => _isLoading = false);
        _showError('Failed to load credentials');
      }
    }
  }

  /// Auto-save credentials silently
  Future<void> _saveCredentials() async {
    try {
      await _storageService.saveCredentials(
        _credentials,
        widget.masterPassword,
      );
    } catch (e) {
      _showError('Failed to save changes');
    }
  }

  void _showError(String message) {
    if (!mounted) return;
    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(content: Text(message), backgroundColor: AppTheme.errorColor),
    );
  }

  void _showSuccess(String message) {
    if (!mounted) return;
    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(content: Text(message), backgroundColor: AppTheme.successColor),
    );
  }

  void _addCredential() {
    _showCredentialDialog(null);
  }

  void _editCredential(Credential credential) {
    _showCredentialDialog(credential);
  }

  void _deleteCredential(Credential credential) {
    showDialog(
      context: context,
      builder: (context) => AlertDialog(
        title: const Text('Delete Credential'),
        content: Text('Delete "${credential.application}"?'),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(context),
            child: const Text('Cancel'),
          ),
          TextButton(
            onPressed: () {
              Navigator.pop(context);
              setState(() {
                _credentials.removeWhere((c) => c.id == credential.id);
              });
              _saveCredentials(); // Auto-save
              _showSuccess('Credential deleted');
            },
            child: Text('Delete', style: TextStyle(color: AppTheme.errorColor)),
          ),
        ],
      ),
    );
  }

  void _showCredentialDialog(Credential? credential) {
    final isEditing = credential != null;
    final appController = TextEditingController(
      text: credential?.application ?? '',
    );
    final userController = TextEditingController(
      text: credential?.username ?? '',
    );
    final passController = TextEditingController(
      text: credential?.password ?? '',
    );

    int entropy = credential != null
        ? _passwordGenerator.calculateEntropy(credential.password)
        : 0;

    showDialog(
      context: context,
      builder: (context) => StatefulBuilder(
        builder: (context, setDialogState) {
          void updateEntropy() {
            setDialogState(() {
              entropy = _passwordGenerator.calculateEntropy(
                passController.text,
              );
            });
          }

          return AlertDialog(
            title: Text(isEditing ? 'Edit Credential' : 'Add Credential'),
            content: SingleChildScrollView(
              child: Column(
                mainAxisSize: MainAxisSize.min,
                crossAxisAlignment: CrossAxisAlignment.stretch,
                children: [
                  TextField(
                    controller: appController,
                    decoration: const InputDecoration(
                      labelText: 'Application',
                      prefixIcon: Icon(Icons.apps),
                    ),
                  ),
                  const SizedBox(height: 16),
                  TextField(
                    controller: userController,
                    decoration: const InputDecoration(
                      labelText: 'Username',
                      prefixIcon: Icon(Icons.person_outline),
                    ),
                  ),
                  const SizedBox(height: 16),
                  TextField(
                    controller: passController,
                    decoration: InputDecoration(
                      labelText: 'Password',
                      prefixIcon: const Icon(Icons.lock_outline),
                      suffixIcon: IconButton(
                        icon: const Icon(Icons.auto_awesome),
                        tooltip: 'Generate',
                        onPressed: () {
                          passController.text = _passwordGenerator.generate();
                          updateEntropy();
                        },
                      ),
                    ),
                    onChanged: (_) => updateEntropy(),
                  ),
                  const SizedBox(height: 12),

                  // Entropy indicator
                  if (passController.text.isNotEmpty)
                    Container(
                      padding: const EdgeInsets.symmetric(
                        horizontal: 12,
                        vertical: 8,
                      ),
                      decoration: BoxDecoration(
                        color: AppTheme.surfaceColor,
                        borderRadius: BorderRadius.circular(8),
                      ),
                      child: Row(
                        children: [
                          Icon(
                            Icons.security,
                            size: 18,
                            color: _getEntropyColor(entropy),
                          ),
                          const SizedBox(width: 8),
                          Text(
                            'Entropy: $entropy bits',
                            style: TextStyle(
                              fontSize: 13,
                              color: _getEntropyColor(entropy),
                              fontWeight: FontWeight.w500,
                            ),
                          ),
                          const Spacer(),
                          Text(
                            _passwordGenerator.getStrengthLabel(entropy),
                            style: TextStyle(
                              fontSize: 12,
                              color: _getEntropyColor(entropy),
                            ),
                          ),
                        ],
                      ),
                    ),
                ],
              ),
            ),
            actions: [
              TextButton(
                onPressed: () => Navigator.pop(context),
                child: const Text('Cancel'),
              ),
              ElevatedButton(
                onPressed: () {
                  final app = appController.text.trim();
                  final user = userController.text.trim();
                  final pass = passController.text;

                  if (app.isEmpty || user.isEmpty || pass.isEmpty) {
                    return;
                  }

                  Navigator.pop(context);

                  setState(() {
                    if (isEditing) {
                      final index = _credentials.indexWhere(
                        (c) => c.id == credential.id,
                      );
                      if (index != -1) {
                        _credentials[index] = credential.copyWith(
                          application: app,
                          username: user,
                          password: pass,
                        );
                      }
                    } else {
                      _credentials.add(
                        Credential(
                          application: app,
                          username: user,
                          password: pass,
                        ),
                      );
                    }
                  });

                  _saveCredentials(); // Auto-save
                  _showSuccess(
                    isEditing ? 'Credential updated' : 'Credential added',
                  );
                },
                child: Text(isEditing ? 'Save' : 'Add'),
              ),
            ],
          );
        },
      ),
    );
  }

  Color _getEntropyColor(int entropy) {
    if (entropy < 28) return AppTheme.errorColor;
    if (entropy < 36) return Colors.orange;
    if (entropy < 60) return Colors.yellow;
    if (entropy < 80) return Colors.lightGreen;
    return AppTheme.successColor;
  }

  void _copyPassword(Credential credential) {
    Clipboard.setData(ClipboardData(text: credential.password));
    _showSuccess('Password copied');
  }

  void _togglePasswordVisibility(String id) {
    setState(() {
      _revealedPasswordId = _revealedPasswordId == id ? null : id;
    });
  }

  void _logout() {
    _biometricService.clearCache();
    Navigator.pushReplacement(
      context,
      MaterialPageRoute(builder: (_) => const LoginScreen()),
    );
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('My Passwords'),
        automaticallyImplyLeading: false,
        actions: [
          IconButton(
            icon: const Icon(Icons.logout),
            onPressed: _logout,
            tooltip: 'Lock',
          ),
        ],
      ),
      body: _isLoading
          ? const Center(child: CircularProgressIndicator())
          : _credentials.isEmpty
          ? _buildEmptyState()
          : _buildCredentialsList(),
      floatingActionButton: _isLoading
          ? null
          : FloatingActionButton(
              onPressed: _addCredential,
              child: const Icon(Icons.add),
            ),
    );
  }

  Widget _buildEmptyState() {
    return Center(
      child: Column(
        mainAxisAlignment: MainAxisAlignment.center,
        children: [
          Icon(
            Icons.lock_outline,
            size: 80,
            color: const Color.fromRGBO(179, 179, 179, 0.5),
          ),
          const SizedBox(height: 16),
          Text(
            'No passwords yet',
            style: Theme.of(
              context,
            ).textTheme.titleLarge?.copyWith(color: AppTheme.textSecondary),
          ),
          const SizedBox(height: 8),
          Text(
            'Tap + to add your first credential',
            style: Theme.of(context).textTheme.bodyMedium,
          ),
        ],
      ),
    );
  }

  Widget _buildCredentialsList() {
    return ListView.builder(
      padding: const EdgeInsets.all(16),
      itemCount: _credentials.length,
      itemBuilder: (context, index) {
        final credential = _credentials[index];
        final isRevealed = _revealedPasswordId == credential.id;

        return Dismissible(
          key: Key(credential.id),
          direction: DismissDirection.endToStart,
          background: Container(
            alignment: Alignment.centerRight,
            padding: const EdgeInsets.only(right: 20),
            decoration: BoxDecoration(
              color: AppTheme.errorColor,
              borderRadius: BorderRadius.circular(16),
            ),
            child: const Icon(Icons.delete_outline, color: Colors.white),
          ),
          confirmDismiss: (direction) async {
            _deleteCredential(credential);
            return false; // We handle deletion manually
          },
          child: Card(
            margin: const EdgeInsets.only(bottom: 12),
            child: InkWell(
              onTap: () => _editCredential(credential),
              borderRadius: BorderRadius.circular(16),
              child: Padding(
                padding: const EdgeInsets.all(16),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Row(
                      children: [
                        Container(
                          width: 44,
                          height: 44,
                          decoration: BoxDecoration(
                            gradient: AppTheme.primaryGradient,
                            borderRadius: BorderRadius.circular(12),
                          ),
                          child: Center(
                            child: Text(
                              credential.application.isNotEmpty
                                  ? credential.application[0].toUpperCase()
                                  : '?',
                              style: const TextStyle(
                                color: Colors.white,
                                fontSize: 20,
                                fontWeight: FontWeight.bold,
                              ),
                            ),
                          ),
                        ),
                        const SizedBox(width: 12),
                        Expanded(
                          child: Column(
                            crossAxisAlignment: CrossAxisAlignment.start,
                            children: [
                              Text(
                                credential.application,
                                style: const TextStyle(
                                  fontSize: 16,
                                  fontWeight: FontWeight.w600,
                                ),
                              ),
                              const SizedBox(height: 2),
                              Text(
                                credential.username,
                                style: TextStyle(
                                  fontSize: 14,
                                  color: AppTheme.textSecondary,
                                ),
                              ),
                            ],
                          ),
                        ),
                      ],
                    ),
                    const SizedBox(height: 12),
                    Row(
                      children: [
                        Expanded(
                          child: Container(
                            padding: const EdgeInsets.symmetric(
                              horizontal: 12,
                              vertical: 8,
                            ),
                            decoration: BoxDecoration(
                              color: AppTheme.backgroundColor,
                              borderRadius: BorderRadius.circular(8),
                            ),
                            child: Text(
                              isRevealed ? credential.password : '•' * 12,
                              style: TextStyle(
                                fontFamily: 'monospace',
                                fontSize: 14,
                                color: isRevealed
                                    ? AppTheme.textPrimary
                                    : AppTheme.textSecondary,
                              ),
                            ),
                          ),
                        ),
                        const SizedBox(width: 8),
                        IconButton(
                          onPressed: () =>
                              _togglePasswordVisibility(credential.id),
                          icon: Icon(
                            isRevealed
                                ? Icons.visibility_off_outlined
                                : Icons.visibility_outlined,
                            size: 20,
                          ),
                          tooltip: isRevealed ? 'Hide' : 'Reveal',
                        ),
                        IconButton(
                          onPressed: () => _copyPassword(credential),
                          icon: const Icon(Icons.copy, size: 20),
                          tooltip: 'Copy',
                        ),
                      ],
                    ),
                  ],
                ),
              ),
            ),
          ),
        );
      },
    );
  }
}
