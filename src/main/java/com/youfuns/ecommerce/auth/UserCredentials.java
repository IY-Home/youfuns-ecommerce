package com.youfuns.ecommerce.auth;

import com.youfuns.ecommerce.LoggerManager;
import com.youfuns.exceptions.IllegalFieldException;
import com.youfuns.paramtypes.Hash;
import com.youfuns.paramtypes.ParamType;
import com.youfuns.paramtypes.UuidFormat;
import com.youfuns.ecommerce.frontend.utils.ResultReturn;
import com.youfuns.utils.SimpleLogger;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public final class UserCredentials {
    private final UUID id;
    private final List<Hash> hashedUsernames;
    private Hash passwordHash;
    private boolean isLocked;
    private Instant lockoutExpiry;
    private final AtomicInteger failedAttempts = new AtomicInteger(0);
    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final int LOCKOUT_DURATION_SECONDS = 300; // 5 minutes

    public UserCredentials(UUID id, List<String> usernames, String password) {
        LoggerManager.quickLog(this, "Creating UserCredentials instance...");
        this.id = id;
        LoggerManager.quickLog(this, "Hashing usernames " + usernames + " and password...");
        this.hashedUsernames = new ArrayList<>(usernames.stream()
                .map(HashingService::hashUsername)
                .toList());
        ResultReturn passwordCheck = PasswordStrengthValidator.isPasswordValid(password);
        if (!passwordCheck.isSuccess()) throw new IllegalFieldException(passwordCheck.message(), ParamType.PASSWORD);
        this.passwordHash = HashingService.hashPassword(password);
        LoggerManager.quickLog(this, "Created UserCredentials instance with id " + UuidFormat.shortenUUID(id));
    }

    // ============= VALIDATION METHODS =============

    public ResultReturn validateLogin(String username, String password) {
        LoggerManager.quickLog(this, "Processing login for user " + username);

        if (isLocked) {
            if (Instant.now().isBefore(lockoutExpiry)) {
                LoggerManager.quickLog(this, "Login attempt on locked account: " +
                        UuidFormat.shortenUUID(id), SimpleLogger.Level.WARN);
                return new ResultReturn(ResultReturn.Result.FAILURE,
                        "Account is temporarily locked. Try again later.");
            } else {
                // Lockout expired, reset
                isLocked = false;
                failedAttempts.set(0);
            }
        }

        Hash hashedUsername = HashingService.hashUsername(username);
        boolean usernameExists = hashedUsernames.contains(hashedUsername);
        boolean passwordValid = HashingService.verifyPassword(password, this.passwordHash);

        boolean authenticated = usernameExists && passwordValid;

        if (authenticated) {
            // Reset failed attempts on success
            failedAttempts.set(0);
            LoggerManager.quickLog(this, "Successful login for user: " +
                    UuidFormat.shortenUUID(id));
            return new ResultReturn(ResultReturn.Result.SUCCESS, "Login successful.");
        } else {
            // Increment failed attempts
            int attempts = failedAttempts.incrementAndGet();
            LoggerManager.quickLog(this, "Failed login attempt " + attempts +
                    " for user: " + UuidFormat.shortenUUID(id), SimpleLogger.Level.WARN);

            // Lock account if too many failures
            if (attempts >= MAX_FAILED_ATTEMPTS) {
                lockAccount();
                return new ResultReturn(ResultReturn.Result.FAILURE,
                        "Account locked due to too many failed attempts. Try again in " +
                                LOCKOUT_DURATION_SECONDS + " seconds.");
            }

            // Return generic message
            return new ResultReturn(ResultReturn.Result.FAILURE,
                    "Invalid username or password.");
        }
    }

    private void lockAccount() {
        lockAccount(LOCKOUT_DURATION_SECONDS);
    }

    private void lockAccount(int seconds) {
        isLocked = true;
        lockoutExpiry = Instant.now().plusSeconds(seconds);
        LoggerManager.quickLog(this, "Account locked for user: " +
                UuidFormat.shortenUUID(id) + " for " + seconds + " seconds", SimpleLogger.Level.WARN);
    }

    public ResultReturn unlockAccount(RoleToken rt) {
        LoggerManager.quickLog(this, "Called unlock account for user " + UuidFormat.shortenUUID(id));
        PermissionChecker.checkPermission(rt, Permission.DISABLE_ANY_USER);
        isLocked = false;
        lockoutExpiry = null;
        failedAttempts.set(0);
        LoggerManager.quickLog(this, "Account unlocked by admin for user: " +
                UuidFormat.shortenUUID(id));
        return new ResultReturn(ResultReturn.Result.SUCCESS, "Account unlocked.");
    }

    public ResultReturn lockAccount(RoleToken rt, int seconds) {
        LoggerManager.quickLog(this, "Called lock account for user " + UuidFormat.shortenUUID(id));
        PermissionChecker.checkPermission(rt, Permission.DISABLE_ANY_USER);
        lockAccount(seconds);
        return new ResultReturn(ResultReturn.Result.SUCCESS, "Account locked by admin.");
    }

    // ============= PASSWORD MANAGEMENT =============

    public ResultReturn changePassword(RoleToken rt, String oldPassword, String newPassword) {
        LoggerManager.quickLog(this, "Called change password for user " + UuidFormat.shortenUUID(id));

        PermissionChecker.checkPermissionWithUser(rt, this.id, Permission.UPDATE_SELF_USER);

        // Verify old password
        if (!HashingService.verifyPassword(oldPassword, this.passwordHash)) {
            LoggerManager.quickLog(this, "Failed password change attempt - incorrect old password",
                    SimpleLogger.Level.WARN);
            return new ResultReturn(ResultReturn.Result.FAILURE, "Incorrect current password.");
        }

        // Update to new password
        this.passwordHash = HashingService.hashPassword(newPassword);
        LoggerManager.quickLog(this, "Password changed for user: " + UuidFormat.shortenUUID(id));
        return new ResultReturn(ResultReturn.Result.SUCCESS, "Password successfully changed.");
    }

    public ResultReturn resetPasswordAdmin(RoleToken rt, String newPassword) {
        LoggerManager.quickLog(this, "Called change password for user " + UuidFormat.shortenUUID(id) + " as admin");
        PermissionChecker.checkPermission(rt, Permission.MANAGE_ANY_USERS);
        this.passwordHash = HashingService.hashPassword(newPassword);
        LoggerManager.quickLog(this, "Password reset by admin for user: " +
                UuidFormat.shortenUUID(id));
        return new ResultReturn(ResultReturn.Result.SUCCESS, "Password successfully reset.");
    }

    // ============= USERNAME MANAGEMENT =============

    public ResultReturn addUsername(RoleToken rt, String username) {
        LoggerManager.quickLog(this, "Called add username for user " + UuidFormat.shortenUUID(id));
        PermissionChecker.checkPermissionWithUser(rt, this.id, Permission.UPDATE_SELF_USER);

        Hash hashedUsername = HashingService.hashUsername(username);

        // Check if already exists
        if (hashedUsernames.contains(hashedUsername)) {
            LoggerManager.quickLog(this, "Username already exists: " + username);
            return new ResultReturn(ResultReturn.Result.FAILURE, "Username already exists.");
        }
        this.hashedUsernames.add(hashedUsername);
        LoggerManager.quickLog(this, "Added username to UserCredentials instance with id " +
                UuidFormat.shortenUUID(id));
        return new ResultReturn(ResultReturn.Result.SUCCESS, "Username successfully added.");
    }

    public ResultReturn removeUsername(RoleToken rt, String username) {
        LoggerManager.quickLog(this, "Called remove username for user " + UuidFormat.shortenUUID(id));
        PermissionChecker.checkPermissionWithUser(rt, this.id, Permission.UPDATE_SELF_USER);

        Hash hashedUsername = HashingService.hashUsername(username);
        boolean removed = hashedUsernames.remove(hashedUsername);

        if (removed) {
            LoggerManager.quickLog(this, "Removed username from UserCredentials instance with id " +
                    UuidFormat.shortenUUID(id));
            return new ResultReturn(ResultReturn.Result.SUCCESS, "Username successfully removed.");
        } else {
            LoggerManager.quickLog(this, "Username could not be removed. It may not exist.");
            return new ResultReturn(ResultReturn.Result.FAILURE, "Username does not exist.");
        }
    }

    public ResultReturn addUsernameAdmin(RoleToken rt, String username) {
        LoggerManager.quickLog(this, "Called add username for user " + UuidFormat.shortenUUID(id) + " as admin");

        PermissionChecker.checkPermission(rt, Permission.MANAGE_ANY_USERS);

        Hash hashedUsername = HashingService.hashUsername(username);

        if (hashedUsernames.contains(hashedUsername)) {
            return new ResultReturn(ResultReturn.Result.FAILURE, "Username already exists.");
        }

        this.hashedUsernames.add(hashedUsername);
        LoggerManager.quickLog(this, "Added username by admin to UserCredentials instance with id " +
                UuidFormat.shortenUUID(id));
        return new ResultReturn(ResultReturn.Result.SUCCESS, "Username successfully added.");
    }

    public ResultReturn removeUsernameAdmin(RoleToken rt, String username) {
        LoggerManager.quickLog(this, "Called remove username for user " + UuidFormat.shortenUUID(id) + " as admin");
        PermissionChecker.checkPermission(rt, Permission.MANAGE_ANY_USERS);

        Hash hashedUsername = HashingService.hashUsername(username);
        boolean removed = hashedUsernames.remove(hashedUsername);

        if (removed) {
            LoggerManager.quickLog(this, "Removed username by admin from UserCredentials instance with id " +
                    UuidFormat.shortenUUID(id));
            return new ResultReturn(ResultReturn.Result.SUCCESS, "Username successfully removed.");
        } else {
            return new ResultReturn(ResultReturn.Result.FAILURE, "Username does not exist.");
        }
    }

    // ============= HELPER METHODS =============

    public boolean isLocked(RoleToken rt) {
        LoggerManager.quickLog(this, "Called isLocked for user " + UuidFormat.shortenUUID(id));
        if (isLocked && Instant.now().isAfter(lockoutExpiry)) {
            // Auto-unlock if lockout expired
            isLocked = false;
            lockoutExpiry = null;
            failedAttempts.set(0);
        }
        return isLocked;
    }

    public int getRemainingAttempts() {
        LoggerManager.quickLog(this, "Called getRemainingAttempts for user " + UuidFormat.shortenUUID(id));
        return Math.max(0, MAX_FAILED_ATTEMPTS - failedAttempts.get());
    }

    // ============= FOR REPOSITORIES ONLY =============

    boolean isUsernameMatches(Hash username) {
        return hashedUsernames.contains(username);
    }

    List<Hash> getUsernamesInternal() {
        return hashedUsernames;
    }

}