package com.youfuns.cms.user;

import com.youfuns.auth.UserCredentials;
import com.youfuns.auth.UserRole;
import com.youfuns.auth.UserRoleHolder;
import com.youfuns.cms.auth.CmsPermission;
import com.youfuns.cms.auth.CmsRole;
import com.youfuns.cms.paramtypes.EmailAddress;
import com.youfuns.cms.paramtypes.PhoneNumber;
import com.youfuns.cms.paramtypes.Username;
import com.youfuns.logger.LoggerManager;

import java.util.Set;
import java.util.UUID;

public class User {
    private final UUID id;
    private final UserInfo userInfo;
    private final UserCredentials userCredentials;
    private final UserRoleHolder<CmsPermission> userRoleHolder;

    private static final Set<UserRole<CmsPermission>> initialRoles;

    static {
        initialRoles = Set.of(CmsRole.NORMAL_USER);
    }

    public User(UserRegistrationPayload userRegistrationPayload) {
        LoggerManager.quickLog(this, "Creating User instance...");
        this.id = UUID.randomUUID();
        LoggerManager.quickLog(this, "UUID generated: " + id.toString().substring(0, 8) + "...");
        this.userInfo = new UserInfo(
                id,
                userRegistrationPayload.name(),
                new EmailAddress(userRegistrationPayload.email()),
                new PhoneNumber(userRegistrationPayload.phone()),
                new Username(userRegistrationPayload.username()));
        LoggerManager.quickLog(this, "Created UserInfo");
        this.userCredentials = new UserCredentials(this.id, Set.of(
                userRegistrationPayload.email(),
                userRegistrationPayload.username()
        ), userRegistrationPayload.password());
        LoggerManager.quickLog(this, "Created UserCredentials");
        this.userRoleHolder = new UserRoleHolder<CmsPermission>(id, initialRoles);
        LoggerManager.quickLog(this, "Created UserRoleHolder");
        LoggerManager.quickLog(this, "User creation complete.");
    }
    public UUID getId() {
        return id;
    }
    public UserInfo getUserInfo() {
        return userInfo;
    }
    public UserCredentials getUserCredentials() {
        return userCredentials;
    }
    public UserRoleHolder<CmsPermission> getUserRoleHolder() {
        return userRoleHolder;
    }
}
