package com.youfuns.auth.user;

import com.youfuns.auth.login.AuthContext;
import com.youfuns.auth.login.SessionTokenService;
import com.youfuns.auth.login.UserLoginConfig;
import com.youfuns.auth.rbac.*;
import com.youfuns.auth.login.UserCredentials;
import com.youfuns.logger.LoggerManager;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public abstract class User<T extends Enum<T> & Permission> {
    private final UUID id;
    private final UserCredentials userCredentials;
    private final UserRoleHolder<T> userRoleHolder;

    protected final DefaultPermissions<T> defaultPermissions;

    private final Instant createdDate;

    @SuppressWarnings({"unchecked", "rawtypes"})
    public User(Set<UserRole<T>> initialPermissions, UserCredentialPayload userRegistrationPayload,
                AuthContext<T> context, UserLoginConfig customLoginConfig) {
        LoggerManager.quickLog(this, "Creating User instance...");
        this.id = UUID.randomUUID();
        LoggerManager.quickLog(this, "UUID generated: " + id.toString().substring(0, 8) + "...");
        this.userCredentials = new UserCredentials(this.id,
                userRegistrationPayload.usernames(), userRegistrationPayload.password(), customLoginConfig, context);
        LoggerManager.quickLog(this, "Created UserCredentials");
        this.defaultPermissions = context.permissions();
        this.userRoleHolder = new UserRoleHolder<>(id, initialPermissions == null ? (Set) defaultPermissions.getInitialUserPermissions() : initialPermissions, defaultPermissions);
        LoggerManager.quickLog(this, "Created UserRoleHolder");
        this.createdDate = Instant.now();
        LoggerManager.quickLog(this, "User creation complete.");
    }

    public User(UserCredentialPayload userRegistrationPayload, AuthContext<T> context, UserLoginConfig customLoginConfig) {
        this(null, userRegistrationPayload, context, customLoginConfig);
    }

    public User(UUID id, UserCredentials userCredentials, UserRoleHolder<T> userRoleHolder, Instant createdDate, DefaultPermissions<T> defaultPermissions) {
        this.id = id;
        this.userCredentials = userCredentials;
        this.userRoleHolder = userRoleHolder;
        this.createdDate = createdDate;
        this.defaultPermissions = defaultPermissions;
    }

    public UUID getId() {
        return id;
    }
    public RoleToken getToken() {
        return userRoleHolder.getToken();
    }
    public UserCredentials getUserCredentials() {
        return userCredentials;
    }
    public Set<UserRole<T>> getUserRoles() {
        return userRoleHolder.roles();
    }
    protected UserRoleHolder<T> getUserRoleHolder() {
        return userRoleHolder;
    }
    public Instant getCreatedDate() {
        return createdDate;
    }
}
