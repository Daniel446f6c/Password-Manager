import 'package:flutter/material.dart';

import '../services/biometric_service.dart';
import '../services/storage_service.dart';
import '../theme/app_theme.dart';
import 'create_vault_screen.dart';
import 'home_screen.dart';

/// Login screen for entering master password or using fingerprint.
class LoginScreen extends StatefulWidget {
  const LoginScreen({super.key});

  @override
  State<LoginScreen> createState() => _LoginScreenState();
}

class _LoginScreenState extends State<LoginScreen> {
  final _passwordController = TextEditingController();
  final _storageService = StorageService();
  final _biometricService = BiometricService();

  bool _isLoading = true; // Start loading to init storage
  bool _obscurePassword = true;
  bool _biometricAvailable = false;
  String? _errorMessage;

  @override
  void initState() {
    super.initState();
    _initializeApp();
  }

  Future<void> _initializeApp() async {
    await _storageService.requestStoragePermission();
    await _storageService.init();
    await _checkBiometric();

    if (mounted) {
      setState(() => _isLoading = false);
    }
  }

  Future<void> _checkBiometric() async {
    final available = await _biometricService.isFingerprintAvailable();
    final hasCached = _biometricService.hasPasswordCached();

    if (mounted) {
      setState(() {
        _biometricAvailable = available && hasCached;
      });
    }
  }

  Future<void> _login() async {
    final password = _passwordController.text;

    if (password.isEmpty) {
      setState(() => _errorMessage = 'Please enter your master password');
      return;
    }

    setState(() {
      _isLoading = true;
      _errorMessage = null;
    });

    try {
      final isValid = await _storageService.validatePassword(password);

      if (!mounted) return;

      if (isValid) {
        // Cache password for biometric unlock
        _biometricService.cachePassword(password);

        Navigator.pushReplacement(
          context,
          MaterialPageRoute(
            builder: (_) => HomeScreen(
              masterPassword: password,
              vaultPath: _storageService.currentVaultPath!,
            ),
          ),
        );
      } else {
        setState(() {
          _errorMessage = 'Wrong password';
          _isLoading = false;
        });
      }
    } catch (e) {
      setState(() {
        _errorMessage = 'Error: ${e.toString()}';
        _isLoading = false;
      });
    }
  }

  Future<void> _biometricLogin() async {
    if (!_biometricAvailable) return;

    setState(() {
      _isLoading = true;
      _errorMessage = null;
    });

    final authenticated = await _biometricService.authenticate();

    if (!mounted) return;

    if (authenticated) {
      final cachedPassword = _biometricService.getCachedPassword();
      if (cachedPassword != null) {
        Navigator.pushReplacement(
          context,
          MaterialPageRoute(
            builder: (_) => HomeScreen(
              masterPassword: cachedPassword,
              vaultPath: _storageService.currentVaultPath!,
            ),
          ),
        );
      } else {
        setState(() {
          _errorMessage = 'Please login with password first';
          _isLoading = false;
        });
      }
    } else {
      setState(() {
        _isLoading = false;
      });
    }
  }

  Future<void> _openVaultFile() async {
    final success = await _storageService.pickVaultFile();
    if (success && mounted) {
      await _storageService.init(); // Reload current path
      setState(() {
        _passwordController.clear();
        _errorMessage = null;
      });
    } else if (mounted) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(
          content: Text('Failed to open file or invalid format'),
          backgroundColor: AppTheme.errorColor,
        ),
      );
    }
  }

  void _navigateToCreate() {
    Navigator.push(
      context,
      MaterialPageRoute(builder: (_) => const CreateVaultScreen()),
    ).then((_) {
      if (mounted) setState(() {});
    });
  }

  @override
  void dispose() {
    _passwordController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: SafeArea(
        child: _isLoading
            ? const Center(child: CircularProgressIndicator())
            : FutureBuilder<bool>(
                future: _storageService.vaultExists(),
                builder: (context, snapshot) {
                  final vaultExists = snapshot.data ?? false;
                  final currentPath = _storageService.currentVaultPath;

                  return SingleChildScrollView(
                    padding: const EdgeInsets.all(24),
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.stretch,
                      children: [
                        const SizedBox(height: 40),

                        // App icon
                        Container(
                          width: 100,
                          height: 100,
                          decoration: BoxDecoration(
                            gradient: AppTheme.primaryGradient,
                            borderRadius: BorderRadius.circular(24),
                            boxShadow: [
                              BoxShadow(
                                color: const Color.fromRGBO(108, 99, 255, 0.3),
                                blurRadius: 20,
                                offset: const Offset(0, 10),
                              ),
                            ],
                          ),
                          child: const Icon(
                            Icons.lock_outline,
                            size: 50,
                            color: Colors.white,
                          ),
                        ),

                        const SizedBox(height: 32),

                        // Title
                        Text(
                          'KYOWMI-X',
                          style: Theme.of(context).textTheme.headlineLarge,
                          textAlign: TextAlign.center,
                        ),

                        const SizedBox(height: 8),

                        if (currentPath != null)
                          Padding(
                            padding: const EdgeInsets.symmetric(horizontal: 16),
                            child: Text(
                              vaultExists
                                  ? 'Vault: ...${_getFilename(currentPath)}'
                                  : 'Vault not found at path',
                              style: TextStyle(
                                fontSize: 12,
                                color: AppTheme.textSecondary,
                                fontFamily: 'monospace',
                              ),
                              textAlign: TextAlign.center,
                              overflow: TextOverflow.ellipsis,
                            ),
                          ),

                        const SizedBox(height: 40),

                        if (vaultExists) ...[
                          // Password field
                          TextField(
                            controller: _passwordController,
                            obscureText: _obscurePassword,
                            autofocus: true,
                            decoration: InputDecoration(
                              labelText: 'Master Password',
                              prefixIcon: const Icon(Icons.lock_outline),
                              suffixIcon: IconButton(
                                icon: Icon(
                                  _obscurePassword
                                      ? Icons.visibility_outlined
                                      : Icons.visibility_off_outlined,
                                ),
                                onPressed: () {
                                  setState(
                                    () => _obscurePassword = !_obscurePassword,
                                  );
                                },
                              ),
                            ),
                            onSubmitted: (_) => _login(),
                          ),

                          if (_errorMessage != null) ...[
                            const SizedBox(height: 12),
                            Text(
                              _errorMessage!,
                              style: TextStyle(
                                color: AppTheme.errorColor,
                                fontSize: 14,
                              ),
                              textAlign: TextAlign.center,
                            ),
                          ],

                          const SizedBox(height: 24),

                          // Login button
                          ElevatedButton(
                            onPressed: _isLoading ? null : _login,
                            child: _isLoading
                                ? const SizedBox(
                                    width: 24,
                                    height: 24,
                                    child: CircularProgressIndicator(
                                      strokeWidth: 2,
                                      color: Colors.white,
                                    ),
                                  )
                                : const Text('Unlock'),
                          ),

                          // Fingerprint button
                          if (_biometricAvailable) ...[
                            const SizedBox(height: 24),

                            const Row(
                              children: [
                                Expanded(child: Divider()),
                                Padding(
                                  padding: EdgeInsets.symmetric(horizontal: 16),
                                  child: Text(
                                    'or',
                                    style: TextStyle(
                                      color: AppTheme.textSecondary,
                                    ),
                                  ),
                                ),
                                Expanded(child: Divider()),
                              ],
                            ),

                            const SizedBox(height: 24),

                            Center(
                              child: InkWell(
                                onTap: _isLoading ? null : _biometricLogin,
                                borderRadius: BorderRadius.circular(40),
                                child: Container(
                                  width: 80,
                                  height: 80,
                                  decoration: BoxDecoration(
                                    color: AppTheme.surfaceColor,
                                    shape: BoxShape.circle,
                                    border: Border.all(
                                      color: const Color.fromRGBO(
                                        108,
                                        99,
                                        255,
                                        0.5,
                                      ),
                                      width: 2,
                                    ),
                                  ),
                                  child: const Icon(
                                    Icons.fingerprint,
                                    size: 40,
                                    color: AppTheme.primaryColor,
                                  ),
                                ),
                              ),
                            ),

                            const SizedBox(height: 8),

                            Text(
                              'Use fingerprint',
                              style: Theme.of(context).textTheme.bodyMedium,
                              textAlign: TextAlign.center,
                            ),
                          ],
                        ] else ...[
                          // Create vault button
                          ElevatedButton(
                            onPressed: _navigateToCreate,
                            style: ElevatedButton.styleFrom(
                              backgroundColor: AppTheme.primaryColor,
                            ),
                            child: const Text('Create New Vault'),
                          ),
                        ],

                        const SizedBox(height: 24),

                        // Secondary actions
                        Row(
                          mainAxisAlignment: MainAxisAlignment.center,
                          children: [
                            if (vaultExists)
                              TextButton(
                                onPressed: _navigateToCreate,
                                child: const Text('New'),
                              ),

                            if (vaultExists) const SizedBox(width: 16),

                            OutlinedButton.icon(
                              onPressed: _openVaultFile,
                              icon: const Icon(Icons.folder_open, size: 18),
                              label: const Text('Open File'),
                              style: OutlinedButton.styleFrom(
                                foregroundColor: AppTheme.textPrimary,
                                side: const BorderSide(
                                  color: AppTheme.textSecondary,
                                ),
                              ),
                            ),
                          ],
                        ),
                      ],
                    ),
                  );
                },
              ),
      ),
    );
  }

  String _getFilename(String path) {
    return path.split('/').last.split('\\').last;
  }
}
