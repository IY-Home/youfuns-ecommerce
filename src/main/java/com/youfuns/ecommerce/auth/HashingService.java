package com.youfuns.ecommerce.auth;

import com.password4j.Password;
import com.password4j.Argon2Function;
import com.password4j.types.Argon2;

import com.youfuns.ecommerce.LoggerManager;
import com.youfuns.paramtypes.Hash;
import com.youfuns.utils.SimpleLogger;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class HashingService {
    private static final Argon2Function ARGON2 = Argon2Function.getInstance(65536, 3, 4, 32, Argon2.ID);

    // ===== PASSWORD HASHING (Argon2 - salted, non-deterministic) =====

    public static Hash hashPassword(String plainPassword) {
        LoggerManager.quickLog(HashingService.class, "Password hash operation with argon2 started.");
        com.password4j.Hash hash = Password.hash(plainPassword)
                .addRandomSalt()
                .with(ARGON2);
        LoggerManager.quickLog(HashingService.class, "Password hash operation completed.");
        return new Hash(hash.getResult());
    }

    public static boolean verifyPassword(String plainPassword, Hash storedHash) {
        LoggerManager.quickLog(HashingService.class, "Verifying password against hash.");
        boolean matches = Password.check(plainPassword, storedHash.hash()).with(ARGON2);
        LoggerManager.quickLog(HashingService.class, "Password verification complete. Result: " + matches);
        return matches;
    }

    // ===== USERNAME HASHING (SHA-256 - deterministic, no salt) =====

    public static Hash hashUsername(String username) {
        LoggerManager.quickLog(HashingService.class, "Username hash operation with SHA-256 started.");
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(username.getBytes(StandardCharsets.UTF_8));
            String hash = Base64.getEncoder().encodeToString(hashBytes);
            LoggerManager.quickLog(HashingService.class, "Username hash operation completed.");
            return new Hash(hash);
        } catch (NoSuchAlgorithmException e) {
            LoggerManager.quickLog(HashingService.class, "SHA-256 algorithm not available!", SimpleLogger.Level.ERROR);
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }

    public static boolean verifyUsername(String plainUsername, Hash storedHash) {
        LoggerManager.quickLog(HashingService.class, "Verifying username against hash.");
        Hash computedHash = hashUsername(plainUsername);
        boolean matches = computedHash.equals(storedHash);
        LoggerManager.quickLog(HashingService.class, "Username verification complete. Result: " + matches);
        return matches;
    }
}