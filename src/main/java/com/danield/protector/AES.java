package com.danield.protector;

import java.nio.ByteBuffer;
import java.security.InvalidParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.*;
import javax.crypto.spec.*;

/**
 * The {@code AES} class provides secure and easy-to-use AES en- and decryption in GCM mode with a 256 bit key size (optional: derived from a given password).
 * @author Daniel D
 * @version 0.2
 */
public class AES { //TODO add support for 512 bit key size.

	private static final String SECRET_KEY_ALGORITHM = "PBKDF2WithHmacSHA256";
	/** Transformation String ( Algorithm / Mode / Padding_Scheme ) */
	private static final String TRANSFORMATION_STRING = "AES/GCM/NoPadding";
	private static final SecureRandom SECURE_RANDOM = new SecureRandom();
	private static final KeyGenerator KEYGEN = initKeyGen();
	private static final String KEY_ALGORITHM = "AES";
	private static final int TAG_BIT_LENGTH = 128;
	private static final int KEY_BIT_LENGTH = 256;
	private static final int IV_BYTE_LENGTH = 96;

	private AES() {} // we don't want this class to be instantiated.

	/**
	 * Initialize the AES 256bit Key Generator.
	 * @return a {@code KeyGenerator} object that generates secret keys
     * for the specified algorithm and keysize <b>or null on failure.</b>
	 */
	private static KeyGenerator initKeyGen() {

		try {
			KeyGenerator keyGen = KeyGenerator.getInstance(KEY_ALGORITHM);
			keyGen.init(KEY_BIT_LENGTH);
			return keyGen;
		}
		catch (NoSuchAlgorithmException e) {
			System.err.printf("ERROR: Algorithm \"%s\" not supported!\n", KEY_ALGORITHM);
			e.printStackTrace();
		}
		catch (InvalidParameterException e) {
			System.err.printf("ERROR: Keysize \"%s\" is wrong or not supported!\n", KEY_BIT_LENGTH);
			e.printStackTrace();
		}
		return null;

	}

	/**
	 * Generates a initialization vector.
	 * @return the new iv
	 */
	private static byte[] generateIV() {

		byte[] iv = new byte[IV_BYTE_LENGTH];
		SECURE_RANDOM.nextBytes(iv);
		return iv;
		
	}

	/**
	 * Password-based Key Generator for Symmetric Encryption using the "PBKDF2WithHmacSHA256" algorithm.
     * @param password : the password.
     * @param salt : the salt.
     * @param iterationCount : the iteration count.
     * @param keySize : the to-be-derived key length.
	 * @return ENCODED_KEY
	 */
    private static byte[] PBEKeyGen(char[] password, byte[] salt, int iterationCount, int keySize)
    throws NullPointerException, NoSuchAlgorithmException, InvalidKeySpecException, IllegalArgumentException {

        // A user-chosen password that can be used with password-based encryption (PBE).
		// The password can be viewed as some kind of raw key material, from which the encryption mechanism that uses it derives a cryptographic key.
        PBEKeySpec keySpec = new PBEKeySpec(password, salt, iterationCount, keySize);

        SecretKeyFactory pbkdfKeyFactory = SecretKeyFactory.getInstance(SECRET_KEY_ALGORITHM);
        
        SecretKey key = pbkdfKeyFactory.generateSecret(keySpec);

        // Delete sensitive information
        keySpec.clearPassword();

        return key.getEncoded();

    }

	/**
	 * Encrypt data using a generated 256bit aes key.
	 * @param plainData : the data
	 * @param additionalAuthenticationData : is optional. if you don't want to use it pass {@code null} as argument.
	 * @return a {@code HashMap} containing 2 k/v pairs: "KEY" : the key , "DATA" : the data
	 * @throws Exception
	 */
	public static HashMap<String, byte[]> encrypt(byte[] plainData, byte[] additionalAuthenticationData) throws Exception {
		 
		// Create Secret Key
		SecretKey secretKey = KEYGEN.generateKey();
		
		// Create Initialization Vector
		byte[] iv = generateIV();

		// Initialize GCM Parameters
		GCMParameterSpec gcmParamSpec = new GCMParameterSpec(TAG_BIT_LENGTH, iv);

		// Create Cipher Instance
		Cipher cipher = Cipher.getInstance(TRANSFORMATION_STRING);

		// Initialize Cipher for ENCRYPT_MODE
		cipher.init(Cipher.ENCRYPT_MODE, secretKey, gcmParamSpec);
		
		// Associated Data
		if (additionalAuthenticationData != null) {
			cipher.updateAAD(additionalAuthenticationData);
		}

		// Perform Encryption
		byte[] cipherData = cipher.doFinal(plainData);

		// Concatenate "iv" and "cipherData" into one byte array
		ByteBuffer byteBuffer = ByteBuffer.allocate(iv.length + cipherData.length);
		byteBuffer.put(iv);
		byteBuffer.put(cipherData);
		byte[] cipherMessage = byteBuffer.array();

		// Delete sensitive information
		Arrays.fill(iv, (byte)0);

		return new HashMap<String, byte[]>(
			Map.ofEntries(
				Map.entry("KEY", secretKey.getEncoded()),
				Map.entry("DATA", cipherMessage)
			)
		);

	}

	/**
	 * Decrypt data using a 256bit aes key.
	 * @param cipherData : the encrypted data.
	 * @param key : the key which the data was encrypted with.
	 * @param additionalAuthenticationData : is optional. if you don't want to use it pass {@code null} as argument.
	 * @return the decrypted data
	 * @throws Exception
	 */
	public static byte[] decrypt(byte[] cipherData, byte[] key, byte[] additionalAuthenticationData) throws Exception {

		// Create secret key
		SecretKey secretKey = new SecretKeySpec(key, KEY_ALGORITHM);

		// Initialize GCM Parameters
		GCMParameterSpec gcmParamSpec = new GCMParameterSpec(TAG_BIT_LENGTH, cipherData, 0, IV_BYTE_LENGTH);

		// Create Cipher Instance
		Cipher cipher = Cipher.getInstance(TRANSFORMATION_STRING);
		
        // Initialize Cipher for DECRYPT_MODE
        cipher.init(Cipher.DECRYPT_MODE, secretKey, gcmParamSpec);
        
		// Associated Data
		if (additionalAuthenticationData != null) {
			cipher.updateAAD(additionalAuthenticationData);
		}

        // Perform Decryption
		byte[] plainData = cipher.doFinal(cipherData, IV_BYTE_LENGTH, cipherData.length - IV_BYTE_LENGTH);
		
		// Delete sensitive information
		Arrays.fill(key, (byte)0);

		return plainData;

	}

	/**
	 * Encrypt data using a specific password.
	 * @param plainData : the data.
	 * @param password : the password which the data should be encrypted with.
	 * @param additionalAuthenticationData : is optional. if you don't want to use it pass {@code null} as argument.
	 * @return the encrypted data
	 * @throws Exception
	 */
	public static byte[] encrypt(byte[] plainData, String password, byte[] additionalAuthenticationData) throws Exception {

		// Create Password-based Key
		byte[] key = PBEKeyGen(password.toCharArray(), password.getBytes(), 40000, KEY_BIT_LENGTH);

		// Create Secret Key
		SecretKey secretKey = new SecretKeySpec(key, KEY_ALGORITHM);
		
		// Create Initialization Vector
		byte[] iv = generateIV();

		// Initialize GCM Parameters
		GCMParameterSpec gcmParamSpec = new GCMParameterSpec(TAG_BIT_LENGTH, iv);

		// Create Cipher Instance
		Cipher cipher = Cipher.getInstance(TRANSFORMATION_STRING);

		// Initialize Cipher for ENCRYPT_MODE
		cipher.init(Cipher.ENCRYPT_MODE, secretKey, gcmParamSpec);
		
		// Associated Data
		if (additionalAuthenticationData != null) {
			cipher.updateAAD(additionalAuthenticationData);
		}

		// Perform Encryption
		byte[] cipherData = cipher.doFinal(plainData);

		// Concatenate "iv" and "cipherData" into one byte array
		ByteBuffer byteBuffer = ByteBuffer.allocate(iv.length + cipherData.length);
		byteBuffer.put(iv);
		byteBuffer.put(cipherData);
		byte[] cipherMessage = byteBuffer.array();

		// Delete sensitive information
		Arrays.fill(key, (byte)0);
		Arrays.fill(iv, (byte)0);

		return cipherMessage;

	}

	/**
	 * Decrypt data using a specific password.
	 * @param cipherData : the encrypted data.
	 * @param password : the password which the data was encrypted with.
	 * @param additionalAuthenticationData : is optional. if you don't want to use it pass {@code null} as argument.
	 * @return the decrypted data
	 * @throws Exception
	 */
	public static byte[] decrypt(byte[] cipherData, String password, byte[] additionalAuthenticationData) throws Exception {

		// Create Password-based Key
		byte[] key = PBEKeyGen(password.toCharArray(), password.getBytes(), 40000, KEY_BIT_LENGTH);

		// Create secret key
		SecretKey secretKey = new SecretKeySpec(key, KEY_ALGORITHM);

		// Initialize GCM Parameters
		GCMParameterSpec gcmParamSpec = new GCMParameterSpec(TAG_BIT_LENGTH, cipherData, 0, IV_BYTE_LENGTH);

		// Create Cipher Instance
		Cipher cipher = Cipher.getInstance(TRANSFORMATION_STRING);
		
        // Initialize Cipher for DECRYPT_MODE
        cipher.init(Cipher.DECRYPT_MODE, secretKey, gcmParamSpec);
        
		// Associated Data
		if (additionalAuthenticationData != null) {
			cipher.updateAAD(additionalAuthenticationData);
		}

        // Perform Decryption
		byte[] plainData = cipher.doFinal(cipherData, IV_BYTE_LENGTH, cipherData.length - IV_BYTE_LENGTH);

		// Delete sensitive information
		Arrays.fill(key, (byte)0);

		return plainData;

	}

}
