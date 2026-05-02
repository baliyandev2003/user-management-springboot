package com.example.baliyan.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

public class SHA512Util {

    // Generate a random salt
    public static String generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[16]; // 16 bytes = 128 bits
        random.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    // Hash password with salt
    public static String hashPassword(String password, String salt) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-512");
            // Combine password and salt
            String combined = password + salt;
            byte[] hashedBytes = md.digest(combined.getBytes());
            return Base64.getEncoder().encodeToString(hashedBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-512 algorithm not found", e);
        }
    }

    // Verify password with salt
    public static boolean verifyPassword(String inputPassword, String storedHash, String salt) {
        if (inputPassword == null || storedHash == null || salt == null) {
            return false;
        }
        String hashedInput = hashPassword(inputPassword, salt);
        return hashedInput.equals(storedHash);
    }

    // Legacy method for backward compatibility (remove after migration)
    public static String hashPassword(String password) {
        return hashPassword(password, ""); // Empty salt for legacy passwords
    }

    // Legacy verification method (remove after migration)
    public static boolean verifyPassword(String inputPassword, String storedHash) {
        return verifyPassword(inputPassword, storedHash, ""); // Empty salt for legacy passwords
    }
}