package com.danield.protector;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * The {@code SHA} class provides secure hashing using SHA-256/384/512 algorithms.
 * @author Daniel D
 * @version 0.1
 */
public class SHA {

    private static final String SHA256_ALGORITHM = "SHA-256";
    private static final String SHA384_ALGORITHM = "SHA-384";
    private static final String SHA512_ALGORITHM = "SHA-512";

    private SHA() {} // we don't want this class to be instantiated.

    /**
     * Securely perfom hashing using the SHA-256 algorithm.
     * @param data : the data (to hash)
     * @return the resulting hash. <b>OR {@code null} on failure.</b>
     */
    public static byte[] SHA256(byte[] data) {

        MessageDigest messageDigest;
        try {
            messageDigest = MessageDigest.getInstance(SHA256_ALGORITHM);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
        messageDigest.update(data);
        return messageDigest.digest();

    }

    /**
     * Securely perfom hashing using the SHA-384 algorithm.
     * @param data : the data (to hash)
     * @return the resulting hash. <b>OR {@code null} on failure.</b>
     */
    public static byte[] SHA384(byte[] data) {
        
        MessageDigest messageDigest;
        try {
            messageDigest = MessageDigest.getInstance(SHA384_ALGORITHM);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
        messageDigest.update(data);
        return messageDigest.digest();

    }

    /**
     * Securely perfom hashing using the SHA-512 algorithm.
     * @param data : the data (to hash)
     * @return the resulting hash. <b>OR {@code null} on failure.</b>
     */
    public static byte[] SHA512(byte[] data) {

        MessageDigest messageDigest;
        try {
            messageDigest = MessageDigest.getInstance(SHA512_ALGORITHM);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
        messageDigest.update(data);
        return messageDigest.digest();
        
    }

}
