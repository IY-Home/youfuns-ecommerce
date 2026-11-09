package com.youfuns.auth.login;

import java.util.function.BiFunction;
import java.util.function.Function;

public final class HashingService {
    private Function<String, String> passwordHasher = Argon2Hasher::argon2Hash;
    private BiFunction<String, String, Boolean> passwordValidator = Argon2Hasher::verifyArgon2Hash;

    public String hashPassword(String password) {
        return passwordHasher.apply(password);
    }
    public boolean validatePassword(String password, String hashedPassword) {
        return passwordValidator.apply(password, hashedPassword);
    }
    public void set(Function<String, String> passwordHasher, BiFunction<String, String, Boolean> passwordValidator) {
        this.passwordHasher = passwordHasher;
        this.passwordValidator = passwordValidator;
    }
}