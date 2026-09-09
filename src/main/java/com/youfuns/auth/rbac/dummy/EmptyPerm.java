package com.youfuns.auth.rbac.dummy;

import com.youfuns.auth.rbac.Permission;

import java.util.Set;

public enum EmptyPerm implements Permission {
    NO_USER(false, Set.of()),
    SPECIFIC_USER(true, Set.of()),
    ALL(false, Set.of(NO_USER, SPECIFIC_USER));

    private final boolean needsSpecificUser;
    private final Set<Permission> permissions;

    EmptyPerm(boolean needsSpecificUser, Set<Permission> permissions) {
        this.needsSpecificUser = needsSpecificUser;
        this.permissions = permissions;
    }

    @Override
    public boolean needsSpecificUser() {
        return needsSpecificUser;
    }

    @Override
    public Set<? extends Permission> implications() {
        return permissions;
    }
}
