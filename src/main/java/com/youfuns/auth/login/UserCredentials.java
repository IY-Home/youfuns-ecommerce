package com.youfuns.auth.login;

import com.youfuns.auth.AuthManager;
import com.youfuns.auth.rbac.DefaultPermissions;
import com.youfuns.auth.rbac.ResultReturn;
import com.youfuns.auth.rbac.RoleToken;
import com.youfuns.logger.LoggerManager;
import com.youfuns.logger.SimpleLogger;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
import java.util.function.Function;

public final class UserCredentials {
    private final UUID id;
    private final Set<String> usernames;
    private String passwordHash;
    private boolean passwordLocked;
    private boolean adminLocked;
    private Instant passwordLockoutExpiry;
    private Instant adminLockoutExpiry;
    private final AtomicInteger failedAttempts = new AtomicInteger(0);

    private static int MAX_FAILED_ATTEMPTS = 5;
    private static int LOCKOUT_DURATION_SECONDS = 300; // 5 minutes

    private static Function<String, String> passwordHasher = HashingService::argon2Hash;
    private static BiFunction<String, String, Boolean> passwordValidator = HashingService::verifyArgon2Hash;
    private static boolean validatePasswords = true;

    public static final ResultReturn genericLoginFailure = new ResultReturn(ResultReturn.Result.FAILURE, "Login failed");

    private DefaultPermissions<?> defaultPermissions;

    public UserCredentials(
            UUID id, Set<String> usernames, String passwordHash,
            boolean passwordLocked, boolean adminLocked, Instant passwordLockoutExpiry, Instant adminLockoutExpiry,
            int failedAttempts,
            DefaultPermissions<?> defaultPermissions) {
        this.id = id;
        this.usernames = usernames;
        this.passwordHash = passwordHash;
        this.passwordLocked = passwordLocked;
        this.adminLocked = adminLocked;
        this.passwordLockoutExpiry = passwordLockoutExpiry;
        this.adminLockoutExpiry = adminLockoutExpiry;
        this.defaultPermissions = defaultPermissions;
        this.failedAttempts.set(failedAttempts);
    }

    public static void setPasswordHasher(Function<String, String> passwordHasher, BiFunction<String, String, Boolean> passwordValidator) {
        validateCaller();
        UserCredentials.passwordHasher = passwordHasher;
        UserCredentials.passwordValidator = passwordValidator;
    }

    public static void enablePasswordValidation(boolean enable) {
        validateCaller();
        validatePasswords = enable;
    }

    public static void setPasswordLockoutPolicy(int maxAttempts, int lockoutSec) {
        validateCaller();
        MAX_FAILED_ATTEMPTS = maxAttempts;
        LOCKOUT_DURATION_SECONDS = lockoutSec;
    }

    public static void setSessionTokenService(Function<String, String> generateToken, Function<String, String> extractSubject) {
        validateCaller();
        SessionTokenService.generateToken = generateToken;
        SessionTokenService.extractSubject = extractSubject;
    }

    private static void validateCaller() {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        if (stackTrace.length >= 3) {
            String callerClassName = stackTrace[2].getClassName();
            if (!callerClassName.startsWith(AuthManager.class.getPackage().getName()))
                throw new IllegalCallerException("This method cannot be called from outside package");
        } else {
            throw new IllegalCallerException("This method cannot be called from outside package");
        }
    }

    public Set<String> getUsernames() {
        return Set.copyOf(usernames);
    }

    public UserCredentials(UUID id, Set<String> usernames, String password, DefaultPermissions<?> defaultPermissions) {
        LoggerManager.quickLog(this, "Creating UserCredentials instance...");
        this.id = id;
        LoggerManager.quickLog(this, "Hashing usernames and password...");
        this.usernames = new HashSet<>(Set.copyOf(usernames));
        if (validatePasswords) {
            ResultReturn passwordCheck = PasswordStrengthValidator.validatePasswordWithMessage(password);
            if (!passwordCheck.isSuccess()) throw new IllegalArgumentException(passwordCheck.message());
        }
        this.passwordHash = passwordHasher.apply(password);
        this.defaultPermissions = defaultPermissions;
        LoggerManager.quickLog(this, "Created UserCredentials instance");
    }

    public record LoginResult(ResultReturn resultReturn, String jwtToken) {
        public boolean isSuccess() {
            return resultReturn.isSuccess();
        }
        @Override
        public String toString() {
            return "LoginResult [Success=" + this.isSuccess() + ", result=" + this.resultReturn().result().name() + "(" + this.resultReturn().message() + ")" + ", JWT=" + String.valueOf(this.jwtToken()) + "]";
        }

    }

    // ============= VALIDATION METHODS =============
    public LoginResult login(String username, String password) {
        ResultReturn loginResult = validateLogin(username, password);
        String jwt = null;
        if (loginResult.isSuccess()) {
            jwt = SessionTokenService.generateToken(String.valueOf(this.id));
        }
        return new LoginResult(loginResult, jwt);
    }

    public boolean validateJwt(String jwt) {
        return SessionTokenService.validateToken(jwt);
    }

    private ResultReturn validateLogin(String username, String password) {
        LoggerManager.quickLog(this, "Processing login for user " + username);

        if (passwordLocked) {
            if (Instant.now().isBefore(passwordLockoutExpiry)) {
                LoggerManager.quickLog(this, "Login attempt on locked account", SimpleLogger.Level.WARN);
                return new ResultReturn(ResultReturn.Result.FAILURE, "Account is temporarily locked. Try again later.");
            } else {
                // Lockout expired, reset
                passwordLocked = false;
                failedAttempts.set(0);
            }
        }

        if (adminLocked) {
            if (Instant.now().isBefore(adminLockoutExpiry)) {
                LoggerManager.quickLog(this, "Login attempt on locked account", SimpleLogger.Level.WARN);
                return new ResultReturn(ResultReturn.Result.FAILURE, "Account is locked by administrator.");
            } else {
                // Lockout expired, reset
                adminLocked = false;
            }
        }

        boolean usernameExists = usernames.contains(username);
        boolean passwordValid = passwordValidator.apply(password, this.passwordHash);

        boolean authenticated = usernameExists && passwordValid;

        if (authenticated) {
            // Reset failed attempts on success
            failedAttempts.set(0);
            passwordLocked = false;
            adminLocked = false;
            LoggerManager.quickLog(this, "Successful login for user");
            return new ResultReturn(ResultReturn.Result.SUCCESS, "Login successful");
        } else {
            int attempts = failedAttempts.get();
            // Increment failed attempts if username is valid
            if (usernameExists) {
                attempts = failedAttempts.incrementAndGet();
                LoggerManager.quickLog(this, "Failed login attempt " + attempts +
                        " for user", SimpleLogger.Level.WARN);
            }
            // Lock account if too many failures
            if (attempts >= MAX_FAILED_ATTEMPTS) {
                lockAccountForPassword();
                return new ResultReturn(ResultReturn.Result.FAILURE, "Account is temporarily locked");
            }

            // Return generic message
            return genericLoginFailure;
        }
    }

    private void lockAccountForPassword() {
        passwordLocked = true;
        passwordLockoutExpiry = Instant.now().plusSeconds(LOCKOUT_DURATION_SECONDS);
        LoggerManager.quickLog(this, "Account locked for user for " + LOCKOUT_DURATION_SECONDS + " seconds", SimpleLogger.Level.WARN);

    }

    private void lockAccount(int seconds) {
        adminLocked = true;
        adminLockoutExpiry = Instant.now().plusSeconds(seconds);
        LoggerManager.quickLog(this, "Account locked for user for " + seconds + " seconds", SimpleLogger.Level.WARN);
    }

    public void unlockAccount(RoleToken rt) {
        LoggerManager.quickLog(this, "Called unlock account for user");
        defaultPermissions.checkManageUsers(rt);
        passwordLocked = false;
        adminLocked = false;
        passwordLockoutExpiry = null;
        adminLockoutExpiry = null;
        failedAttempts.set(0);
        LoggerManager.quickLog(this, "Account unlocked by admin for user", SimpleLogger.Level.INFO);
    }

    public void lockAccount(RoleToken rt, int seconds) {
        LoggerManager.quickLog(this, "Called lock account for user");
        defaultPermissions.checkManageUsers(rt);
        lockAccount(seconds);
    }

    // ============= PASSWORD MANAGEMENT =============

    public ResultReturn changePassword(RoleToken rt, String oldPassword, String newPassword) {
        LoggerManager.quickLog(this, "Called change password for user");

        defaultPermissions.checkManageSelf(rt, this.id);

        // Verify old password
        if (!passwordValidator.apply(oldPassword, this.passwordHash)) {
            LoggerManager.quickLog(this, "Failed password change attempt - incorrect old password",
                    SimpleLogger.Level.WARN);
            return new ResultReturn(ResultReturn.Result.FAILURE, "Incorrect current password.");
        }

        // Update to new password
        this.passwordHash = passwordHasher.apply(newPassword);
        LoggerManager.quickLog(this, "Password changed for user");
        return new ResultReturn(ResultReturn.Result.SUCCESS, "Password successfully changed.");
    }

    public void resetPasswordAdmin(RoleToken rt, String newPassword) {
        LoggerManager.quickLog(this, "Called change password for user by admin");
        defaultPermissions.checkManageUsers(rt);
        this.passwordHash = passwordHasher.apply(newPassword);
        LoggerManager.quickLog(this, "Password reset by admin for user");
    }

    // ============= USERNAME MANAGEMENT =============

    public ResultReturn addUsername(RoleToken rt, String username) {
        LoggerManager.quickLog(this, "Called add username for user");
        defaultPermissions.checkManageSelf(rt, this.id);

        // Check if already exists
        if (usernames.contains(username)) {
            LoggerManager.quickLog(this, "Username already exists: " + username);
            return new ResultReturn(ResultReturn.Result.FAILURE, "Username already exists.");
        }
        this.usernames.add(username);
        LoggerManager.quickLog(this, "Added username to UserCredentials instance");
        return new ResultReturn(ResultReturn.Result.SUCCESS, "Username successfully added.");
    }

    public ResultReturn removeUsername(RoleToken rt, String username) {
        LoggerManager.quickLog(this, "Called remove username for user");
        defaultPermissions.checkManageSelf(rt, this.id);

        boolean removed = usernames.remove(username);

        if (removed) {
            LoggerManager.quickLog(this, "Removed username from UserCredentials instance");
            return new ResultReturn(ResultReturn.Result.SUCCESS, "Username successfully removed.");
        } else {
            LoggerManager.quickLog(this, "Username could not be removed. It may not exist.");
            return new ResultReturn(ResultReturn.Result.FAILURE, "Username does not exist.");
        }
    }

    public ResultReturn addUsernameAdmin(RoleToken rt, String username) {
        LoggerManager.quickLog(this, "Called add username for user by admin");

        defaultPermissions.checkManageUsers(rt);

        if (usernames.contains(username)) {
            return new ResultReturn(ResultReturn.Result.FAILURE, "Username already exists.");
        }

        this.usernames.add(username);
        LoggerManager.quickLog(this, "Added username by admin to UserCredentials instance");
        return new ResultReturn(ResultReturn.Result.SUCCESS, "Username successfully added.");
    }

    public ResultReturn removeUsernameAdmin(RoleToken rt, String username) {
        LoggerManager.quickLog(this, "Called remove username for user by admin");
        defaultPermissions.checkManageUsers(rt);

        boolean removed = usernames.remove(username);

        if (removed) {
            LoggerManager.quickLog(this, "Removed username by admin from UserCredentials instance");
            return new ResultReturn(ResultReturn.Result.SUCCESS, "Username successfully removed.");
        } else {
            return new ResultReturn(ResultReturn.Result.FAILURE, "Username does not exist.");
        }
    }

    // ============= HELPER METHODS =============

    public boolean isLocked(RoleToken rt) {
        LoggerManager.quickLog(this, "Called passwordLocked for user");
        defaultPermissions.checkManageSelf(rt, this.id);
        if (adminLocked && Instant.now().isAfter(adminLockoutExpiry)) {
            // Auto-unlock if lockout expired
            adminLocked = false;
            adminLockoutExpiry = null;
            failedAttempts.set(0);
        }
        return adminLocked;
    }

    public boolean isLockedAdmin(RoleToken rt) {
        LoggerManager.quickLog(this, "Called passwordLocked for user by admin");
        defaultPermissions.checkManageUsers(rt);
        if (adminLocked && Instant.now().isAfter(adminLockoutExpiry)) {
            // Auto-unlock if lockout expired
            adminLocked = false;
            adminLockoutExpiry = null;
            failedAttempts.set(0);
        }
        return adminLocked;
    }

}