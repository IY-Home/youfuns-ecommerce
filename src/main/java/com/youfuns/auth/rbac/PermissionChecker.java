package com.youfuns.auth.rbac;

import com.youfuns.logger.LoggerManager;
import com.youfuns.logger.SimpleLogger;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class PermissionChecker<T extends Enum<T> & Permission> {
    @FunctionalInterface
    public interface UserPermissionHandler {
        boolean checkUser(UUID executor, UUID target);
    }
    @FunctionalInterface
    public interface PermissionHandler {
        boolean checkUser(UUID executor);
    }
    @FunctionalInterface
    public interface GenericUserPermissionHandler {
        boolean checkUser(Permission permission, UUID executor, UUID target);
    }
    @FunctionalInterface
    public interface GenericPermissionHandler {
        boolean checkUser(Permission permission, UUID executor);
    }

    private final Map<T, UserPermissionHandler> userPermHandlers;
    private final Map<T, PermissionHandler> permHandlers;
    private GenericUserPermissionHandler userPermHandler;
    private GenericPermissionHandler permHandler;
    private final TokenManager tokenManager;

    private static final boolean STRICT_CHECK = false;

    public PermissionChecker() {
        userPermHandlers = new HashMap<>();
        permHandlers = new HashMap<>();
        tokenManager = new TokenManager();
        userPermHandler = (a, b, c) -> { return true; };
        permHandler = (a, b) -> { return true; };
    }

    public boolean checkPermission(RoleToken rt, T requiredPermission) {
        UserRoleHolder<T> uh = tokenManager.getToken(rt);
        if (uh == null) {
            LoggerManager.quickLog(PermissionChecker.class, "The provided token is null", SimpleLogger.Level.WARN);
            return false;
        }
        if (requiredPermission.needsSpecificUser() && STRICT_CHECK) {
            LoggerManager.quickLog(PermissionChecker.class, "Permission " + requiredPermission + " needs a specific user target for verification", SimpleLogger.Level.WARN);
            return false;
        }
        return checkPermissionOnly(uh, requiredPermission);
    }
    public boolean checkPermissionWithUser(RoleToken rt, UUID target, T requiredPermission) {
        UserRoleHolder<T> uh = tokenManager.getToken(rt);
        if (uh == null) {
            LoggerManager.quickLog(PermissionChecker.class, "The provided token is null", SimpleLogger.Level.WARN);
            return false;
        }
        if (!checkPermissionOnly(uh, requiredPermission) || !checkUserHandler(requiredPermission, uh.id(), target)) {
            LoggerManager.quickLog(
                    PermissionChecker.class,
                    ("Permission denied for permission with specific target user."),
            SimpleLogger.Level.WARN);

            return false;
        }
        return true;
    }
    public boolean checkPermissionOnly(RoleToken rt, T requiredPermission) {
        UserRoleHolder<T> uh = tokenManager.getToken(rt);
        if (uh == null) {
            LoggerManager.quickLog(PermissionChecker.class, "The provided token is null", SimpleLogger.Level.WARN);
            return false;
        }
        return checkPermissionOnly(uh, requiredPermission) && checkHandler(requiredPermission, uh.id());
    }

    public PermissionChecker<T> onPermission(T perm, PermissionHandler permHandler) {
        if (perm == null) return this;
        permHandlers.put(perm, permHandler);
        return this;
    }

    public PermissionChecker<T> onUserPermission(T perm, UserPermissionHandler userPermHandler) {
        if (perm == null) return this;
        if (!perm.needsSpecificUser()) return this;
        userPermHandlers.put(perm, userPermHandler);
        return this;
    }

    public PermissionChecker<T> onPermission(GenericPermissionHandler permHandler) {
        this.permHandler = permHandler;
        return this;
    }

    public PermissionChecker<T> onUserPermission(GenericUserPermissionHandler userPermHandler) {
        this.userPermHandler = userPermHandler;
        return this;
    }

    private boolean checkPermissionOnly(UserRoleHolder<T> uh, T requiredPermission) {
        LoggerManager.quickLog(PermissionChecker.class, "Checking permission " + requiredPermission + " for user");
        boolean hasPermission = false;
        for (UserRole<T> userRole : uh.roles()) {
            if (userRole.hasPermission(requiredPermission)) {
                LoggerManager.quickLog(PermissionChecker.class, "Role " + userRole.name() + " has permission", SimpleLogger.Level.DEBUG);
                /* for (Permission permission : requiredPermission.implications()) {
                    if (!userRole.hasPermission((T) permission)) {
                        LoggerManager.quickLog(PermissionChecker.class, "Role does not have implied permission " + permission.name() + ", moving on to next role", SimpleLogger.Level.DEBUG);
                        continue mainLoop;
                    }
                } */
                hasPermission = true;
                break;
            }
        }
        if (!hasPermission) {
            LoggerManager.quickLog(PermissionChecker.class, "Permission denied", SimpleLogger.Level.DEBUG);
            return false;
        }
        return hasPermission;
    }

    private boolean checkHandler(T requiredPermission, UUID userId) {
        if (!permHandlers.containsKey(requiredPermission)) return true;
        return permHandlers.get(requiredPermission).checkUser(userId) && permHandler.checkUser(requiredPermission, userId);
    }
    private boolean checkUserHandler(T requiredPermission, UUID userId, UUID targetId) {
        if (!userPermHandlers.containsKey(requiredPermission) || !requiredPermission.needsSpecificUser()) return true;
        return userPermHandlers.get(requiredPermission).checkUser(userId, targetId) && userPermHandler.checkUser(requiredPermission, userId, targetId);
    }

    public void checkPermissionAndThrow(RoleToken rt, T requiredPermission) {
        if (!checkPermission(rt, requiredPermission)) {
            throw new AccessDeniedException("Permission denied for permission " + requiredPermission);
        }
    }
    public void checkPermissionAndThrowWithUser(RoleToken rt, UUID targetId, T requiredPermission) {
        if (!checkPermissionWithUser(rt, targetId, requiredPermission)) {
            throw new AccessDeniedException("Permission denied for permission " + requiredPermission + " on target user");
        }
    }

    public RoleToken issueToken(UserRoleHolder<T> uh) {
        return tokenManager.issueToken(uh);
    }

    private static final class TokenManager {
        private final Map<RoleToken, UserRoleHolder<?>> activeTokens = new ConcurrentHashMap<>();

        @SuppressWarnings("unchecked")
        public <T extends Enum<T> & Permission> UserRoleHolder<T> getToken(RoleToken roleToken) {
            LoggerManager.quickLog(this, "Validating token...");
            UserRoleHolder<T> userRoleHolder = (UserRoleHolder<T>) activeTokens.get(roleToken);
            activeTokens.remove(roleToken); // Remove single-use token
            return userRoleHolder;
        }

        public <T extends Enum<T> & Permission> RoleToken issueToken(UserRoleHolder<T> uh) {
            LoggerManager.quickLog(this, "Issuing token to user with roles " + uh.roles().toString());
            RoleToken token = new RoleToken(UUID.randomUUID(), uh.id());
            activeTokens.put(token, uh);
            LoggerManager.quickLog(this, "Token issued.");
            return token;
        }
    }
}

