package com.youfuns.auth.login;

import com.youfuns.auth.rbac.ResultReturn;
import com.youfuns.auth.rbac.RoleToken;
import com.youfuns.logger.LoggerManager;
import com.youfuns.logger.SimpleLogger;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public final class UserCredentials {
    private final UUID id;
    private final Set<String> usernames;
    private String passwordHash;

    private final LockoutState lockoutState = new LockoutState();

    private final AuthContext<?> context;
    private UserLoginConfig overrideConfig;

    public UserCredentials(
            UUID id, Set<String> usernames, String passwordHash,
            boolean passwordLocked, boolean adminLocked, Instant passwordLockoutExpiry, Instant adminLockoutExpiry,
            int failedAttempts,
            UserLoginConfig userLoginConfig, AuthContext<?> context) {
        this.id = id;
        this.usernames = usernames;
        this.passwordHash = passwordHash;
        this.lockoutState.passwordLocked = passwordLocked;
        this.lockoutState.adminLocked = adminLocked;
        this.lockoutState.passwordLockoutExpiry = passwordLockoutExpiry;
        this.lockoutState.adminLockoutExpiry = adminLockoutExpiry;
        this.lockoutState.failedAttempts.set(failedAttempts);
        this.overrideConfig = userLoginConfig;
        this.context = context;
    }

    public UserCredentials(UUID id, Set<String> usernames, String password,
                           UserLoginConfig userLoginConfig, AuthContext<?> context) {
        LoggerManager.quickLog(this, "Creating UserCredentials instance...");
        this.id = id;
        LoggerManager.quickLog(this, "Hashing usernames and password...");
        this.usernames = new HashSet<>(Set.copyOf(usernames));
        this.context = context;
        this.overrideConfig = userLoginConfig;
        if (getConfig().validatePasswords()) {
            ResultReturn passwordCheck = PasswordStrengthValidator.validatePasswordWithMessage(password);
            if (!passwordCheck.isSuccess()) throw new IllegalArgumentException(passwordCheck.message());
        }
        this.passwordHash = context.hasherConfig().get().hashPassword(password);
        LoggerManager.quickLog(this, "Created UserCredentials instance");
    }

    public static class LockoutState {
        private LockoutState() {}
        private volatile boolean passwordLocked;
        private volatile boolean adminLocked;
        private volatile Instant passwordLockoutExpiry;
        private volatile Instant adminLockoutExpiry;
        private final AtomicInteger failedAttempts = new AtomicInteger(0);
    }

    private UserLoginConfig getConfig() {
        return overrideConfig == null ? context.defaultLoginConfig().get() : overrideConfig;
    }

    public void setConfig(UserLoginConfig config) {
        overrideConfig = config;
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

    public Set<String> getUsernames() {
        return Set.copyOf(usernames);
    }

    // ============= VALIDATION METHODS =============
    public LoginResult login(String username, String password, SessionTokenService sessionTokenService) {
        ResultReturn loginResult = validateLogin(username, password);
        String jwt = null;
        if (loginResult.isSuccess()) {
            jwt = sessionTokenService.generateToken(String.valueOf(this.id));
        }
        return new LoginResult(loginResult, jwt);
    }

    private ResultReturn validateLogin(String username, String password) {
        LoggerManager.quickLog(this, "Processing login for user " + username);

        if (lockoutState.passwordLocked) {
            if (Instant.now().isBefore(lockoutState.passwordLockoutExpiry)) {
                LoggerManager.quickLog(this, "Login attempt on locked account", SimpleLogger.Level.WARN);
                return new ResultReturn(ResultReturn.Result.FAILURE, "Account is temporarily locked. Try again later.");
            } else {
                // Lockout expired, reset
                lockoutState.passwordLocked = false;
                lockoutState.failedAttempts.set(0);
            }
        }

        if (lockoutState.adminLocked) {
            if (Instant.now().isBefore(lockoutState.adminLockoutExpiry)) {
                LoggerManager.quickLog(this, "Login attempt on locked account", SimpleLogger.Level.WARN);
                return new ResultReturn(ResultReturn.Result.FAILURE, "Account is locked by administrator.");
            } else {
                // Lockout expired, reset
                lockoutState.adminLocked = false;
            }
        }

        boolean usernameExists = usernames.contains(username);
        boolean passwordValid = context.hasherConfig().get().validatePassword(password, this.passwordHash);

        boolean authenticated = usernameExists && passwordValid;

        if (authenticated) {
            // Reset failed attempts on success
            lockoutState.failedAttempts.set(0);
            lockoutState.passwordLocked = false;
            lockoutState.adminLocked = false;
            LoggerManager.quickLog(this, "Successful login for user");
            return new ResultReturn(ResultReturn.Result.SUCCESS, "Login successful");
        } else {
            int attempts = lockoutState.failedAttempts.get();
            // Increment failed attempts if username is valid
            if (usernameExists) {
                attempts = lockoutState.failedAttempts.incrementAndGet();
                LoggerManager.quickLog(this, "Failed login attempt " + attempts +
                        " for user", SimpleLogger.Level.WARN);
            }
            // Lock account if too many failures
            if (attempts >= getConfig().maxFailedAttempts()) {
                lockAccountForPassword();
                return new ResultReturn(ResultReturn.Result.FAILURE, "Account is temporarily locked");
            }

            // Return generic message
            return getConfig().genericLoginFailure();
        }
    }

    private void lockAccountForPassword() {
        lockoutState.passwordLocked = true;
        lockoutState.passwordLockoutExpiry = Instant.now().plusSeconds(getConfig().lockoutDurationSeconds());
        LoggerManager.quickLog(this, "Account locked for user for " + getConfig().lockoutDurationSeconds()+ " seconds", SimpleLogger.Level.WARN);

    }

    private void lockAccount(int seconds) {
        lockoutState.adminLocked = true;
        lockoutState.adminLockoutExpiry = Instant.now().plusSeconds(seconds);
        LoggerManager.quickLog(this, "Account locked for user for " + seconds + " seconds", SimpleLogger.Level.WARN);
    }

    public void unlockAccount(RoleToken rt) {
        LoggerManager.quickLog(this, "Called unlock account for user");
        context.permissions().checkManageUsers(rt);
        lockoutState. passwordLocked = false;
        lockoutState.adminLocked = false;
        lockoutState. passwordLockoutExpiry = null;
        lockoutState.adminLockoutExpiry = null;
        lockoutState.failedAttempts.set(0);
        LoggerManager.quickLog(this, "Account unlocked by admin for user", SimpleLogger.Level.INFO);
    }

    public void lockAccount(RoleToken rt, int seconds) {
        LoggerManager.quickLog(this, "Called lock account for user");
        context.permissions().checkManageUsers(rt);
        lockAccount(seconds);
    }

    // ============= PASSWORD MANAGEMENT =============

    public ResultReturn changePassword(RoleToken rt, String oldPassword, String newPassword) {
        LoggerManager.quickLog(this, "Called change password for user");

        context.permissions().checkManageSelf(rt, this.id);

        // Verify old password
        if (!context.hasherConfig().get().validatePassword(oldPassword, this.passwordHash)) {
            LoggerManager.quickLog(this, "Failed password change attempt - incorrect old password",
                    SimpleLogger.Level.WARN);
            return new ResultReturn(ResultReturn.Result.FAILURE, "Incorrect current password.");
        }

        // Update to new password
        this.passwordHash = context.hasherConfig().get().hashPassword(newPassword);
        LoggerManager.quickLog(this, "Password changed for user");
        return new ResultReturn(ResultReturn.Result.SUCCESS, "Password successfully changed.");
    }

    public void resetPasswordAdmin(RoleToken rt, String newPassword) {
        LoggerManager.quickLog(this, "Called change password for user by admin");
        context.permissions().checkManageUsers(rt);
        this.passwordHash = context.hasherConfig().get().hashPassword(newPassword);
        LoggerManager.quickLog(this, "Password reset by admin for user");
    }

    // ============= USERNAME MANAGEMENT =============

    public ResultReturn addUsername(RoleToken rt, String username) {
        LoggerManager.quickLog(this, "Called add username for user");
        context.permissions().checkManageSelf(rt, this.id);

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
        context.permissions().checkManageSelf(rt, this.id);

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

        context.permissions().checkManageUsers(rt);

        if (usernames.contains(username)) {
            return new ResultReturn(ResultReturn.Result.FAILURE, "Username already exists.");
        }

        this.usernames.add(username);
        LoggerManager.quickLog(this, "Added username by admin to UserCredentials instance");
        return new ResultReturn(ResultReturn.Result.SUCCESS, "Username successfully added.");
    }

    public ResultReturn removeUsernameAdmin(RoleToken rt, String username) {
        LoggerManager.quickLog(this, "Called remove username for user by admin");
        context.permissions().checkManageUsers(rt);

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
        context.permissions().checkManageSelf(rt, this.id);
        if (lockoutState.adminLocked && Instant.now().isAfter(lockoutState.adminLockoutExpiry)) {
            // Auto-unlock if lockout expired
            lockoutState.adminLocked = false;
            lockoutState.adminLockoutExpiry = null;
            lockoutState.failedAttempts.set(0);
        }
        return lockoutState.adminLocked;
    }

    public boolean isLockedAdmin(RoleToken rt) {
        LoggerManager.quickLog(this, "Called passwordLocked for user by admin");
        context.permissions().checkManageUsers(rt);
        if (lockoutState.adminLocked && Instant.now().isAfter(lockoutState.adminLockoutExpiry)) {
            // Auto-unlock if lockout expired
            lockoutState.adminLocked = false;
            lockoutState.adminLockoutExpiry = null;
            lockoutState.failedAttempts.set(0);
        }
        return lockoutState.adminLocked;
    }

}