package com.youfuns.auth;

import com.youfuns.auth.login.UserCredentials;
import com.youfuns.auth.rbac.*;
import com.youfuns.logger.LoggerManager;
import com.youfuns.repo.Repository;
import com.youfuns.auth.user.User;
import com.youfuns.webserver.JwtService;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.Function;

public class AuthManager<PermissionClass extends Enum<PermissionClass> & Permission, UserClass extends User<PermissionClass>> {
    private PermissionChecker<PermissionClass> permissionChecker;
    private DefaultPermissions<PermissionClass> defaultPermissions;
    private Repository<UUID, UserClass> userRepository;
    private final BiFunction<Repository<UUID, UserClass>, String, List<UserClass>> findUsersByUsername;

    private AuthManager(PermissionChecker<PermissionClass> permissionChecker,
                        DefaultPermissions<PermissionClass> defaultPermissions,
                        Repository<UUID, UserClass> initialRepository,
                        BiFunction<Repository<UUID, UserClass>, String, List<UserClass>> findUsersByUsername) {
        this.permissionChecker = permissionChecker;
        this.defaultPermissions = defaultPermissions;
        this.userRepository = initialRepository;
        this.findUsersByUsername = findUsersByUsername;
        defaultPermissions.assignToClasses(UserCredentials.class, UserRoleHolder.class, User.class);
        LoggerManager.quickLog(this, "Created AuthManager");
    }

    public static <T extends Enum<T> & Permission, U extends User<T>> Builder<T, U> builder(Class<T> permission, Class<U> user) {
        return new Builder<>();
    }

    public UserCredentials.LoginResult login(String username, String password) {
        UserCredentials.LoginResult loginResult = null;
        for (UserClass user : findUsersByUsername.apply(userRepository, username)) {
            loginResult = user.getUserCredentials().login(username, password);
            if (!loginResult.isSuccess()) continue;
            break;
        }
        return (loginResult == null) ? new UserCredentials.LoginResult(UserCredentials.genericLoginFailure, null) : loginResult;
    }

    public ResultReturn addUser(UserClass user) {
        Set<String> usernames = user.getUserCredentials().getUsernames();
        for (String username : usernames) {
            if (!findUsersByUsername.apply(userRepository, username).isEmpty()) {
                return new ResultReturn(ResultReturn.Result.FAILURE, "Username is already in use");
            }
        }
        return userRepository.insert(user.getId(), user);
    }

    public Optional<UserClass> findById(UUID id) {
        return userRepository.findById(id);
    }

    public UserClass getUserFromJwt(String jwt) {
        String subject = JwtService.extractSubject(jwt);
        if (subject == null) return null;
        Optional<UserClass> user = userRepository.findById(UUID.fromString(subject));
        return user.orElse(null);
    }

    public PermissionChecker<PermissionClass> getPermissionChecker() {
        return permissionChecker;
    }

    public ResultReturn deleteUser(UserClass user) {
        defaultPermissions.checkManageSelf(user.getUserRoleHolder().getToken(), user.getId());
        return userRepository.delete(user.getId());
    }

    public static class Builder<PermissionClass extends Enum<PermissionClass> & Permission, UserClass extends User<PermissionClass>> {
        private DefaultPermissions<PermissionClass> defaultPermissions;
        private final PermissionChecker<PermissionClass> permissionChecker;
        private Repository<UUID, UserClass> userRepository;
        private BiFunction<Repository<UUID, UserClass>, String, List<UserClass>> findUsersByUsername;

        private Builder() {
            LoggerManager.quickLog(this, "Beginning AuthManager.Builder");
            this.permissionChecker = new PermissionChecker<>();
        }

        public Builder<PermissionClass, UserClass> defaultPermissions(Set<UserRole<PermissionClass>> initialPerms, PermissionClass manageSelf, PermissionClass manageAdmin, PermissionClass assignRole) {
            if (defaultPermissions != null) throw new IllegalStateException("Default permissions already initialized.");
            this.defaultPermissions = new DefaultPermissions<>(initialPerms, permissionChecker, manageSelf, manageAdmin, assignRole);
            LoggerManager.quickLog(this, "Set default permissions");
            return this;
        }

        public Builder<PermissionClass, UserClass> passwordHasher(Function<String, String> passwordHasher, BiFunction<String, String, Boolean> passwordValidator) {
            UserCredentials.setPasswordHasher(passwordHasher, passwordValidator);
            LoggerManager.quickLog(this, "Set password hasher");
            return this;
        }

        public Builder<PermissionClass, UserClass> enablePasswordStrengthValidation(boolean enable) {
            UserCredentials.enablePasswordValidation(enable);
            LoggerManager.quickLog(this, "Set enable password strength validation:");
            return this;
        }

        public Builder<PermissionClass, UserClass> repository(Repository<UUID, UserClass> userRepository) {
            this.userRepository = userRepository;
            LoggerManager.quickLog(this, "Set repository");
            return this;
        }

        public Builder<PermissionClass, UserClass> toFindUsersByUsername(BiFunction<Repository<UUID, UserClass>, String, List<UserClass>> function) {
            this.findUsersByUsername = function;
            LoggerManager.quickLog(this, "Set findUsersByUsername function");
            return this;
        }

        public Builder<PermissionClass, UserClass> selfPermissionEqualsSelf() {
            permissionChecker.onUserPermission((permission, executor, target) -> executor.equals(target));
            LoggerManager.quickLog(this, "Enabled selfPermissionEqualsSelf");
            return this;
        }

        public AuthManager<PermissionClass, UserClass> build() {
            if (defaultPermissions == null) throw new IllegalStateException("Default permissions not initialized.");
            if (userRepository == null) throw new IllegalStateException("User repository not initialized.");
            if (findUsersByUsername == null) throw new IllegalStateException("Username search function not initialized.");
            LoggerManager.quickLog(this, "Building AuthManager");
            return new AuthManager<>(permissionChecker, defaultPermissions, userRepository, findUsersByUsername);
        }
    }
}
