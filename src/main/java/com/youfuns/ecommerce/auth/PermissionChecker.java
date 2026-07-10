package com.youfuns.ecommerce.auth;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.youfuns.ecommerce.LoggerManager;
import com.youfuns.ecommerce.user.User;
import com.youfuns.exceptions.AccessDeniedException;
import com.youfuns.paramtypes.UuidFormat;
import com.youfuns.utils.SimpleLogger;

public final class PermissionChecker {
    private static TokenManager tokenManager = new TokenManager();

    private static boolean STRICT_CHECK = false;

    public static void checkPermission(RoleToken rt, Permission requiredPermission) {
        UserRoleHolder uh = tokenManager.getToken(rt);
        if (uh == null) {
            LoggerManager.quickLog(PermissionChecker.class, "The provided token (" + UuidFormat.shortenUUID(rt.id()) + ") is null", SimpleLogger.Level.ERROR);
            throw new AccessDeniedException("The provided token is invalid");
        }
        if (requiredPermission.needsSpecificUser() && STRICT_CHECK) {
            LoggerManager.quickLog(PermissionChecker.class, "Permission " + requiredPermission + " needs a specific user target for verification!", SimpleLogger.Level.ERROR);
            throw new AccessDeniedException(requiredPermission);
        }
        checkPermissionOnly(uh, requiredPermission);
    }
    public static void checkPermissionWithUser(RoleToken rt, UUID target, Permission requiredPermission) {
        UserRoleHolder uh = tokenManager.getToken(rt);
        if (uh == null) {
            LoggerManager.quickLog(PermissionChecker.class, "The provided token (" + UuidFormat.shortenUUID(rt.id()) + ") is null", SimpleLogger.Level.ERROR);
            throw new AccessDeniedException("The provided token is invalid");
        }
        checkPermissionOnly(uh, requiredPermission);
        if (!uh.getId().equals(target)) {
            LoggerManager.quickLog(
                    PermissionChecker.class,
                    ("Permission denied: User ("
                        + UuidFormat.shortenUUID(uh.getId())
                        + ") does not match action target ("
                        + UuidFormat.shortenUUID(target) + ")!"),
            SimpleLogger.Level.ERROR);

            throw new AccessDeniedException(uh.getId(), target, requiredPermission);
        }
    }
    public static void checkPermissionOnly(RoleToken rt, Permission requiredPermission) {
        UserRoleHolder uh = tokenManager.getToken(rt);
        if (uh == null) {
            LoggerManager.quickLog(PermissionChecker.class, "The provided token (" + UuidFormat.shortenUUID(rt.id()) + ") is null", SimpleLogger.Level.ERROR);
            throw new AccessDeniedException("The provided token is invalid");
        }
        checkPermissionOnly(uh, requiredPermission);
    }

    private static void checkPermissionOnly(UserRoleHolder uh, Permission requiredPermission) {
        LoggerManager.quickLog(PermissionChecker.class, "Checking permission " + requiredPermission + " for user " + UuidFormat.shortenUUID(uh.getId()));
        boolean hasPermission = false;
        for (UserRole userRole : uh.getRoles()) {
            if (userRole.hasPermission(requiredPermission)) {
                hasPermission = true;
                break;
            }
        }
        if (!hasPermission) {
            LoggerManager.quickLog(PermissionChecker.class, "Permission denied!", SimpleLogger.Level.ERROR);
            throw new AccessDeniedException(uh.getRoles(), requiredPermission);
        }
    }

    public static RoleToken issueToken(UserRoleHolder uh) {
        return tokenManager.issueToken(uh);
    }

    private static final class TokenManager {
        private final int TTL_SECONDS = 10;
        private final Map<RoleToken, UserRoleHolder> activeTokens = new ConcurrentHashMap<>();
        private final ScheduledExecutorService cleanup = Executors.newSingleThreadScheduledExecutor();
        public TokenManager() {
            cleanup.scheduleAtFixedRate(this::removeExpiredTokens, 1, 1, TimeUnit.MINUTES);
        }
        private void removeExpiredTokens() {
            LoggerManager.quickLog(this, "Removing expired tokens...");
            Instant now = Instant.now();
            activeTokens.entrySet().removeIf(entry -> entry.getKey().expirationDate().isBefore(now));
        }

        public UserRoleHolder getToken(RoleToken roleToken) {
            LoggerManager.quickLog(this, "Validating token...");
            UserRoleHolder userRoleHolder = activeTokens.get(roleToken);
            activeTokens.remove(roleToken); // Remove single-use token
            return userRoleHolder;
        }

        public RoleToken issueToken(UserRoleHolder uh) {
            LoggerManager.quickLog(this, "Issuing token to user " + UuidFormat.shortenUUID(uh.getId()) + " with roles " + uh.getRoles().toString());
            RoleToken token = new RoleToken(UUID.randomUUID(), Instant.now(), Instant.now().plusSeconds(TTL_SECONDS), uh.getId());
            activeTokens.put(token, uh);
            LoggerManager.quickLog(this, "Token issued.");
            return token;
        }
    }
}

