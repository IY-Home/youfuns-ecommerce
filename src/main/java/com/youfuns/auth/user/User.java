package com.youfuns.auth.user;

import com.youfuns.auth.rbac.*;
import com.youfuns.auth.login.UserCredentials;
import com.youfuns.logger.LoggerManager;

import java.util.Set;
import java.util.UUID;

public abstract class User<T extends Enum<T> & Permission> {
    private final UUID id;
    private final UserCredentials userCredentials;
    private final UserRoleHolder<T> userRoleHolder;

    protected static DefaultPermissions<?> defaultPermissions;

    static void setDefaultPermissions(DefaultPermissions defaultPermissions) {
        User.defaultPermissions = defaultPermissions;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public User(Set<UserRole<T>> initialPermissions, UserCredentialPayload userRegistrationPayload) {
        LoggerManager.quickLog(this, "Creating User instance...");
        this.id = UUID.randomUUID();
        LoggerManager.quickLog(this, "UUID generated: " + id.toString().substring(0, 8) + "...");
        this.userCredentials = new UserCredentials(this.id, Set.of(
                userRegistrationPayload.username()
        ), userRegistrationPayload.password());
        LoggerManager.quickLog(this, "Created UserCredentials");
        this.userRoleHolder = new UserRoleHolder<>(id, initialPermissions == null ? (Set) defaultPermissions.getInitialUserPermissions() : initialPermissions);
        LoggerManager.quickLog(this, "Created UserRoleHolder");
        LoggerManager.quickLog(this, "User creation complete.");
    }

    public User(UserCredentialPayload userRegistrationPayload) {
        this(null, userRegistrationPayload);
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
    public UserRoleHolder<T> getUserRoleHolder() {
        return userRoleHolder;
    }
}
