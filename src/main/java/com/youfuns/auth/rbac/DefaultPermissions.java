package com.youfuns.auth.rbac;

import com.youfuns.logger.LoggerManager;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.UUID;

public class DefaultPermissions<T extends Enum<T> & Permission> {
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

    public Set<UserRole<T>> getInitialUserPermissions() {
        return initialUserPermissions;
    }

    public RoleToken issueToken(UserRoleHolder<T> uh) {
        return checker.issueToken(uh);
    }

    public void assignToClasses(Class<?>... classes) {
        LoggerManager.quickLog(this, "Assigning permissions to classes: " + Arrays.toString(classes));
        for (Class<?> clazz : classes) {
            try {
                Method method = clazz.getDeclaredMethod("setDefaultPermissions", DefaultPermissions.class);
                method.setAccessible(true);
                method.invoke(null, this);
            } catch (NoSuchMethodException | IllegalAccessException |
                     InvocationTargetException e) {
                LoggerManager.quickLog(this, e.getClass().getSimpleName() + " when assigning permissions to class " + clazz.getName() + ": " + e.getMessage());
                continue;
            }
        }
    }
}