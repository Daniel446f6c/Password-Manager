import 'package:flutter_test/flutter_test.dart';
import 'package:kyowmi_x/services/crypto_service.dart';

void main() {
  late CryptoService cryptoService;

  setUp(() {
    cryptoService = CryptoService();
  });

  group('CryptoService Hardened Tests', () {
    const password = 'SuperSecurePassword123!';
    const plaintext = 'This is a secret message.';

    test('Encryption and Decryption should return original text', () {
      // 1. Encrypt
      final ciphertext = cryptoService.encrypt(plaintext, password);

      // 2. Ensure ciphertext is longer than salt + IV
      expect(ciphertext.length, greaterThan(16 + 12));

      // 3. Decrypt
      final decryptedText = cryptoService.decrypt(ciphertext, password);

      // 4. Assert
      expect(decryptedText, equals(plaintext));
    });

    test('Decryption with wrong password should fail', () {
      final ciphertext = cryptoService.encrypt(plaintext, password);

      // Attempting to decrypt with a different password should throw an exception
      // because the GCM Tag verification will fail.
      expect(
        () => cryptoService.decrypt(ciphertext, 'WrongPassword'),
        throwsA(isA<Exception>()),
      );
    });

    test('Password hashing and verification', () {
      final hash = cryptoService.hashPassword(password);

      expect(cryptoService.verifyPassword(password, hash), isTrue);
      expect(cryptoService.verifyPassword('wrong_pass', hash), isFalse);
    });
  });
}
