package com.youfuns.cms.auth;

import com.youfuns.auth.UserRole;

import java.util.Set;

public class CmsRole {

    // ===== NORMAL USER =====
    public static final UserRole<CmsPermission> NORMAL_USER = new UserRole<>(
            "NORMAL_USER",
            Set.of(
                    CmsPermission.VIEW_PUBLIC_CONTENT,
                    CmsPermission.VIEW_PRIVATE_CONTENT,  // own content only
                    CmsPermission.CREATE_CONTENT,
                    CmsPermission.EDIT_OWN_CONTENT,
                    CmsPermission.DELETE_OWN_CONTENT
            )
    );

    // ===== MODERATOR =====
    public static final UserRole<CmsPermission> MODERATOR = new UserRole<>(
            "MODERATOR",
            Set.of(
                    // Content management
                    CmsPermission.VIEW_ALL_CONTENT,
                    CmsPermission.EDIT_ANY_CONTENT,
                    CmsPermission.DELETE_ANY_CONTENT,
                    CmsPermission.APPROVE_CONTENT,
                    CmsPermission.REJECT_CONTENT,
                    CmsPermission.PIN_CONTENT,
                    CmsPermission.UNPIN_CONTENT,
                    CmsPermission.LOCK_CONTENT,
                    CmsPermission.UNLOCK_CONTENT,

                    // User management (basic)
                    CmsPermission.VIEW_USER_PROFILES,
                    CmsPermission.WARN_USER,
                    CmsPermission.MUTE_USER,
                    CmsPermission.UNMUTE_USER
            )
    );

    // ===== ADMIN =====
    public static final UserRole<CmsPermission> ADMIN = new UserRole<>(
            "ADMIN",
            Set.of(
                    // All moderator permissions
                    CmsPermission.VIEW_ALL_CONTENT,
                    CmsPermission.EDIT_ANY_CONTENT,
                    CmsPermission.DELETE_ANY_CONTENT,
                    CmsPermission.APPROVE_CONTENT,
                    CmsPermission.REJECT_CONTENT,
                    CmsPermission.PIN_CONTENT,
                    CmsPermission.UNPIN_CONTENT,
                    CmsPermission.LOCK_CONTENT,
                    CmsPermission.UNLOCK_CONTENT,

                    // Full user management
                    CmsPermission.MANAGE_USERS,
                    CmsPermission.MANAGE_ROLES,
                    CmsPermission.SUSPEND_USER,
                    CmsPermission.DELETE_USERS,

                    // System access
                    CmsPermission.MANAGE_SYSTEM,
                    CmsPermission.VIEW_LOGS,
                    CmsPermission.VIEW_ANALYTICS,
                    CmsPermission.CONFIGURE_SYSTEM
            )
    );

    // ===== SUPER ADMIN =====
    public static final UserRole<CmsPermission> SUPER_ADMIN = new UserRole<>(
            "SUPER_ADMIN",
            Set.of(CmsPermission.SUPER_ADMIN)
    );
}