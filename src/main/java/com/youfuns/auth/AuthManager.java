package com.youfuns.auth;

import java.util.function.BiFunction;
import java.util.function.Function;

public class AuthManager<T extends Enum<T> & Permission> {
    private PermissionChecker<T> permissionChecker;
    private DefaultPermissions<T> defaultPermissions;

    private AuthManager(PermissionChecker<T> permissionChecker, DefaultPermissions<T> defaultPermissions) {
        this.permissionChecker = permissionChecker;
        this.defaultPermissions = defaultPermissions;
        defaultPermissions.assignToClasses(UserCredentials.class, UserRoleHolder.class);
    }

    public static <T extends Enum<T> & Permission> Builder<T> builder(T permission) {
        return new Builder<>();
    }

    public static class Builder<T extends Enum<T> & Permission> {
        private DefaultPermissions<T> defaultPermissions;
        private PermissionChecker<T> permissionChecker;

        private Builder() {
            this.permissionChecker = new PermissionChecker<>();
        }

        public Builder<T> defaultPermissions(T manageSelf, T manageAdmin, T assignRole) {
            if (defaultPermissions != null) throw new IllegalStateException("Default permissions already initialized.");
            this.defaultPermissions = new DefaultPermissions<>(permissionChecker, manageSelf, manageAdmin, assignRole);
            return this;
        }

        public Builder<T> passwordHasher(Function<String, String> passwordHasher, BiFunction<String, String, Boolean> passwordValidator) {
            UserCredentials.setPasswordHasher(passwordHasher, passwordValidator);
            return this;
        }
    }
}
