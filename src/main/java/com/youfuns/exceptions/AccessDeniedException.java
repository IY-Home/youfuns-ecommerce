package com.youfuns.exceptions;

import java.util.List;
import java.util.UUID;
import com.youfuns.ecommerce.auth.UserRole;
import com.youfuns.ecommerce.auth.Permission;

/**
 * Thrown when a user attempts to perform an action they don't have permission for.
 */
public class AccessDeniedException extends RuntimeException {

    private final UUID userId;
    private final List<UserRole> userRole;
    private final Permission requiredPermission;
    private final String attemptedAction;
    private final boolean requiresSpecificUser;
    private final UUID targetUserId;

    // 1. Simple message
    public AccessDeniedException(String message) {
        super(message);
        this.userId = null;
        this.userRole = null;
        this.requiredPermission = null;
        this.attemptedAction = null;
        this.requiresSpecificUser = false;
        this.targetUserId = null;
    }

    // 2. With user context
    public AccessDeniedException(UUID uuid, List<UserRole> userRole, String attemptedAction) {
        super(String.format("Access denied for user %s (role: %s) attempting to %s",
                uuid.toString(), userRole, attemptedAction));
        this.userId = uuid;
        this.userRole = userRole;
        this.requiredPermission = null;
        this.attemptedAction = attemptedAction;
        this.requiresSpecificUser = false;
        this.targetUserId = null;
    }

    // 3. With permission context
    public AccessDeniedException(UUID uuid, List<UserRole> userRole, Permission requiredPermission, String attemptedAction) {
        super(String.format("Access denied for user %s (role: %s) attempting to %s. Required permission: %s",
                uuid.toString(), userRole, attemptedAction, requiredPermission));
        this.userId = uuid;
        this.userRole = userRole;
        this.requiredPermission = requiredPermission;
        this.attemptedAction = attemptedAction;
        this.requiresSpecificUser = false;
        this.targetUserId = null;
    }

    // 4. For PermissionChecker
    public AccessDeniedException(List<UserRole> userRole, Permission requiredPermission) {
        String message = String.format("Access denied for user (role: %s). Required permission: %s",
                    userRole, requiredPermission);
        this.userId = null;
        this.userRole = userRole;
        this.requiredPermission = requiredPermission;
        this.attemptedAction = null;
        this.requiresSpecificUser = false;
        this.targetUserId = null;
        super(message);
    }
    public AccessDeniedException(Permission requiredPermission) {
        String message = String.format("Access denied: The permission (%s) requires a specific user target, commonly the user himself.",
                requiredPermission);
        this.userId = null;
        this.userRole = null;
        this.requiredPermission = requiredPermission;
        this.attemptedAction = "Exercise a permission that requires a specific user without providing specific user";
        this.requiresSpecificUser = true;
        this.targetUserId = null;
        super(message);
    }

    // 5. User and target user mismatch
    public AccessDeniedException(UUID user, UUID target, Permission requiredPermission) {
        super(String.format("Access denied for user %s exercising permission %s: Target user %s does not match user.", user.toString(), requiredPermission, target.toString()));
        this.userId = user;
        this.userRole = null;
        this.requiredPermission = requiredPermission;
        this.attemptedAction = null;
        this.requiresSpecificUser = true;
        this.targetUserId = target;
    }

    // 6. With cause
    public AccessDeniedException(String message, Throwable cause) {
        super(message, cause);
        this.userId = null;
        this.userRole = null;
        this.requiredPermission = null;
        this.attemptedAction = null;
        this.requiresSpecificUser = false;
        this.targetUserId = null;
    }

    // Getters
    public UUID getUserId() { return userId; }
    public List<UserRole> getUserRole() { return List.copyOf(userRole); }
    public Permission getRequiredPermission() { return requiredPermission; }
    public String getAttemptedAction() { return attemptedAction; }
}