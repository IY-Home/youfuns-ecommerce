package com.youfuns.auth.rbac;

import java.util.Set;
import java.util.UUID;

public class AlwaysTruePermissions<T extends Enum<T> & Permission> extends DefaultPermissions<T> {
    public AlwaysTruePermissions(Set<UserRole<T>> initialUserPermissions) {
        super(initialUserPermissions, null, null, null, null);
    }

    @Override
    public void checkManageSelf(RoleToken rt, UUID userId) {
        return;
    }

    public void checkManageUsers(RoleToken rt) {
        return;
    }

    public void checkManageRoles(RoleToken rt) {
        return;
    }

    @Override
    public RoleToken issueToken(UserRoleHolder<T> uh) {
        return uh.getToken();
    }
}