package com.youfuns.auth.login;

import com.youfuns.auth.rbac.ResultReturn;

import java.io.Serializable;

public record UserLoginConfig(
        int maxFailedAttempts,
        int lockoutDurationSeconds,
        boolean validatePasswords,
        ResultReturn genericLoginFailure
) implements Serializable {

    public static final int DEFAULT_MAX_FAILED_ATTEMPTS = 5;
    public static final int DEFAULT_LOCKOUT_DURATION_SECONDS = 300;
    public static final ResultReturn DEFAULT_GENERIC_LOGIN_FAILURE =
            new ResultReturn(ResultReturn.Result.FAILURE, "Login failed");

    public UserLoginConfig {
        if (maxFailedAttempts <= 0)
            throw new IllegalArgumentException("maxFailedAttempts must be > 0");
        if (lockoutDurationSeconds < 0)
            throw new IllegalArgumentException("lockoutDurationSeconds must be >= 0");
    }

    public static UserLoginConfig defaults() {
        return new UserLoginConfig(
                DEFAULT_MAX_FAILED_ATTEMPTS,
                DEFAULT_LOCKOUT_DURATION_SECONDS,
                true,
                DEFAULT_GENERIC_LOGIN_FAILURE);
    }

    public UserLoginConfig withMaxFailedAttempts(int v) {
        return new UserLoginConfig(v, lockoutDurationSeconds, validatePasswords, genericLoginFailure);
    }
    public UserLoginConfig withLockoutDurationSeconds(int v) {
        return new UserLoginConfig(maxFailedAttempts, v, validatePasswords, genericLoginFailure);
    }
    public UserLoginConfig withValidatePasswords(boolean v) {
        return new UserLoginConfig(maxFailedAttempts, lockoutDurationSeconds, v, genericLoginFailure);
    }
    public UserLoginConfig withGenericLoginFailure(ResultReturn v) {
        return new UserLoginConfig(maxFailedAttempts, lockoutDurationSeconds, validatePasswords, v);
    }
}