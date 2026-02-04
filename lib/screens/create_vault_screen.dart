import 'package:flutter/material.dart';

import '../services/storage_service.dart';
import '../theme/app_theme.dart';

/// Screen for creating a new password vault with master password.
class CreateVaultScreen extends StatefulWidget {
  const CreateVaultScreen({super.key});

  @override
  State<CreateVaultScreen> createState() => _CreateVaultScreenState();
}

class _CreateVaultScreenState extends State<CreateVaultScreen> {
  final _filenameController = TextEditingController();
  final _passwordController = TextEditingController();
  final _confirmController = TextEditingController();
  final _storageService = StorageService();

  bool _isLoading = false;
  bool _obscurePassword = true;
  bool _obscureConfirm = true;
  String? _errorMessage;
  bool _vaultCreated = false;

  Future<void> _createVault() async {
    final filename = _filenameController.text.trim();
    final password = _passwordController.text;
    final confirm = _confirmController.text;

    if (filename.isEmpty) {
      setState(() => _errorMessage = 'Please enter a vault name');
      return;
    }

    // Validations
    if (password.length < 8) {
      setState(() => _errorMessage = 'Password must be at least 8 characters');
      return;
    }

    if (password != confirm) {
      setState(() => _errorMessage = 'Passwords do not match');
      return;
    }

    setState(() {
      _isLoading = true;
      _errorMessage = null;
    });

    try {
      final hasPermission = await _storageService.requestStoragePermission();
      if (!hasPermission) {
        setState(() {
          _errorMessage = 'Storage permission required';
          _isLoading = false;
        });
        return;
      }

      // 1. Determine target directory (Default or User Chosen)
      // For now, using your default app directory logic:
      final targetDir = await _storageService.getAppDirectoryPath();

      // 2. CHECK IF FILE EXISTS IN THAT SPECIFIC FOLDER
      final alreadyExists = await _storageService.checkVaultExistsAtPath(
        targetDir,
        filename,
      );

      if (alreadyExists) {
        setState(() {
          _errorMessage =
              'A file named "$filename.pVv2" already exists in this folder.';
          _isLoading = false;
        });
        return;
      }

      // 3. Create the vault
      await _storageService.createVault(password, filename);

      if (mounted) {
        setState(() {
          _vaultCreated = true;
          _isLoading = false;
        });
      }
    } catch (e) {
      setState(() {
        _errorMessage = 'Error creating vault: ${e.toString()}';
        _isLoading = false;
      });
    }
  }

  void _goBack() {
    Navigator.pop(context);
  }

  @override
  void dispose() {
    _passwordController.dispose();
    _confirmController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Create Vault'),
        leading: IconButton(
          icon: const Icon(Icons.arrow_back),
          onPressed: _goBack,
        ),
      ),
      body: SafeArea(
        child: SingleChildScrollView(
          padding: const EdgeInsets.all(24),
          child: _vaultCreated ? _buildSuccessView() : _buildFormView(),
        ),
      ),
    );
  }

  Widget _buildFormView() {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.stretch,
      children: [
        const SizedBox(height: 24),

        // Icon
        Container(
          width: 80,
          height: 80,
          decoration: BoxDecoration(
            color: AppTheme.surfaceColor,
            borderRadius: BorderRadius.circular(20),
          ),
          child: const Icon(
            Icons.add_circle_outline,
            size: 40,
            color: AppTheme.primaryColor,
          ),
        ),

        const SizedBox(height: 24),

        Text(
          'Create a New Vault',
          style: Theme.of(context).textTheme.headlineMedium,
          textAlign: TextAlign.center,
        ),

        const SizedBox(height: 8),

        Text(
          'Choose a strong master password. You\'ll need this to unlock your vault.',
          style: Theme.of(context).textTheme.bodyMedium,
          textAlign: TextAlign.center,
        ),

        const SizedBox(height: 40),

        // Filename field
        TextField(
          controller: _filenameController,
          decoration: InputDecoration(
            labelText: 'Vault Name',
            hintText: 'Enter vault name',
            prefixIcon: const Icon(Icons.lock_outline),
            suffixIcon: IconButton(
              icon: Icon(
                _obscurePassword
                    ? Icons.visibility_outlined
                    : Icons.visibility_off_outlined,
              ),
              onPressed: () {
                setState(() => _obscurePassword = !_obscurePassword);
              },
            ),
          ),
        ),

        const SizedBox(height: 16),

        // Password field
        TextField(
          controller: _passwordController,
          obscureText: _obscurePassword,
          autofocus: true,
          decoration: InputDecoration(
            labelText: 'Master Password',
            hintText: 'Minimum 8 characters',
            prefixIcon: const Icon(Icons.lock_outline),
            suffixIcon: IconButton(
              icon: Icon(
                _obscurePassword
                    ? Icons.visibility_outlined
                    : Icons.visibility_off_outlined,
              ),
              onPressed: () {
                setState(() => _obscurePassword = !_obscurePassword);
              },
            ),
          ),
        ),

        const SizedBox(height: 16),

        // Confirm password field
        TextField(
          controller: _confirmController,
          obscureText: _obscureConfirm,
          decoration: InputDecoration(
            labelText: 'Confirm Password',
            prefixIcon: const Icon(Icons.lock_outline),
            suffixIcon: IconButton(
              icon: Icon(
                _obscureConfirm
                    ? Icons.visibility_outlined
                    : Icons.visibility_off_outlined,
              ),
              onPressed: () {
                setState(() => _obscureConfirm = !_obscureConfirm);
              },
            ),
          ),
          onSubmitted: (_) => _createVault(),
        ),

        if (_errorMessage != null) ...[
          const SizedBox(height: 12),
          Text(
            _errorMessage!,
            style: const TextStyle(color: AppTheme.errorColor, fontSize: 14),
            textAlign: TextAlign.center,
          ),
        ],

        const SizedBox(height: 32),

        // Create button
        ElevatedButton(
          onPressed: _isLoading ? null : _createVault,
          child: _isLoading
              ? const SizedBox(
                  width: 24,
                  height: 24,
                  child: CircularProgressIndicator(
                    strokeWidth: 2,
                    color: Colors.white,
                  ),
                )
              : const Text('Create Vault'),
        ),

        const SizedBox(height: 24),

        // Warning
        Container(
          padding: const EdgeInsets.all(16),
          decoration: BoxDecoration(
            color: const Color.fromRGBO(207, 102, 121, 0.1),
            borderRadius: BorderRadius.circular(12),
            border: Border.all(color: const Color.fromRGBO(207, 102, 121, 0.3)),
          ),
          child: Row(
            children: [
              const Icon(
                Icons.warning_amber_rounded,
                color: AppTheme.errorColor,
              ),
              const SizedBox(width: 12),
              Expanded(
                child: Text(
                  'Creating a new vault may take a few seconds.',
                  style: TextStyle(
                    color: const Color.fromRGBO(207, 102, 121, 0.9),
                    fontSize: 13,
                  ),
                ),
              ),
            ],
          ),
        ),
      ],
    );
  }

  Widget _buildSuccessView() {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.stretch,
      children: [
        const SizedBox(height: 60),

        Container(
          width: 100,
          height: 100,
          decoration: BoxDecoration(
            color: const Color.fromRGBO(76, 175, 80, 0.1),
            shape: BoxShape.circle,
          ),
          child: const Icon(
            Icons.check_circle_outline,
            size: 60,
            color: AppTheme.successColor,
          ),
        ),

        const SizedBox(height: 32),

        Text(
          'Vault Created!',
          style: Theme.of(context).textTheme.headlineMedium,
          textAlign: TextAlign.center,
        ),

        const SizedBox(height: 8),

        Text(
          'Your password vault has been created successfully. You can now login with your master password.',
          style: Theme.of(context).textTheme.bodyMedium,
          textAlign: TextAlign.center,
        ),

        const SizedBox(height: 48),

        ElevatedButton(onPressed: _goBack, child: const Text('Go to Login')),
      ],
    );
  }
}
