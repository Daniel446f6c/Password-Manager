import 'dart:math';

/// Generates secure random passwords and calculates entropy.
class PasswordGenerator {
  static const String _lowerLetters = 'abcdefghijklmnopqrstuvwxyz';
  static const String _upperLetters = 'ABCDEFGHIJKLMNOPQRSTUVWXYZ';
  static const String _digits = '0123456789';
  static const String _symbols = r'.,:;-_+*~#^!$%&?=<>|[](){}';

  static const int _defaultLength = 14;

  final Random _random = Random.secure();

  /// Generate a secure password of fixed 14 characters.
  /// Always includes: uppercase, lowercase, digit, symbol.
  String generate() {
    return _generatePassword(_defaultLength);
  }

  String _generatePassword(int length) {
    final allChars = _lowerLetters + _upperLetters + _digits + _symbols;
    String password;

    // Keep generating until we have a valid password
    do {
      final chars = List<String>.generate(
        length,
        (_) => allChars[_random.nextInt(allChars.length)],
      );
      password = chars.join();
    } while (!_isValid(password));

    return password;
  }

  /// Validate that password contains at least one of each required character type
  bool _isValid(String password) {
    bool hasLower = false;
    bool hasUpper = false;
    bool hasDigit = false;
    bool hasSymbol = false;

    for (final char in password.split('')) {
      if (_lowerLetters.contains(char)) {
        hasLower = true;
      } else if (_upperLetters.contains(char)) {
        hasUpper = true;
      } else if (_digits.contains(char)) {
        hasDigit = true;
      } else if (_symbols.contains(char)) {
        hasSymbol = true;
      }
    }

    return hasLower && hasUpper && hasDigit && hasSymbol;
  }

  /// Calculate password entropy in bits.
  /// Entropy = length * log2(charsetSize)
  int calculateEntropy(String password) {
    if (password.isEmpty) return 0;

    int charsetSize = 0;
    bool hasLower = false;
    bool hasUpper = false;
    bool hasDigit = false;
    bool hasSymbol = false;

    for (final char in password.split('')) {
      if (!hasLower && _lowerLetters.contains(char)) {
        hasLower = true;
        charsetSize += _lowerLetters.length; // 26
      } else if (!hasUpper && _upperLetters.contains(char)) {
        hasUpper = true;
        charsetSize += _upperLetters.length; // 26
      } else if (!hasDigit && _digits.contains(char)) {
        hasDigit = true;
        charsetSize += _digits.length; // 10
      } else if (!hasSymbol && _symbols.contains(char)) {
        hasSymbol = true;
        charsetSize += _symbols.length; // 27
      }
    }

    if (charsetSize == 0) return 0;

    // Entropy = length * log2(charsetSize)
    // log2(x) = log(x) / log(2)
    final entropy = password.length * (log(charsetSize) / log(2));
    return entropy.round();
  }

  /// Get a strength label based on entropy
  String getStrengthLabel(int entropy) {
    if (entropy < 28) return 'Very Weak';
    if (entropy < 36) return 'Weak';
    if (entropy < 60) return 'Moderate';
    if (entropy < 80) return 'Strong';
    return 'Very Strong';
  }
}
