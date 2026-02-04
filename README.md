# Password Manager ( KYOWMI-X )

Securely store and manage your passwords locally on your Android device.

## Features

- 🔐 **AES-256-GCM Encryption** - Military-grade encryption with PBKDF2 key derivation
- 🔑 **Master Password** - Single password to unlock your vault
- 👆 **Fingerprint Unlock** - Optional biometric authentication for quick access
- 🎲 **Password Generator** - Generate secure 14-character passwords with entropy indicator
- 🌙 **Dark Theme** - Easy on the eyes, always
- 💾 **Local Storage** - All data stays on your device in shared storage

## Getting Started

### Requirements

- Android 8.0+ (API 26+)
- Flutter SDK 3.10+

### Build & Run

```bash
# Get dependencies
flutter pub get

# Run on connected device/emulator
flutter run

# Build release APK
flutter build apk --release
```

### Output APK

After building, the release APK will be at:
```
build/app/outputs/flutter-apk/app-release.apk
```

## Project Structure

```
lib/
├── main.dart                    # App entry point
├── models/
│   └── credential.dart          # Password entry model
├── services/
│   ├── crypto_service.dart      # AES-256-GCM encryption
│   ├── storage_service.dart     # .privVaultv2 file operations
│   ├── password_generator.dart  # Secure password generation
│   └── biometric_service.dart   # Fingerprint authentication
├── screens/
│   ├── login_screen.dart        # Master password login
│   ├── create_vault_screen.dart # New vault creation
│   └── home_screen.dart         # Credentials list
└── theme/
    └── app_theme.dart           # Dark theme configuration
```

## Security

- **Encryption**: AES-256-GCM with 96-bit IV
- **Key Derivation**: PBKDF2-HMAC-SHA256 with 40,000 iterations
- **Password Verification**: SHA-256 hash stored at file header
- **Storage**: Files stored in `Documents/PasswordManager/` with `.privVaultv2` extension

## License

This project is licensed under the GNU General Public License v3 - see the LICENSE file for details.

## Author

Daniel D  
[@Github](https://github.com/Daniel446f6c/)
