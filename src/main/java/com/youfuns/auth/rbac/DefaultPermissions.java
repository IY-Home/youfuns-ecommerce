package com.youfuns.auth.rbac;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;

public sealed class DefaultPermissions<T extends Enum<T> & Permission> permits DefaultPermissions.AlwaysTruePermissions {
    final PermissionChecker<T> checker;
    final Set<UserRole<T>> initialUserPermissions;
    final T manageSelfPermission;
    final T manageUsersPermission;
    final T assignRolePermission;

    public DefaultPermissions(Set<UserRole<T>> initialUserPermissions, PermissionChecker<T> checker, T manageSelf, T manageAdmin, T assignRole) {
        this.initialUserPermissions = Collections.unmodifiableSet(initialUserPermissions);
        this.checker = checker;
        this.manageSelfPermission = manageSelf;
        this.manageUsersPermission = manageAdmin;
        this.assignRolePermission = assignRole;
    }

    public void checkManageSelf(RoleToken rt, UUID userId) {
        checker.checkPermissionAndThrowWithUser(rt, userId, this.manageSelfPermission);
    }

    public void checkManageUsers(RoleToken rt) {
        checker.checkPermissionAndThrow(rt, this.manageUsersPermission);
    }

    public void checkManageRoles(RoleToken rt) {
        checker.checkPermissionAndThrow(rt, this.assignRolePermission);
    }

    public PermissionChecker<T> getChecker() {
        return checker;
    }

    public Set<UserRole<T>> getInitialUserPermissions() {
        return initialUserPermissions;
    }

    public RoleToken issueToken(UserRoleHolder<T> uh) {
        return checker.issueToken(uh);
    }


    // ONLY for testing isolated components
    public static final class AlwaysTruePermissions<T extends Enum<T> & Permission> extends DefaultPermissions<T> {
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
            return uh == null ? new RoleToken(UUID.randomUUID(), UUID.randomUUID()) : checker.issueToken(uh);
        }
    }

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
}