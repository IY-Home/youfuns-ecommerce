package com.youfuns.cms.auth;

import com.youfuns.auth.Permission;

import java.util.Collections;
import java.util.List;

public enum CmsPermission implements Permission {
    // ===== CONTENT PERMISSIONS =====
    VIEW_PUBLIC_CONTENT(false, List.of()),
    VIEW_PRIVATE_CONTENT(true, List.of(VIEW_PUBLIC_CONTENT)),  // needs specific user/group
    CREATE_CONTENT(true, List.of()),                           // own content only
    EDIT_OWN_CONTENT(true, List.of(CREATE_CONTENT)),          // edit what you created
    DELETE_OWN_CONTENT(true, List.of(CREATE_CONTENT)),        // delete what you created

    // ===== MODERATOR PERMISSIONS =====
    VIEW_ALL_CONTENT(false, List.of(VIEW_PUBLIC_CONTENT, VIEW_PRIVATE_CONTENT)),
    EDIT_ANY_CONTENT(false, List.of(EDIT_OWN_CONTENT, VIEW_ALL_CONTENT)),
    DELETE_ANY_CONTENT(false, List.of(DELETE_OWN_CONTENT, VIEW_ALL_CONTENT)),
    APPROVE_CONTENT(false, List.of(VIEW_ALL_CONTENT)),
    REJECT_CONTENT(false, List.of(VIEW_ALL_CONTENT)),
    PIN_CONTENT(false, List.of(VIEW_ALL_CONTENT)),
    UNPIN_CONTENT(false, List.of(VIEW_ALL_CONTENT)),
    LOCK_CONTENT(false, List.of(VIEW_ALL_CONTENT)),
    UNLOCK_CONTENT(false, List.of(VIEW_ALL_CONTENT)),

    // ===== USER MANAGEMENT (MODERATOR+) =====
    VIEW_USER_PROFILES(false, List.of()),
    MUTE_USER(true, List.of(VIEW_USER_PROFILES)),            // specific user
    UNMUTE_USER(true, List.of(VIEW_USER_PROFILES)),          // specific user
    WARN_USER(true, List.of(VIEW_USER_PROFILES)),            // specific user
    SUSPEND_USER(true, List.of(VIEW_USER_PROFILES)),         // specific user

    // ===== ADMIN PERMISSIONS =====
    MANAGE_USERS(false, List.of(
            VIEW_USER_PROFILES,
            MUTE_USER, UNMUTE_USER,
            WARN_USER, SUSPEND_USER
    )),
    MANAGE_ROLES(false, List.of(MANAGE_USERS)),
    MANAGE_SYSTEM(false, List.of(MANAGE_ROLES)),
    VIEW_LOGS(false, List.of(MANAGE_SYSTEM)),
    VIEW_ANALYTICS(false, List.of(MANAGE_SYSTEM)),
    CONFIGURE_SYSTEM(false, List.of(MANAGE_SYSTEM)),
    DELETE_USERS(false, List.of(MANAGE_USERS, SUSPEND_USER)),

    // ===== SUPER ADMIN (ALL PERMISSIONS) =====
    SUPER_ADMIN(false, List.of(
            MANAGE_USERS, MANAGE_ROLES, MANAGE_SYSTEM,
            VIEW_LOGS, VIEW_ANALYTICS, CONFIGURE_SYSTEM,
            DELETE_USERS,
            VIEW_ALL_CONTENT, EDIT_ANY_CONTENT, DELETE_ANY_CONTENT,
            APPROVE_CONTENT, REJECT_CONTENT, PIN_CONTENT, LOCK_CONTENT,
            SUSPEND_USER, DELETE_USERS
    ));

    private final boolean needsSpecificUser;
    private final List<? extends Permission> implications;

    CmsPermission(boolean needsSpecificUser, List<? extends Permission> implications) {
        this.needsSpecificUser = needsSpecificUser;
        this.implications = Collections.unmodifiableList(implications);
    }

    @Override
    public boolean needsSpecificUser() {
        return needsSpecificUser;
    }

    @Override
    public List<? extends Permission> implications() {
        return implications;
    }
}