package com.youfuns.ecommerce.auth;

import com.youfuns.ecommerce.LoggerManager;
import com.youfuns.paramtypes.UuidFormat;
import com.youfuns.ecommerce.frontend.utils.ResultReturn;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class UserRoleHolder {
    private final UUID id;
    private final List<UserRole> roles;
    private static final UserRole[] defaultRoles = { UserRole.CUSTOMER };

    private static final UUID BOOTSTRAP_TOKEN = System.getenv("YOUFUNS_BOOTSTRAP_MANAGER_TOKEN") != null ? UUID.fromString(System.getenv("YOUFUNS_BOOTSTRAP_MANAGER_TOKEN")) : UUID.randomUUID();;
    private static boolean BOOTSTRAP_REGISTERED = false;


    public UserRoleHolder(UUID id) {
        LoggerManager.quickLog(this, "Creating UserRoleHolder instance...");
        this.id = id;
        this.roles = new ArrayList<>(Arrays.asList(defaultRoles));
        LoggerManager.quickLog(this, "Created UserRoleHolder instance with id " + UuidFormat.shortenUUID(id) + " and roles " + Arrays.toString(defaultRoles));
    }

    public List<UserRole> getRoles() {
        LoggerManager.quickLog(this, "Getting UserRoles from UserCredentials instance...");
        return List.copyOf(roles);
    }

    public ResultReturn addRole(RoleToken commandingUser, UserRole role) {
        // Check if the commanding user has the permission to add user roles
        PermissionChecker.checkPermission(commandingUser, Permission.MANAGE_ANY_ROLES);
        // If no exception, add the role to self roles
        this.roles.add(role);
        LoggerManager.quickLog(this, "Role " + role + " added to UserCredentials instance with id " + UuidFormat.shortenUUID(id) + " by commanding user");
        return new ResultReturn(ResultReturn.Result.SUCCESS, "Role " + role + " successfully added.");
    }

    public ResultReturn removeRole(RoleToken commandingUser, UserRole role) {
        PermissionChecker.checkPermission(commandingUser, Permission.MANAGE_ANY_ROLES);
        this.roles.remove(role);
        LoggerManager.quickLog(this, "Role " + role + " removed from UserCredentials instance with id " + UuidFormat.shortenUUID(id) + " by commanding user");
        return new ResultReturn(ResultReturn.Result.SUCCESS, "Role " + role + " successfully removed.");
    }

    boolean addRoleBootstrap(UUID bootstrapToken) {
        LoggerManager.quickLog(this, "Called addRoleBootstrap() to user " + UuidFormat.shortenUUID(id));
        if (BOOTSTRAP_REGISTERED) {
            LoggerManager.quickLog(this, "Manager already registered!");
            return false;
        }
        if (bootstrapToken == null) {
            LoggerManager.quickLog(this, "BootstrapToken is null!");
            return false;
        }
        if (!BOOTSTRAP_TOKEN.equals(bootstrapToken)) {
            LoggerManager.quickLog(this, "BootstrapToken is invalid!");
            return false;
        }
        BOOTSTRAP_REGISTERED = true;
        this.roles.add(UserRole.MANAGER);
        LoggerManager.quickLog(this, "User roles updated: " + Arrays.toString(roles.toArray()));
        LoggerManager.quickLog(this, "Bootstrap manager registered successfully");
        return true;
    }

    public UUID getId() {
        return id;
    }

    public RoleToken getToken() {
        return PermissionChecker.issueToken(this);
    }
}
